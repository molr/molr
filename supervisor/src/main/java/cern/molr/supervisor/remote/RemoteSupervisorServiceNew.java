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
        //simple implementation, would be changed when supervisor will be able to manage many sessions
        State state=Optional.ofNullable(session).map((session)->new State(false,1)).orElse(new State(true,0));
        return Optional.of(state);
    }
}
