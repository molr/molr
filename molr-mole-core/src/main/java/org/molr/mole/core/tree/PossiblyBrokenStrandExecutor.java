package org.molr.mole.core.tree;

import com.google.common.collect.ImmutableList;
import org.molr.commons.domain.Block;
import org.molr.commons.domain.RunState;
import org.molr.commons.domain.Strand;
import org.molr.commons.domain.StrandCommand;
import org.molr.mole.core.utils.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Objects.requireNonNull;

/**
 * NOTE: Synchronization is still under evaluation.. for the moment all the methods are synchronizeb but it should
 * be changed asap.
 */
public class PossiblyBrokenStrandExecutor implements StrandExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PossiblyBrokenStrandExecutor.class);

    private final TreeStructure structure;
    private final Strand strand;
    private final StrandFactory strandFactory;
    private final LeafExecutor leafExecutor;
    private final ReplayProcessor<RunState> stateSink;
    private final Flux<RunState> stateStream;
    private final ReplayProcessor<Block> blockSink;
    private final Flux<Block> blockStream;
    private ImmutableList<StrandExecutor> childExecutors;
    private RunState actualState;
    private Block actualBlock;

    public PossiblyBrokenStrandExecutor(Strand strand, Block actualBlock, TreeStructure structure, StrandFactory strandFactory, LeafExecutor leafExecutor) {
        requireNonNull(actualBlock, "actualBlock cannot be null");
        this.structure = requireNonNull(structure, "structure cannot be null");
        this.strand = requireNonNull(strand, "strand cannot be null");
        this.strandFactory = requireNonNull(strandFactory, "strandFactory cannot be null");
        this.leafExecutor = requireNonNull(leafExecutor, "leafExecutor cannot be null");
        childExecutors = ImmutableList.of();
        stateSink = ReplayProcessor.cacheLast();
        stateStream = stateSink.publishOn(Schedulers.elastic());
        blockSink = ReplayProcessor.cacheLast();
        blockStream = blockSink.publishOn(Schedulers.elastic());

        updateActualBlock(actualBlock);
        updateState(RunState.PAUSED);
    }

    @Override
    public synchronized void instruct(StrandCommand command) {
        switch (command) {
            case PAUSE:
                pause();
            case SKIP:
                skip();
            case RESUME:
                resume();
            case STEP_INTO:
                stepInto();
            case STEP_OVER:
                stepOver();
        }
        throw new IllegalArgumentException("Command '" + command + "' could not be interpreted.");
    }

    private synchronized CompletableFuture<Boolean> resume() {
        return CompletableFuture.supplyAsync(() -> {
            updateState(RunState.RUNNING);
            while (actualState == RunState.RUNNING) {
                if (structure.isLeaf(actualBlock)) {
                    CompletableFuture<Boolean> leafFuture = leafExecutor.executeAsync(actualBlock);
                    try {
                        Boolean leafOk = leafFuture.get();
                        if (!leafOk) {
                            LOGGER.warn("[{}] execution of {} returned {}. Pausing strand", strand, actualBlock, leafOk);
                            updateState(RunState.PAUSED);
                            return false;
                        }
                    } catch (InterruptedException e) {
                        throw new IllegalStateException(strand + " leaf execution interrupted", e);
                    } catch (ExecutionException e) {
                        throw new IllegalStateException(strand + " leaf execution threw exception", e);
                    }
                }

                if (structure.isParallel(actualBlock)) {
                    LOGGER.warn("[{}] resume will run parallel blocks sequentially for the moment ({}). WORK IN PROGRESS", strand, actualBlock);
                }

                if (structure.isLeaf(actualBlock)) {
                    moveNext();
                } else {
                    moveInto();
                }
            }

            return true;
        });

    }

    private synchronized CompletableFuture<Boolean> pause() {
        LOGGER.debug("[{}] paused and instructed children to pause", strand);
        updateState(RunState.PAUSED);
        childExecutors.forEach(child -> child.instruct(StrandCommand.PAUSE));
        return CompletableFuture.completedFuture(true);
    }

    private synchronized CompletableFuture<Boolean> stepOver() {
        if (structure.isLeaf(actualBlock)) {
            return runLeaf().thenApply(res -> {
                moveNext();
                return res;
            });
        }

//        if (structure.isParallel(actualBlock)) {
//            updateState(RunState.RUNNING);
//            List<Mono<Boolean>> futuresAsMono = structure.childrenOf(actualBlock).stream()
//                    .map(this::createChildStrandExecutor)
//                    .map(childExecutor -> childExecutor.instruct(StrandCommand.STEP_OVER))
//                    .map(Mono::fromFuture)
//                    .collect(Collectors.toList());
//
//            return Mono.zip(futuresAsMono, objs -> ArrayUtils.convertArrayTo(objs, Boolean.class))
//                    .map((List<Boolean> booleans) -> booleans.stream().reduce(true, Boolean::logicalAnd))
//                    .doOnNext(r -> LOGGER.debug("[{}] children RESUME finished with result {}", strand, r))
//                    .doOnNext(r -> updateState(RunState.PAUSED))
//                    .doOnNext(r -> moveNext())
//                    .toFuture();
//        }

        boolean overallResult = true;
        for (Block childBlock : structure.childrenOf(actualBlock)) {
            try {
                updateActualBlock(childBlock);
                boolean childResult = stepOver().get();
                overallResult &= childResult;
            } catch (InterruptedException e) {
                throw new IllegalStateException(strand + " child execution interrupted", e);
            } catch (ExecutionException e) {
                throw new IllegalStateException(strand + " child execution threw exception", e);
            }
        }
//        moveNext();
        return CompletableFuture.completedFuture(overallResult);
    }

    private synchronized CompletableFuture<Boolean> skip() {
        moveNext();
        return CompletableFuture.completedFuture(true);
    }

    private synchronized CompletableFuture<Boolean> stepInto() {
        if (structure.isLeaf(actualBlock)) {
            LOGGER.debug("[{}] {} is a leaf, stepping into is not allowed", strand, actualBlock);
            return CompletableFuture.completedFuture(false);
        }

        if (structure.isParallel(actualBlock)) {
            for (Block childBlock : structure.childrenOf(actualBlock)) {
                StrandExecutor childExecutor = createChildStrandExecutor(childBlock);
                childExecutor.instruct(StrandCommand.PAUSE);
            }

            updateState(RunState.PAUSED);
            return CompletableFuture.completedFuture(true);
        }

        moveInto();
        updateState(RunState.PAUSED);
        return CompletableFuture.completedFuture(true);
    }

    private synchronized StrandExecutor createChildStrandExecutor(Block childBlock) {
        Strand childStrand = strandFactory.createChildStrand(strand);
        StrandExecutor childExecutor = new PossiblyBrokenStrandExecutor(childStrand, childBlock, structure.substructure(childBlock), strandFactory, leafExecutor);
        childExecutor.getStateStream().filter(RunState.FINISHED::equals).subscribe(s -> {
            synchronized (this) { /* must use the same lock as public synchronized methods */
                removeChildExecutor(childExecutor);
            }
        });
        addChildExecutor(childExecutor);
        LOGGER.debug("[{}] created child strand {}", strand, childStrand);
        return childExecutor;
    }

    private synchronized void moveInto() {
        List<Block> children = structure.childrenOf(actualBlock);
        if (children.isEmpty()) {
            throw new IllegalStateException("Cannot move into block " + actualBlock + ", no children!");
        }

        Block firstChild = children.get(0);
        LOGGER.debug("[{}] {} moved into first child {}", strand, actualBlock, firstChild);
        updateActualBlock(firstChild);
    }

    private synchronized void addChildExecutor(StrandExecutor childExecutor) {
        childExecutors = ImmutableList.<StrandExecutor>builder().addAll(childExecutors).add(childExecutor).build();
    }

    private synchronized void removeChildExecutor(StrandExecutor childExecutor) {
        childExecutors = childExecutors.stream().filter(e -> !e.equals(childExecutor)).collect(toImmutableList());
    }

    private synchronized CompletableFuture<Boolean> runLeaf() {
        LOGGER.debug("[{}] executing block {}", strand, actualBlock);
        updateState(RunState.RUNNING);

        return leafExecutor.executeAsync(actualBlock).thenApply(res -> {
            updateState(RunState.PAUSED);
            return res;
        });
    }

    private synchronized void moveNext() {
        Optional<Block> nextBlock = structure.nextBlock(actualBlock);
        if (nextBlock.isPresent()) {
            LOGGER.debug("[{}] {} moved to next block {}", strand, actualBlock, nextBlock.get());
            updateActualBlock(nextBlock.get());
        } else {
            LOGGER.debug("[{}] {} is the last block. Finished", strand, actualBlock);
            updateState(RunState.FINISHED);
            updateActualBlock(null);
        }
    }

    private synchronized void updateActualBlock(Block newBlock) {
        if (newBlock == null) {
            blockSink.onComplete();
        } else {
            blockSink.onNext(newBlock);
        }
        actualBlock = newBlock;
    }

    private synchronized void updateState(RunState newState) {
        stateSink.onNext(newState);
        actualState = newState;
    }

    @Override
    public Flux<RunState> getStateStream() {
        return stateStream;
    }

    @Override
    public Flux<Block> getBlockStream() {
        return blockStream;
    }

    @Override
    public Flux<Set<StrandCommand>> getAllowedCommandStream() {
        return null;
    }

    @Override
    public RunState getState() {
        return null;
    }
}
