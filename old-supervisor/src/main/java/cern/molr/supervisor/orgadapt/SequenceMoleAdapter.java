package cern.molr.supervisor.orgadapt;

import cern.molr.commons.impl.mission.MissionImpl;
import cern.molr.sample.commands.SequenceCommand;
import cern.molr.sample.mole.SequenceMission;
import cern.molr.sample.mole.SequenceMole;
import cern.molr.sample.states.SequenceMissionState;
import org.molr.commons.domain.Block;
import org.molr.commons.domain.ImmutableMissionRepresentation;
import org.molr.commons.domain.Mission;
import org.molr.commons.domain.MissionCommand;
import org.molr.commons.domain.MissionHandle;
import org.molr.commons.domain.MissionRepresentation;
import org.molr.commons.domain.MissionState;
import org.molr.commons.domain.RunState;
import org.molr.commons.domain.Strand;
import org.molr.mole.core.api.Mole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public class SequenceMoleAdapter implements Mole {

    private static final Logger LOGGER = LoggerFactory.getLogger(SequenceMoleAdapter.class);

    /**
     * This can be the same for every mission, as it only has to be unique within an instance
     */
    private static final Strand MAIN_STRAND = Strand.ofId("0");

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final AtomicLong idSeq = new AtomicLong(0);

    private final Map<Mission, SequenceMission> missions;
    private final Map<Mission, MissionRepresentation> representations;

    private final ConcurrentHashMap<MissionHandle, Mission> runningMissions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<MissionHandle, SequenceMole> missionRunners = new ConcurrentHashMap<>();


    public SequenceMoleAdapter(Set<SequenceMission> missions) {
        this.missions = missions.stream().collect(toMap(m -> missionFrom(m), m -> m));
        this.representations = missions.stream().collect(toMap(m -> missionFrom(m), m -> representationFrom(m)));
    }

    @Override
    public Set<Mission> availableMissions() {
        return new HashSet<>(missions.keySet());
    }

    @Override
    public Mono<MissionRepresentation> representationOf(Mission mission) {
        return Mono.just(representations.get(mission));
    }

    @Override
    public void instantiate(MissionHandle handle, Mission mission, Map<String, Object> params) {
        /* XXX as the mole keeps a state, probably the mission (or sequence class) should be in the constructor for cleanness*/
        SequenceMole mole = new SequenceMole();
        missionRunners.put(handle, mole);
        runningMissions.put(handle, mission);

        MissionImpl cernMission = cernMissionFrom(mission);
        executorService.submit(() -> mole.run(cernMission, null));
    }


    @Override
    public Flux<MissionState> statesFor(MissionHandle handle) {
        SequenceMole mex = missionRunners.get(handle);
        if (mex == null) {
            return Flux.error(new IllegalStateException("No states for mission handle " + handle + " are available."));
        }
        return Flux.from(mex.getStatesPublisher()).map(s -> this.orgStateFrom(handle, s)).filter(Optional::isPresent).map(Optional::get);
    }

    @Override
    public void instruct(MissionHandle handle, Strand strand, MissionCommand command) {
        if (!MAIN_STRAND.equals(strand)) {
            throw new IllegalArgumentException("No strand " + strand + " available in this mission.");
        }
        LOGGER.info("Command arrived: handle={}; strand={}; command={}.", handle, strand, command);
        SequenceMole runner = missionRunners.get(handle);
        if (runner == null) {
            throw new IllegalStateException("No runner for handle '" + handle
                    + "' available.");
        }
        Optional<cern.molr.commons.api.request.MissionCommand> cernCommand = cernCommandFrom(command);
        if (cernCommand.isPresent()) {
            runner.sendCommand(cernCommand.get());
        } else {
            LOGGER.warn("Command '" + command + "' could not be translated into a sequence command. Ignoring it.");
        }
    }


    private final Optional<MissionState> orgStateFrom(MissionHandle handle, cern.molr.commons.api.response.MissionState cernState) {
        MissionState.Builder builder = MissionState.builder();
        if (cernState instanceof SequenceMissionState) {
            Mission mission = runningMissions.get(handle);
            if (mission == null) {
                LOGGER.warn("No mission running for handle '" + handle + "'. Cannot translate state.");
                return Optional.empty();
            }
            MissionRepresentation rep = representations.get(mission);

            SequenceMissionState seqState = (SequenceMissionState) cernState;
            int taskNumber = seqState.getTaskNumber();
            Block cursor = null;
            if (taskNumber >= 0) {
                cursor = rep.childrenOf(rep.rootBlock()).get(taskNumber);
            }
            builder.add(MAIN_STRAND, runStateFrom(seqState), cursor, null, allowedCommands(seqState));
        } else {
            LOGGER.warn("Published state for handle '" + handle + "' is not a sequence state but of type '" + cernState.getClass() + "'. Cannot translate state.");
            return Optional.empty();
        }

        return Optional.of(builder.build());
    }

    private final static RunState runStateFrom(SequenceMissionState seqState) {
        SequenceMissionState.State state = seqState.getState();
        switch (state) {
            case WAITING:
                return RunState.PAUSED;
            case TASK_RUNNING:
            case RUNNING_AUTOMATIC:
                return RunState.RUNNING;
            case TASKS_FINISHED:
                return RunState.FINISHED;
        }
        return RunState.UNDEFINED;
    }

    private final static Set<MissionCommand> allowedCommands(SequenceMissionState seqState) {
        return seqState.getPossibleCommands().stream().map(m -> orgCommandFrom(m)).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toSet());
    }

    private static Optional<MissionCommand> orgCommandFrom(cern.molr.commons.api.request.MissionCommand m) {
        if (m instanceof SequenceCommand) {
            SequenceCommand seqCommand = (SequenceCommand) m;
            switch (seqCommand.getCommand()) {
                case STEP:
                    return Optional.of(MissionCommand.STEP_OVER);
                case PAUSE:
                    return Optional.of(MissionCommand.PAUSE);
                case RESUME:
                    return Optional.of(MissionCommand.RESUME);
                case SKIP:
                    return Optional.of(MissionCommand.SKIP);
            }
        }
        return Optional.empty();
    }

    private static final Optional<cern.molr.commons.api.request.MissionCommand> cernCommandFrom(MissionCommand command) {
        return commandFrom(command).map(SequenceCommand::new);
    }

    private static final Optional<SequenceCommand.Command> commandFrom(MissionCommand command) {
        switch (command) {
            case RESUME:
                return Optional.of(SequenceCommand.Command.RESUME);
            case SKIP:
                return Optional.of(SequenceCommand.Command.SKIP);
            case PAUSE:
                return Optional.of(SequenceCommand.Command.PAUSE);
            case STEP_OVER:
                return Optional.of(SequenceCommand.Command.STEP);
        }
        return Optional.empty();
    }

    ;


    private final MissionImpl cernMissionFrom(Mission mission) {
        return new MissionImpl(SequenceMole.class.getName(), missions.get(mission).getClass().getName());
    }

    private static final Mission missionFrom(SequenceMission m) {
        return new Mission(nameOf(m));
    }

    private static String nameOf(SequenceMission m) {
        return m.getClass().getSimpleName();
    }

    private final String id() {
        return Long.toString(idSeq.getAndIncrement());
    }

    private MissionRepresentation representationFrom(SequenceMission sequence) {
        Block rootBlock = Block.idAndText(id(), nameOf(sequence));
        ImmutableMissionRepresentation.Builder builder = ImmutableMissionRepresentation.builder(rootBlock);
        for (SequenceMission.Task task : sequence.getTasks()) {
            builder.parentToChild(rootBlock, Block.idAndText(id(), task.name()));
        }
        return builder.build();
    }

}
