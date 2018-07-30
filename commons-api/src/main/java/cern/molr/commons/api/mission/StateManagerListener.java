package cern.molr.commons.api.mission;

/**
 * A listener of {@link StateManager}.
 * It is used to send back the state when it is changed
 */
public interface StateManagerListener {
    /**
     * Called when the state is changed
     */
    void onStateChanged();
}
