package cern.molr.server.supervisor;

import cern.molr.server.StatefulMoleSupervisor;
import cern.molr.server.SupervisorsManager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Implemntation of a SupervisorsManager wchich choose the first idle appropriate supervisor found to run a mission
 *
 * @author yassine
 */
public class SupervisorsManagerImpl implements SupervisorsManager {


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
        return id;
    }

    @Override
    public void removeSupervisor(String id) {
        StatefulMoleSupervisor supervisor=supervisorsRegistry.remove(id);
        possibleSupervisorsRegistry.forEach((mis,vec)->{
            vec.remove(supervisor);
        });
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
