package cern.molr.commons.commands;

import cern.molr.commons.api.request.MissionCommand;

public class Start implements MissionCommand {
    @Override
    public String toString() {
        return "Mission command: Start";
    }
}
