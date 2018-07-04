package cern.molr.server.api;

import java.time.Duration;

/**
 * Listener for managing the heartbeat mechanism
 * @author yassine-kr
 */
public interface TimeOutStateListener {

    /**
     * Called when the Molr server does not receive a state from the supervisor after a fixed duration
     * @param timeOutDuration the duration between the last state receiving and calling this method
     */
    void onStateUnavailable(Duration timeOutDuration);
}
