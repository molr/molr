package cern.molr.server.supervisor;

import cern.molr.mission.Mission;
import cern.molr.mission.step.StepResult;
import cern.molr.server.StatefulMoleSupervisor;
import cern.molr.supervisor.impl.MoleSupervisorProxy;
import cern.molr.type.Ack;
import cern.molr.type.either.Either;
import cern.molr.mole.supervisor.MoleSupervisor;

import java.util.concurrent.CompletableFuture;

/**
 * Implementation of a proxy MoleSupervisor which is able to return whether it is idle or not based on the state of its completableFuture
 * a better solution would be to ask {@link MoleSupervisor server}
 *
 * @author yassine
 */
public class StatefulMoleSupervisorProxy extends MoleSupervisorProxy implements StatefulMoleSupervisor {

    private CompletableFuture<?> completableFuture=null;

    public StatefulMoleSupervisorProxy(String host, int port) {
        super(host, port);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I, O> CompletableFuture<O> run(Mission m, I args, String missionExecutionId) {
        completableFuture=super.run(m,args,missionExecutionId);
        return (CompletableFuture<O>) completableFuture;
    }

    @Override
    public <I, O> CompletableFuture<Either<StepResult, O>> step(Mission m, I args, String missionExecutionId) {
        throw new RuntimeException("resume has not been implemented!");
    }

    @Override
    public <I, O> CompletableFuture<O> resume(Mission m, I args, String missionExecutionId) {
        throw new RuntimeException("resume has not been implemented!");
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<Ack> cancel(String missionExecutionId) {
        completableFuture=super.cancel(missionExecutionId);
        return (CompletableFuture<Ack>) completableFuture;
    }

    @Override
    public boolean isIdle() {
        return completableFuture == null || completableFuture.isDone();
    }

}
