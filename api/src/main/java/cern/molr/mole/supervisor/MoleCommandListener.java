package cern.molr.mole.supervisor;

/**
 * Listener for commands sent by supervisor to JVM
 *
 * @author yassine-kr
 */
public interface MoleCommandListener {

    /**
     * Triggered when a command is sent by supervisor
     */
    void onCommand(MoleExecutionCommand command);

}
