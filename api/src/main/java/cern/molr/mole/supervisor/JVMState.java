package cern.molr.mole.supervisor;

import cern.molr.exception.CommandNotAcceptedException;

/**
 * Represents a JVM state.
 * @author yassine
 */
public interface JVMState {
    /**
     * Method which verify whether a command is accepted by the current state
     * @param command
     * @throws Exception if the command is not accepted
     */
    void acceptCommand(MoleExecutionCommand command) throws CommandNotAcceptedException;

    /**
     * Change the current state according to the last accepted command
     */
    void changeState();


}
