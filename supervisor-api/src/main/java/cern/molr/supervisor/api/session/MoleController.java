package cern.molr.supervisor.api.session;

import cern.molr.commons.request.MissionCommand;
import cern.molr.commons.response.CommandResponse;

import java.io.Closeable;

/**
 * Controller of an execution of a mole being executed in a Mole runner
 *
 * @author yassine-kr
 */
public interface MoleController extends Closeable {

    void addMoleExecutionListener(EventsListener listener);

    void removeMoleExecutionListener(EventsListener listener);

    CommandResponse sendCommand(MissionCommand command);

}
