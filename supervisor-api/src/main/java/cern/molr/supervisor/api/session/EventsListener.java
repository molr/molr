package cern.molr.supervisor.api.session;

import cern.molr.commons.api.response.MissionEvent;

/**
 * Listener for events triggered by a mission execution
 *
 * @author yassine-kr
 */
public interface EventsListener {

    /**
     * Triggered when an event is sent by the MoleRunner (its source can be the mole itself)
     */
    void onEvent(MissionEvent event);

}
