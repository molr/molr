package cern.molr.api.supervisor;

import cern.molr.api.supervisor.SupervisorSessionsManager;

/**
 * A listener notified by a {@link SupervisorSessionsManager}
 * @author yassine-kr
 */
public interface SupervisorSessionsManagerListener {
    void onSessionAdded(String missionId);
    void onSessionRemoved(String missionId);

}
