package io.molr.commons.domain;

/**
 * Mission related commands.
 */
public enum MissionCommand {

    /*
     * Dispose a completed mission instance and remove it from mole.
     */
    DISPOSE,
    /*
     * Abort an ongoing mission. Stops execution of all running strands.
     */
    ABORT
    /*
     * Candidates for future releases:
     * KILL: running mission
     */
    
}

