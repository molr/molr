package cern.molr.mole.supervisor;

/**
 * It manages a set of {@link MoleSession}s, each session is identified by the id of the mission running on it
 * @author yassine
 */
public interface SupervisorSessionsManager {
    void addSession(String missionId,MoleSession session);
    MoleSession getSession(String missionId);
    void removeSession(String missionId);
    void removeSession(MoleSession session);
    int getSessionsNumber();
}
