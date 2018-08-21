package cern.molr.sample.controllers;

import cern.molr.client.api.ClientControllerData;
import cern.molr.client.impl.StandardController;
import cern.molr.commons.api.response.CommandResponse;
import cern.molr.sample.commands.SequenceCommand;
import org.reactivestreams.Publisher;

/**
 * A custom controller for missions run by the SequenceMole
 *
 * @author yassine-kr
 */
public class SequenceMissionController extends StandardController {

    public SequenceMissionController(ClientControllerData clientControllerData) {
        super(clientControllerData);
    }

    public Publisher<CommandResponse> step() {
        return instruct(new SequenceCommand(SequenceCommand.Command.STEP));
    }

    public Publisher<CommandResponse> skip() {
        return instruct(new SequenceCommand(SequenceCommand.Command.SKIP));
    }

    public Publisher<CommandResponse> resume() {
        return instruct(new SequenceCommand(SequenceCommand.Command.RESUME));
    }

    public Publisher<CommandResponse> pause() {
        return instruct(new SequenceCommand(SequenceCommand.Command.PAUSE));
    }
}
