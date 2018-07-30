package cern.molr.supervisor.api.session.runner;

import cern.molr.commons.api.request.MissionCommand;

/**
 * Listener for commands sent by the supervisor to the MoleRunner.
 *
 * @author yassine-kr
 */
public interface CommandListener {

    /**
     * Triggered when a command is sent by the supervisor
     */
    void onCommand(MissionCommand command);

}
