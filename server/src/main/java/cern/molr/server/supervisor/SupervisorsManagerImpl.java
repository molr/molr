package cern.molr.server.supervisor;

import cern.molr.server.StatefulMoleSupervisor;
import cern.molr.server.SupervisorsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Implementation of a {@link SupervisorsManager} which choose the first found idle appropriate supervisor
 * @author yassine
 */
@Service
public class SupervisorsManagerImpl implements SupervisorsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SupervisorsManagerImpl.class);

    private ConcurrentMap<String, StatefulMoleSupervisor> supervisorsRegistry=new ConcurrentHashMap<>();
    private ConcurrentMap<String, Vector<StatefulMoleSupervisor>> possibleSupervisorsRegistry=new ConcurrentHashMap<>();

    @Override
    public String addSupervisor(StatefulMoleSupervisor supervisor, List<String> missionsAccepted) {
        String id=makeEId();
        supervisorsRegistry.put(id,supervisor);
        missionsAccepted.forEach((missiomName)->{
            possibleSupervisorsRegistry.putIfAbsent(missiomName,new Vector<StatefulMoleSupervisor>());
            possibleSupervisorsRegistry.get(missiomName).add(supervisor);
        });
        LOGGER.info("A Supervisor Server registred to MolR server id {}",id);
        return id;
    }

    @Override
    public void removeSupervisor(String id) {
        StatefulMoleSupervisor supervisor=supervisorsRegistry.remove(id);
        possibleSupervisorsRegistry.forEach((mis,vec)->{
            vec.remove(supervisor);
        });
        LOGGER.info("Supervisor Server {} unregistred from MolR server",id);
    }

    @Override
    public void removeSupervisor(StatefulMoleSupervisor supervisor) {
        supervisorsRegistry.entrySet().stream().filter((e)-> e.getValue()==supervisor).findFirst().ifPresent((e)->{
            removeSupervisor(e.getKey());
        });
    }

    @Override
    public Optional<StatefulMoleSupervisor> chooseSupervisor(String missionContentClassName) {
        Optional<Vector<StatefulMoleSupervisor>> optional=Optional.ofNullable(possibleSupervisorsRegistry.get(missionContentClassName));
        return optional.map((vec)->vec.stream().filter(StatefulMoleSupervisor::isIdle).findFirst().orElse(null));

    }

    private String makeEId() {
        return UUID.randomUUID().toString();
    }
}
