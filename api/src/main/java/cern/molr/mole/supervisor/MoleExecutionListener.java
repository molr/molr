package cern.molr.mole.supervisor;

/**
 * Listener for events triggered by execution of a mole in a separate JVM
 *
 * @author yassine
 */
public interface MoleExecutionListener {

    /**
     * Triggered when an event is sent by JVM (its origin can be the mole itself)
     */
    void onEvent(MoleExecutionEvent event);

}
