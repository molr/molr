package cern.molr.server;

import cern.molr.mole.supervisor.MoleSupervisor;

/**
 * It represent supervisor which can tell whether is idle or not
 *
 * @author yassine
 */
public interface StatefulMoleSupervisor extends MoleSupervisor {
    boolean isIdle();
}
