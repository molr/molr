package cern.molr.supervisor.api.supervisor;

/**
 * A listener notified by a {@link SupervisorSessionsManager}
 * @author yassine-kr
 */
public interface SupervisorSessionsManagerListener {
    void onSessionAdded(String missionId);
    void onSessionRemoved(String missionId);

}
