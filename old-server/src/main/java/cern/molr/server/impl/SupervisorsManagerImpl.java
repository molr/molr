package cern.molr.server.impl;

import cern.molr.commons.api.response.SupervisorState;
import cern.molr.server.api.RemoteMoleSupervisor;
import cern.molr.server.api.SupervisorsManager;
import cern.molr.server.api.SupervisorsManagerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Implementation of a {@link SupervisorsManager} which chooses the first found idle appropriate
 * supervisor to execute a mission
 *
 * @author yassine-kr
 */
@Service
public class SupervisorsManagerImpl implements SupervisorsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SupervisorsManagerImpl.class);

    private ConcurrentMap<String, RemoteMoleSupervisor> supervisorsRegistry = new ConcurrentHashMap<>();
    private ConcurrentMap<String, Vector<RemoteMoleSupervisor>> possibleSupervisorsRegistry = new ConcurrentHashMap<>();

    private HashSet<SupervisorsManagerListener> managerListeners = new HashSet<>();

    @Override
    public String addSupervisor(RemoteMoleSupervisor supervisor, List<String> missionsAccepted) {
        String id = makeEId();
        supervisorsRegistry.put(id, supervisor);
        missionsAccepted.forEach((missiomName) -> {
            possibleSupervisorsRegistry.putIfAbsent(missiomName, new Vector<RemoteMoleSupervisor>());
            possibleSupervisorsRegistry.get(missiomName).add(supervisor);
        });
        LOGGER.info("A Supervisor Server registered to MolR server id {}", id);
        return id;
    }

    @Override
    public void removeSupervisor(String id) {
        RemoteMoleSupervisor supervisor = supervisorsRegistry.remove(id);
        possibleSupervisorsRegistry.forEach((mis, vec) -> {
            vec.remove(supervisor);
        });
        notifyRemovedSupervisor(id);
        supervisor.close();
        LOGGER.info("Supervisor Server {} unregistered from MolR server", id);
    }

    @Override
    public void removeSupervisor(RemoteMoleSupervisor supervisor) {
        supervisorsRegistry.entrySet().stream().filter((e) -> e.getValue() == supervisor).findFirst().ifPresent((e) -> {
            removeSupervisor(e.getKey());
        });
    }

    //TODO find the reason of a NoSupervisorFoundException thrown sometimes (maybe the config files are not loaded)
    //I think it is caused by the supervisor being not able to load the names of missions that can execute.
    @Override
    public synchronized Optional<RemoteMoleSupervisor> chooseSupervisor(String missionName) {

        Optional<Vector<RemoteMoleSupervisor>> optional = Optional
                .ofNullable(possibleSupervisorsRegistry.get(missionName));

        return optional.flatMap((vec) -> {
            for (int i = 0; i < vec.size(); i++) {
                Optional<SupervisorState> state = vec.get(i).getSupervisorState();
                if (state.isPresent()) {
                    if (state.get().isAvailable()) {
                        return Optional.of(vec.get(i));
                    }
                } else {
                    removeSupervisor(vec.get(i));
                    i--;
                }
            }
            return Optional.empty();
        });

    }

    @Override
    public void addListener(SupervisorsManagerListener listener) {
        managerListeners.add(listener);
    }

    private void notifyRemovedSupervisor(String supervisorId) {
        managerListeners.forEach((listener) -> listener.onSupervisorRemoved(supervisorId));
    }

    private String makeEId() {
        return UUID.randomUUID().toString();
    }
}
