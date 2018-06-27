package cern.molr.sample.mole;

import java.util.List;

/**
 * A mission which is executed by a {@link SequenceMole}. A sequence mission is a simple list of tasks which are
 * executed consecutively by the Mole.
 * @author yassine-kr
 */
public interface SequenceMission {

    List<Runnable> getTasks();
}
