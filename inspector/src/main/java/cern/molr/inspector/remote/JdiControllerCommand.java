package cern.molr.inspector.remote;

import cern.molr.inspector.controller.JdiController;

/**
 * Commands which can be issued to the {@link JdiController} remotely.
 */
public enum JdiControllerCommand {
    STEP_FORWARD, TERMINATE, RESUME
}
