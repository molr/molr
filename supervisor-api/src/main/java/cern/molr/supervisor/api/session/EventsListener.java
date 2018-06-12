package cern.molr.supervisor.api.session;

import cern.molr.commons.api.response.MissionEvent;

/**
 * Listener for events triggered by execution of a mole in a MoleRrunner
 *
 * @author yassine-kr
 */
public interface EventsListener {

    /**
     * Triggered when an event is sent by the MoleRunner (its origin can be the mole itself)
     */
    void onEvent(MissionEvent event);

}
