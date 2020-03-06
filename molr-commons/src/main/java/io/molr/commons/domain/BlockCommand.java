package io.molr.commons.domain;

/**
 * Block related commands.
 */
public enum BlockCommand {

    /*
     * set a breakpoint at given blocks
     */
    SET_BREAKPOINT,
    UNSET_BREAKPOINT,
    /*
     * mark a block as being ignored during mission execution.  
     */
    SET_IGNORE,
    UNSET_IGNORE

}
