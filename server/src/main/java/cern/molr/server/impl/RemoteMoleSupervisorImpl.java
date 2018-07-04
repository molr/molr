package cern.molr.server.impl;

import cern.molr.commons.api.request.MissionCommandRequest;
import cern.molr.commons.api.request.client.ServerInstantiationRequest;
import cern.molr.commons.api.response.CommandResponse;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.api.response.SupervisorState;
import cern.molr.commons.api.web.SimpleSubscriber;
import cern.molr.server.api.MolrServerToSupervisor;
import cern.molr.server.api.RemoteMoleSupervisor;
import cern.molr.server.api.TimeOutStateListener;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Implementation of a Remote Supervisor which is able to return its state using network
 *
 * @author yassine-kr
 */
public class RemoteMoleSupervisorImpl implements RemoteMoleSupervisor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteMoleSupervisorImpl.class);

    private MolrServerToSupervisor client;
    private SupervisorState state = null;
    private HashSet<TimeOutStateListener> listeners = new HashSet<>();

    public RemoteMoleSupervisorImpl(String host, int port) {
        this.client = new MolrServerToSupervisorImpl(host, port);
    }

    /**
     * @param timeOutDuration The maximum duration to wait for receiving the next state, otherwise notify the listeners
     *                        that the state is not available
     */
    public RemoteMoleSupervisorImpl(String host, int port, Duration timeOutDuration, Duration interval) {
        this(host, port);
        client.getSupervisorHeartbeat((int) interval.getSeconds()).subscribe(new SimpleSubscriber<SupervisorState>() {

            private Timer timer = new Timer();
            private TimerTask task;

            @Override
            public void onSubscribe(Subscription subscription) {
                super.onSubscribe(subscription);
                updateTimer();
            }

            @Override
            public void consume(SupervisorState supervisorState) {
                LOGGER.info("receiving new state from the supervisor [{}]", supervisorState);
                state = supervisorState;
                updateTimer();
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }

            private void updateTimer() {
                if (task != null)
                    task.cancel();
                task = new TimerTask() {
                    @Override
                    public void run() {
                        LOGGER.warn("State time out reached [{}]", timeOutDuration);
                        notifyListeners(timeOutDuration);
                        updateTimer();
                    }
                };
                timer.schedule(task, timeOutDuration.toMillis());
            }
        });
    }



    @Override
    public <I> Publisher<MissionEvent> instantiate(ServerInstantiationRequest<I> serverRequest, String
            missionId) {
        return client.instantiate(serverRequest.getMissionName(), missionId, serverRequest.getMissionArguments());
    }

    @Override
    public Publisher<CommandResponse> instruct(MissionCommandRequest commandRequest) {
        return client.instruct("unknown", commandRequest.getMissionId(), commandRequest.getCommand());
    }

    @Override
    public Optional<SupervisorState> getSupervisorState() {
        if (state == null)
            return client.getState();
        else
            return Optional.of(state);
    }

    @Override
    public void addStateAvailabilityListener(TimeOutStateListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners(Duration timeOutDuration) {
        listeners.forEach((listener) -> listener.onStateUnavailable(timeOutDuration));
    }

}
