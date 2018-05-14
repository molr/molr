package cern.molr.supervisor.impl;

import cern.molr.mole.supervisor.MoleSession;
import cern.molr.mole.supervisor.SupervisorSessionsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Implementation of a supervisor sessions manager
 * @author yassine
 */
public class SupervisorSessionsManagerImpl implements SupervisorSessionsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SupervisorSessionsManagerImpl.class);

    private ConcurrentMap<String,MoleSession> sessionsRegistry=new ConcurrentHashMap<>();

    @Override
    public void addSession(String missionId,MoleSession session) {
        LOGGER.info("Adding a session to supervisor: mission id {}",missionId);
        sessionsRegistry.put(missionId,session);
    }

    @Override
    public Optional<MoleSession> getSession(String missionId) {
        LOGGER.info("Getting a session from sessions manager: mission id {}",missionId);
        return Optional.of(sessionsRegistry.get(missionId));
    }

    @Override
    public void removeSession(String missionId) {
        LOGGER.info("Removing a session from sessions manager: mission id {}",missionId);
        sessionsRegistry.remove(missionId);
    }

    @Override
    public void removeSession(MoleSession session) {
        sessionsRegistry.entrySet().stream().filter((s)->s.getValue()==session).findFirst().ifPresent((s)->removeSession(s.getKey()));
    }

    @Override
    public int getSessionsNumber() {
        return sessionsRegistry.size();
    }
}
