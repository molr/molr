package cern.molr.supervisor.api.session.runner;

import cern.molr.commons.request.MissionCommand;

/**
 * Listener for commands sent by supervisor to the MoleRunner
 *
 * @author yassine-kr
 */
public interface CommandListener {

    /**
     * Triggered when a command is sent by supervisor
     */
    void onCommand(MissionCommand command);

}
