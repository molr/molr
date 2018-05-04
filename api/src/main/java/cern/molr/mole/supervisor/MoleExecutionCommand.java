package cern.molr.mole.supervisor;

/**
 * Generic command send by supervisor to JVM in which the mole is being executed
 * @author yassine
 */
public interface MoleExecutionCommand {
    /**
     * @return id of the mission concerned by the command
     */
    String getMissionId();

    void setMissionId(String missionId);
}
