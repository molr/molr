/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.server;

import cern.molr.commons.api.exception.ExecutionNotAcceptedException;
import cern.molr.commons.api.exception.NoSupervisorFoundException;
import cern.molr.commons.api.exception.UnknownMissionException;
import cern.molr.commons.api.request.MissionCommandRequest;
import cern.molr.commons.api.request.client.ServerInstantiationRequest;
import cern.molr.commons.api.response.CommandResponse;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.sample.mission.*;
import cern.molr.server.api.RemoteMoleSupervisor;
import cern.molr.server.api.SupervisorsManager;
import cern.molr.server.impl.RemoteMoleSupervisorImpl;
import io.netty.util.internal.ConcurrentSet;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Service used for communication between server and supervisors
 *
 * @author yassine-kr
 */
@Service
public class ServerRestExecutionService {

    private final ServerState registry = new ServerState();
    private final SupervisorsManager supervisorsManager;


    public ServerRestExecutionService(SupervisorsManager supervisorsManager, RegisteredMissions missions) {

        //TODO remove this init code after implementing a deployment service
        //Just for testing, normally missions must be verified before deployment
        missions.getMissions().forEach(registry::registerNewMission);

        this.supervisorsManager = supervisorsManager;
    }


    public <I> String instantiate(ServerInstantiationRequest<I> request)
            throws ExecutionNotAcceptedException, NoSupervisorFoundException {
        String missionEId = makeEId();
        missionExists(request.getMissionName());
        Optional<RemoteMoleSupervisor> optional = supervisorsManager.chooseSupervisor(request.getMissionName());
        return optional.map((supervisor) -> {
            Publisher<MissionEvent> executionEventStream = supervisor.instantiate(request, missionEId);
            registry.registerNewMissionExecution(missionEId, supervisor, executionEventStream);
            return missionEId;
        }).orElseThrow(() ->
                new NoSupervisorFoundException("No appropriate supervisor found to execute such mission!"));

    }

    private void missionExists(String missionName) throws ExecutionNotAcceptedException {
        if (!registry.missionExists(missionName)) {
            throw new ExecutionNotAcceptedException("Mission not defined in MolR registry");
        }
    }

    public Publisher<MissionEvent> getEventsStream(String mEId) throws UnknownMissionException {
        Optional<Publisher<MissionEvent>> optionalStream = registry.getMissionExecutionStream(mEId);
        return optionalStream.orElseThrow(() -> new UnknownMissionException("No such mission running"));
    }

    private String makeEId() {
        return UUID.randomUUID().toString();
    }

    public Publisher<CommandResponse> instruct(MissionCommandRequest commandRequest)
            throws UnknownMissionException {
        Optional<RemoteMoleSupervisor> optionalSupervisor = registry.getMoleSupervisor(commandRequest.getMissionId());
        return optionalSupervisor
                .orElseThrow(() -> new UnknownMissionException("No such mission running"))
                .instruct(commandRequest);
    }

    public String addSupervisor(String host, int port, List<String> missionsAccepted) {
        RemoteMoleSupervisor moleSupervisor = new RemoteMoleSupervisorImpl(host, port);
        return supervisorsManager.addSupervisor(moleSupervisor, missionsAccepted);
    }

    public void removeSupervisor(String id) {
        supervisorsManager.removeSupervisor(id);
    }

    public static class ServerState {

        /**
         * Accepted missions
         */
        private ConcurrentSet<String> missionRegistry = new ConcurrentSet<>();
        private ConcurrentMap<String, Publisher<MissionEvent>> missionExecutionRegistry = new ConcurrentHashMap<>();
        private ConcurrentMap<String, RemoteMoleSupervisor> moleSupervisorRegistry = new ConcurrentHashMap<>();

        public void registerNewMission(String missionName) {
            missionRegistry.add(missionName);
        }

        public void registerNewMissionExecution(String missionId,
                                                RemoteMoleSupervisor supervisor, Publisher<MissionEvent> stream) {
            moleSupervisorRegistry.put(missionId, supervisor);
            missionExecutionRegistry.put(missionId, stream);
        }

        public boolean missionExists(String missionName) {
            return missionRegistry.contains(missionName);
        }

        public Optional<Publisher<MissionEvent>> getMissionExecutionStream(String missionEId) {
            return Optional.ofNullable(missionExecutionRegistry.get(missionEId));
        }

        public Optional<RemoteMoleSupervisor> getMoleSupervisor(String missionId) {
            return Optional.ofNullable(moleSupervisorRegistry.get(missionId));
        }

        public void removeMissionExecution(String missionId) {
            moleSupervisorRegistry.remove(missionId);
            missionExecutionRegistry.remove(missionId);
        }

    }

}
