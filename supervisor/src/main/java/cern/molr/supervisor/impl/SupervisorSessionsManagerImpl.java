package cern.molr.supervisor.impl;

import cern.molr.mole.supervisor.MoleSession;
import cern.molr.mole.supervisor.SupervisorSessionsManager;
import cern.molr.mole.supervisor.SupervisorSessionsManagerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Implementation of a supervisor sessions manager
 * @author yassine-kr
 */
public class SupervisorSessionsManagerImpl implements SupervisorSessionsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SupervisorSessionsManagerImpl.class);
    private final Set<SupervisorSessionsManagerListener> listeners=new HashSet<>();

    private ConcurrentMap<String,MoleSession> sessionsRegistry=new ConcurrentHashMap<>();

    @Override
    public void addSession(String missionId,MoleSession session) {
        LOGGER.info("Adding a session to supervisor: mission id {}",missionId);
        sessionsRegistry.put(missionId,session);
        notifyListenersAdd(missionId);
    }

    @Override
    public Optional<MoleSession> getSession(String missionId) {
        LOGGER.info("Getting a session from sessions manager: mission id {}",missionId);
        return Optional.ofNullable(sessionsRegistry.get(missionId));
    }

    @Override
    public void removeSession(String missionId) {
        LOGGER.info("Removing a session from sessions manager: mission id {}",missionId);
        sessionsRegistry.remove(missionId);
        notifyListenersRemove(missionId);
    }

    @Override
    public void removeSession(MoleSession session) {
        sessionsRegistry.entrySet().stream().filter((s)->s.getValue()==session)
                .findFirst().ifPresent((s)->removeSession(s.getKey()));
    }

    @Override
    public int getSessionsNumber() {
        return sessionsRegistry.size();
    }

    @Override
    public void addListener(SupervisorSessionsManagerListener listener) {
        listeners.add(listener);
    }

    private void notifyListenersAdd(String missionId){
        listeners.forEach((s)->s.onSessionAdded(missionId));
    }

    private void notifyListenersRemove(String missionId){
        listeners.forEach((s)->s.onSessionRemoved(missionId));
    }
}
