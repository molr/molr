package cern.molr.supervisor.api.session;

import cern.molr.commons.response.MissionEvent;

/**
 * Listener for events triggered by execution of a mole in a Mole runner
 *
 * @author yassine-kr
 */
public interface EventsListener {

    /**
     * Triggered when an event is sent by the mole runner (its origin can be the mole itself)
     */
    void onEvent(MissionEvent event);

}
