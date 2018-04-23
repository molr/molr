package cern.molr.server;

import cern.molr.mole.supervisor.MoleSupervisor;

/**
 * It represents a supervisor which can tell whether it is idle or not
 *
 * @author yassine
 */
public interface StatefulMoleSupervisor extends MoleSupervisor {
    boolean isIdle();
}
