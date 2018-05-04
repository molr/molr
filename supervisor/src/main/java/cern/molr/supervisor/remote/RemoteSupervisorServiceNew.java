package cern.molr.supervisor.remote;

import cern.molr.server.StatefulMoleSupervisorNew;
import cern.molr.supervisor.impl.MoleSupervisorImplNew;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Spring service which manages separate JVM.
 * It is stateful, it can tells whether is idle or not
 * @author yassine
 */
@Service
public class RemoteSupervisorServiceNew extends MoleSupervisorImplNew implements StatefulMoleSupervisorNew {

    @Override
    public Optional<State> getState() {
        //State example
        //TODO compute availability status using a specific algorithm
        State state=sessionsManager.getSessionsNumber()==0?new State(true,0):new State(false,sessionsManager.getSessionsNumber());
        return Optional.of(state);
    }
}
