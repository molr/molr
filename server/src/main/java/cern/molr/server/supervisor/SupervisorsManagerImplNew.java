package cern.molr.server.supervisor;

import cern.molr.commons.AnnotatedMissionMaterializer;
import cern.molr.commons.response.SupervisorStateResponse;
import cern.molr.commons.web.MolrWebClient;
import cern.molr.server.StatefulMoleSupervisor;
import cern.molr.server.StatefulMoleSupervisorNew;
import cern.molr.server.SupervisorsManager;
import cern.molr.server.SupervisorsManagerNew;
import cern.molr.supervisor.request.SupervisorStateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

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

        return optional.flatMap((vec)->{
            for(StatefulMoleSupervisorNew supervisor:vec){
                Optional<StatefulMoleSupervisorNew.State> state=supervisor.getState();
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
