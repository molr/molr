package cern.molr.server.impl;

import cern.molr.server.api.RemoteMoleSupervisor;
import cern.molr.server.api.SupervisorsManager;
import cern.molr.commons.response.SupervisorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Implementation of a {@link SupervisorsManager} which choose the first found idle appropriate
 * supervisor to execute a mission
 * @author yassine-kr
 */
@Service
public class SupervisorsManagerImpl implements SupervisorsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SupervisorsManagerImpl.class);

    private ConcurrentMap<String, RemoteMoleSupervisor> supervisorsRegistry=new ConcurrentHashMap<>();
    private ConcurrentMap<String, Vector<RemoteMoleSupervisor>> possibleSupervisorsRegistry=new ConcurrentHashMap<>();

    @Override
    public String addSupervisor(RemoteMoleSupervisor supervisor, List<String> missionsAccepted) {
        String id=makeEId();
        supervisorsRegistry.put(id,supervisor);
        missionsAccepted.forEach((missiomName)->{
            possibleSupervisorsRegistry.putIfAbsent(missiomName,new Vector<RemoteMoleSupervisor>());
            possibleSupervisorsRegistry.get(missiomName).add(supervisor);
        });
        LOGGER.info("A Supervisor Server registred to MolR server id {}",id);
        return id;
    }

    @Override
    public void removeSupervisor(String id) {
        RemoteMoleSupervisor supervisor=supervisorsRegistry.remove(id);
        possibleSupervisorsRegistry.forEach((mis,vec)->{
            vec.remove(supervisor);
        });
        LOGGER.info("Supervisor Server {} unregistred from MolR server",id);
    }

    @Override
    public void removeSupervisor(RemoteMoleSupervisor supervisor) {
        supervisorsRegistry.entrySet().stream().filter((e)-> e.getValue()==supervisor).findFirst().ifPresent((e)->{
            removeSupervisor(e.getKey());
        });
    }

    @Override
    public Optional<RemoteMoleSupervisor> chooseSupervisor(String missionContentClassName) {

        Optional<Vector<RemoteMoleSupervisor>> optional=Optional
                .ofNullable(possibleSupervisorsRegistry.get(missionContentClassName));

        return optional.flatMap((vec)->{
            for(RemoteMoleSupervisor supervisor:vec){
                Optional<SupervisorState> state=supervisor.getSupervisorState();
                if(state.isPresent() && state.get().isAvailable())
                    return Optional.of(supervisor);
            }
            return Optional.empty();
        });

    }

    private String makeEId() {
        return UUID.randomUUID().toString();
    }
}
