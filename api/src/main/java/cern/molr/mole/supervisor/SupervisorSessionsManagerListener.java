package cern.molr.mole.supervisor;

/**
 * A listener notified by a {@link SupervisorSessionsManager}
 * @author yassine
 */
public interface SupervisorSessionsManagerListener {
    void onSessionAdded(String missionId);
    void onSessionRemoved(String missionId);

}
