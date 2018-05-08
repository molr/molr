package cern.molr.mole.supervisor;

import java.io.Closeable;

/**
 * Controller of an execution of a mole being executed in separate JVM
 *
 * @author yassine
 */
public interface MoleExecutionController extends Closeable {

    void addMoleExecutionListener(MoleExecutionListener listener);

    void removeMoleExecutionListener(MoleExecutionListener listener);

    MoleExecutionResponseCommand start();

    MoleExecutionResponseCommand sendCommand(MoleExecutionCommand command);

    MoleExecutionResponseCommand terminate();
}
