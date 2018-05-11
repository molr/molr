package cern.molr.mole.supervisor;

/**
 * Generic command send by the supervisor to the JVM in which the mole is being executed
 * TODO separate the command and the request which should contain the mission id and command
 * @author yassine
 */
public interface MoleExecutionCommand {
    /**
     * @return id of the mission concerned by the command
     */
    String getMissionId();

    void setMissionId(String id);
}
