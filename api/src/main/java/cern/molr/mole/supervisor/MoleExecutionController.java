package cern.molr.mole.supervisor;

/**
 * Controller of an execution of a mole being executed in a separate JVM
 *
 * @author yassine
 */
public interface MoleExecutionController {

    void addMoleExecutionListener(MoleExecutionListener listener);

    void removeMoleExecutionListener(MoleExecutionListener listener);

    MoleExecutionCommandResponse start();

    MoleExecutionCommandResponse sendCommand(MoleExecutionCommand command);

    MoleExecutionCommandResponse terminate();
}
