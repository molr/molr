package cern.molr.supervisor.api.supervisor;

import cern.molr.supervisor.api.session.MissionSession;

import java.util.Optional;

/**
 * It manages a set of {@link MissionSession}s, each session is identified by the id of the mission running on it
 *
 * @author yassine-kr
 */
public interface SupervisorSessionsManager {
    void addSession(String missionId, MissionSession session);

    Optional<MissionSession> getSession(String missionId);

    void removeSession(String missionId);

    void removeSession(MissionSession session);

    int getSessionsNumber();

    void addListener(SupervisorSessionsManagerListener listener);
}
