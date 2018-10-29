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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Objects.requireNonNull;

public class StrandExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(StrandExecutor.class);

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

    public StrandExecutor(Strand strand, Block actualBlock, TreeStructure structure, StrandFactory strandFactory, LeafExecutor leafExecutor) {
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

    public synchronized CompletableFuture<Boolean> instruct(StrandCommand command) {
        switch (command) {
            case PAUSE:
                return pause();
            case SKIP:
                return skip();
            case RESUME:
                return resume();
            case STEP_INTO:
                return stepInto();
            case STEP_OVER:
                return stepOver();
        }
        throw new IllegalArgumentException("Command '" + command + "' could not be interpreted.");
    }

    private CompletableFuture<Boolean> resume() {
        return null;
    }

    private CompletableFuture<Boolean> pause() {
        LOGGER.debug("[{}] paused and instructed children to pause", strand);
        updateState(RunState.PAUSED);
        childExecutors.forEach(child -> child.instruct(StrandCommand.PAUSE));
        return CompletableFuture.completedFuture(true);
    }

    private CompletableFuture<Boolean> stepOver() {
        if (structure.isLeaf(actualBlock)) {
            return runLeaf().thenApply(res -> {
                moveNext();
                return res;
            });
        }

        if (structure.isParallel(actualBlock)) {
            updateState(RunState.RUNNING);
            List<Mono<Boolean>> futuresAsMono = structure.childrenOf(actualBlock).stream()
                    .map(this::createChildStrandExecutor)
                    .map(childExecutor -> childExecutor.instruct(StrandCommand.STEP_OVER))
                    .map(Mono::fromFuture)
                    .collect(Collectors.toList());

            return Mono.zip(futuresAsMono, objs -> ArrayUtils.convertArrayTo(objs, Boolean.class))
                    .map((List<Boolean> booleans) -> booleans.stream().reduce(true, Boolean::logicalAnd))
                    .doOnNext(r -> LOGGER.debug("[{}] children RESUME finished with result {}", strand, r))
                    .doOnNext(r -> updateState(RunState.PAUSED))
                    .doOnNext(r -> moveNext())
                    .toFuture();
        }

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

    private CompletableFuture<Boolean> skip() {
        moveNext();
        return CompletableFuture.completedFuture(true);
    }

    private CompletableFuture<Boolean> stepInto() {
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

    private StrandExecutor createChildStrandExecutor(Block childBlock) {
        Strand childStrand = strandFactory.createChildStrand(strand);
        StrandExecutor childExecutor = new StrandExecutor(childStrand, childBlock, structure.substructure(childBlock), strandFactory, leafExecutor);
        childExecutor.getStateStream().filter(RunState.FINISHED::equals).subscribe(s -> {
            synchronized (this) { /* must use the same lock as public synchronized methods */
                removeChildExecutor(childExecutor);
            }
        });
        addChildExecutor(childExecutor);
        LOGGER.debug("[{}] created child strand {}", strand, childStrand);
        return childExecutor;
    }

    private void moveInto() {
        List<Block> children = structure.childrenOf(actualBlock);
        if (children.isEmpty()) {
            throw new IllegalStateException("Cannot move into block " + actualBlock + ", no children!");
        }

        Block firstChild = children.get(0);
        LOGGER.debug("[{}] {} moved into first child {}", strand, actualBlock, firstChild);
        updateActualBlock(firstChild);
    }

    private void addChildExecutor(StrandExecutor childExecutor) {
        childExecutors = ImmutableList.<StrandExecutor>builder().addAll(childExecutors).add(childExecutor).build();
    }

    private void removeChildExecutor(StrandExecutor childExecutor) {
        childExecutors = childExecutors.stream().filter(e -> !e.equals(childExecutor)).collect(toImmutableList());
    }

    private CompletableFuture<Boolean> runLeaf() {
        LOGGER.debug("[{}] executing block {}", strand, actualBlock);
        updateState(RunState.RUNNING);

        return leafExecutor.executeAsync(actualBlock).thenApply(res -> {
            updateState(RunState.PAUSED);
            return res;
        });
    }

    private void moveNext() {
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

    private void updateActualBlock(Block newBlock) {
        if (newBlock == null) {
            blockSink.onComplete();
        } else {
            blockSink.onNext(newBlock);
        }
        actualBlock = newBlock;
    }

    private void updateState(RunState newState) {
        stateSink.onNext(newState);
        actualState = newState;
    }

    public Flux<RunState> getStateStream() {
        return stateStream;
    }

    public Flux<Block> getBlockStream() {
        return blockStream;
    }
}
