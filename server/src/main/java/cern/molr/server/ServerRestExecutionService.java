/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.server;

import cern.molr.commons.AnnotatedMissionMaterializer;
import cern.molr.commons.MissionImpl;
import cern.molr.exception.MissionExecutionNotAccepted;
import cern.molr.exception.MissionMaterializationException;
import cern.molr.exception.NoAppropriateSupervisorFound;
import cern.molr.exception.UnknownMissionException;
import cern.molr.mission.Mission;
import cern.molr.mission.MissionMaterializer;
import cern.molr.mole.supervisor.*;
import cern.molr.sample.mission.*;
import cern.molr.sample.mole.RunnableMole;
import cern.molr.server.supervisor.StatefulMoleSupervisorProxy;
import cern.molr.mole.supervisor.MissionCommandRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Gateway used for communication between server and supervisors
 * @author yassine-kr
 */
@Service
public class ServerRestExecutionService {

    private ServerState registry = new ServerState();
    private final SupervisorsManager supervisorsManager;


    public ServerRestExecutionService(SupervisorsManager supervisorsManager) {
        //TODO remove this init code after implementing a deployment service
        MissionMaterializer m = new AnnotatedMissionMaterializer();
        try {
            registry.registerNewMission(m.materialize(RunnableHelloWriter.class));
            registry.registerNewMission(m.materialize(IntDoubler.class));
            registry.registerNewMission(m.materialize(Fibonacci.class));
            //Just for testing, normally missions must be verified before deployment
            registry.registerNewMission(new MissionImpl(RunnableMole.class.getName(),IncompatibleMission.class.getName()));
            registry.registerNewMission(m.materialize(RunnableExceptionMission.class));
        } catch (MissionMaterializationException e) {
            throw new RuntimeException(e);
        }
        this.supervisorsManager=supervisorsManager;
    }


    public <I,O> String instantiate(String missionDefnClassName, I args) throws MissionExecutionNotAccepted,NoAppropriateSupervisorFound {
        String missionEId = makeEId();
        Mission mission=getMission(missionDefnClassName);
        Optional<StatefulMoleSupervisor> optional= supervisorsManager.chooseSupervisor(missionDefnClassName);
        return optional.map((supervisor)->{
                Flux<MoleExecutionEvent> flux = supervisor.instantiate(mission, args, missionEId);
                registry.registerNewMissionExecution(missionEId, supervisor, flux);
                return missionEId;
        }).orElseThrow(() -> new NoAppropriateSupervisorFound("No appropriate supervisor found to controller such mission!"));

    }

    private Mission getMission(String mName)throws MissionExecutionNotAccepted{
        return registry.getMission(mName).orElseThrow(() -> new MissionExecutionNotAccepted("Mission not defined in MolR registry"));
    }

    public Flux<MoleExecutionEvent> getFlux(String mEId) throws UnknownMissionException{
        Optional<Flux<MoleExecutionEvent>> optionalFlux = registry.getMissionExecutionFlux(mEId);
        return optionalFlux.orElseThrow(() -> new UnknownMissionException("No such mission running"));
    }

    private String makeEId() {
        return UUID.randomUUID().toString();
    }

    public static class ServerState {

        /* State of the server */
        /**
         * Accepted missions
         */
        private ConcurrentMap<String, Mission> missionRegistry = new ConcurrentHashMap<>();
        private ConcurrentMap<String, Flux<MoleExecutionEvent>> missionExecutionRegistry =  new ConcurrentHashMap<>();
        private ConcurrentMap<String, MoleSupervisor> moleSupervisorRegistry =  new ConcurrentHashMap<>();

        public void registerNewMission(Mission m) {
            missionRegistry.put(m.getMissionDefnClassName(), m);
        }

        public void registerNewMissionExecution(String missionId, MoleSupervisor supervisor, Flux<MoleExecutionEvent> flux) {
            moleSupervisorRegistry.put(missionId, supervisor);
            missionExecutionRegistry.put(missionId, flux);
        }

        public Optional<Mission> getMission(String mName) {
            return Optional.ofNullable(missionRegistry.get(mName));
        }

        public Optional<Flux<MoleExecutionEvent>> getMissionExecutionFlux(String missionEId){
            return Optional.ofNullable(missionExecutionRegistry.get(missionEId));
        }

        public Optional<MoleSupervisor> getMoleSupervisor(String missionExecutionId) {
            return Optional.ofNullable(moleSupervisorRegistry.get(missionExecutionId));
        }

        public void removeMissionExecution(String missionId){
            moleSupervisorRegistry.remove(missionId);
            missionExecutionRegistry.remove(missionId);
        }

    }

    public Mono<MoleExecutionCommandResponse> instruct(MoleExecutionCommand command) throws UnknownMissionException {
        Optional<MoleSupervisor> optionalSupervisor = registry.getMoleSupervisor(command.getMissionId());
        return optionalSupervisor
                .orElseThrow(() -> new UnknownMissionException("No such mission running"))
                .instruct(command);
    }

    public String addSupervisor(String host,int port, List<String> missionsAccepted){
        StatefulMoleSupervisor moleSupervisor = new StatefulMoleSupervisorProxy(host,port);
        return supervisorsManager.addSupervisor(moleSupervisor,missionsAccepted);
    }

    public void removeSupervisor(String id){
        supervisorsManager.removeSupervisor(id);
    }

}
