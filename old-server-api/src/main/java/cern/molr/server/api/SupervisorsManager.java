package cern.molr.server.api;

import java.util.List;
import java.util.Optional;

/**
 * It manages a set of supervisors identified by different ids. Has a method which chooses an appropriate
 * supervisor to execute a mission.
 *
 * @author yassine-kr
 */
public interface SupervisorsManager {
    String addSupervisor(RemoteMoleSupervisor supervisor, List<String> missionsAccepted);

    void removeSupervisor(String id);

    void removeSupervisor(RemoteMoleSupervisor supervisor);

    Optional<RemoteMoleSupervisor> chooseSupervisor(String missionName);

    void addListener(SupervisorsManagerListener listener);
}
