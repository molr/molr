package cern.molr.server.api;

/**
 * Listener for a {@link SupervisorsManager}
 */
public interface SupervisorsManagerListener {

    /**
     * Called when a supervisor is removed by the manager
     *
     * @param supervisorId the supervisor id of the removed supervisor
     */
    void onSupervisorRemoved(String supervisorId);
}
