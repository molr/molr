package cern.molr.mole.supervisor;

import java.io.Closeable;

/**
 * Controller of an execution of a mole being executed in a separate JVM
 *
 * @author yassine-kr
 */
public interface MoleExecutionController extends Closeable {

    void addMoleExecutionListener(MoleExecutionListener listener);

    void removeMoleExecutionListener(MoleExecutionListener listener);

    MoleExecutionCommandResponse sendCommand(MoleExecutionCommand command);

}
