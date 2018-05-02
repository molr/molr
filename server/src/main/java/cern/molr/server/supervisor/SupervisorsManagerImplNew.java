package cern.molr.server.supervisor;

import cern.molr.commons.AnnotatedMissionMaterializer;
import cern.molr.server.StatefulMoleSupervisor;
import cern.molr.server.StatefulMoleSupervisorNew;
import cern.molr.server.SupervisorsManager;
import cern.molr.server.SupervisorsManagerNew;
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
 * Implementation of a {@link SupervisorsManager} which choose the first found idle appropriate supervisor to run a mission
 * TODO remove "New" from class name
 * @author yassine
 */
@Service
public class SupervisorsManagerImplNew implements SupervisorsManagerNew {

    private static final Logger LOGGER = LoggerFactory.getLogger(SupervisorsManagerImplNew.class);

    private ConcurrentMap<String, StatefulMoleSupervisorNew> supervisorsRegistry=new ConcurrentHashMap<>();
    private ConcurrentMap<String, Vector<StatefulMoleSupervisorNew>> possibleSupervisorsRegistry=new ConcurrentHashMap<>();

    @Override
    public String addSupervisor(StatefulMoleSupervisorNew supervisor, List<String> missionsAccepted) {
        String id=makeEId();
        supervisorsRegistry.put(id,supervisor);
        missionsAccepted.forEach((missiomName)->{
            possibleSupervisorsRegistry.putIfAbsent(missiomName,new Vector<StatefulMoleSupervisorNew>());
            possibleSupervisorsRegistry.get(missiomName).add(supervisor);
        });
        LOGGER.info("A Supervisor Server registred to MolR server id {}",id);
        return id;
    }

    @Override
    public void removeSupervisor(String id) {
        StatefulMoleSupervisorNew supervisor=supervisorsRegistry.remove(id);
        possibleSupervisorsRegistry.forEach((mis,vec)->{
            vec.remove(supervisor);
        });
        LOGGER.info("Supervisor Server {} unregistred from MolR server",id);
    }

    @Override
    public void removeSupervisor(StatefulMoleSupervisorNew supervisor) {
        supervisorsRegistry.entrySet().stream().filter((e)-> e.getValue()==supervisor).findFirst().ifPresent((e)->{
            removeSupervisor(e.getKey());
        });
    }

    @Override
    public Optional<StatefulMoleSupervisorNew> chooseSupervisor(String missionContentClassName) {
        Optional<Vector<StatefulMoleSupervisorNew>> optional=Optional.ofNullable(possibleSupervisorsRegistry.get(missionContentClassName));
        return optional.map((vec)->vec.stream().filter(StatefulMoleSupervisorNew::isIdle).findFirst().orElse(null));

    }

    private String makeEId() {
        return UUID.randomUUID().toString();
    }
}
