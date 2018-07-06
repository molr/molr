package cern.molr.supervisor.api.session;

import cern.molr.commons.api.request.MissionCommand;
import cern.molr.commons.api.response.CommandResponse;

import java.io.Closeable;

/**
 * Controller of mission execution
 *
 * @author yassine-kr
 */
public interface MoleController extends Closeable {

    void addMoleExecutionListener(EventsListener listener);

    void removeMoleExecutionListener(EventsListener listener);

    CommandResponse sendCommand(MissionCommand command);

}
