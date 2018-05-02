package cern.molr.server;

import cern.molr.mole.supervisor.MoleSupervisor;
import cern.molr.mole.supervisor.MoleSupervisorNew;

/**
 * It represents a supervisor which can tell whether it is idle or not
 * TODO remove "New" from class name
 *
 * @author yassine
 */
public interface StatefulMoleSupervisorNew extends MoleSupervisorNew {
    boolean isIdle();
}
