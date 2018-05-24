package cern.molr.server;

import java.util.List;
import java.util.Optional;

/**
 * It manages a set of mole supervisors identified by different ids. Has a method which chooses an appropriate
 * supervisor to execute a mission
 *
 * @author yassine-kr
 */
public interface SupervisorsManager {
    String addSupervisor(StatefulMoleSupervisor supervisor, List<String> missionsAccepted);
    void removeSupervisor(String id);
    void removeSupervisor(StatefulMoleSupervisor supervisor);
    Optional<StatefulMoleSupervisor> chooseSupervisor(String missionContentClassName);
}
