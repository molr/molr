package cern.molr.server;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * It manages set of mole supervisors identified by different ids. Has a method which chooses an appropriate supervisor to execute a mission
 * TODO remove "New" from class name
 *
 * @author yassine
 */
public interface SupervisorsManagerNew {
    String addSupervisor(StatefulMoleSupervisorNew supervisor, List<String> missionsAccepted);
    void removeSupervisor(String id);
    void removeSupervisor(StatefulMoleSupervisorNew supervisor);
    Optional<StatefulMoleSupervisorNew> chooseSupervisor(String missionContentClassName);
}
