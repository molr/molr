package cern.molr.server.api;

import cern.molr.commons.api.response.SupervisorState;

/**
 * Listener notified when a supervisor state is received from the remote supervisor
 *
 * @author yassine-kr
 */
public interface SupervisorStateListener {

    /**
     * Called when a new state is received
     */
    void onNewSupervisorState(SupervisorState state);
}
