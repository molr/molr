package cern.molr.mole.supervisor;

/**
 * Controller of an execution of a mole being executed on separate JVM
 *
 * @author yassine
 */
public interface MoleExecutionController {

    void addMoleExecutionListener(MoleExecutionListener listener);

    void removeMoleExecutionListener(MoleExecutionListener listener);

    MoleExecutionRequestCommandResult start();

    MoleExecutionRequestCommandResult sendCommand(MoleExecutionCommand command);

    MoleExecutionRequestCommandResult terminate();
}
