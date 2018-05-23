package cern.molr.supervisor.remote;

import cern.molr.supervisor.impl.MoleSupervisorImpl;
import org.springframework.stereotype.Service;

/**
 * Spring service which manages a separate JVM
 * @author yassine-kr
 */
@Service
public class RemoteMoleSupervisorService extends MoleSupervisorImpl {
}
