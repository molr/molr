package cern.molr.mole.supervisor;

/**
 * Generic command send by supervisor to JVM in which the mole is being executed
 * @author yassine
 */
public interface MoleExecutionCommand {
    /**
     * @return id of the mission concerned by the command
     */
    String getId();

    void setId(String id);
}
