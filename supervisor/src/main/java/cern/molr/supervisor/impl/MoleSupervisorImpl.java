/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.supervisor.impl;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import cern.molr.exception.ModeMismatchException;
import cern.molr.mission.Mission;
import cern.molr.mission.run.RunSession;
import cern.molr.mission.step.StepResult;
import cern.molr.mission.step.StepSession;
import cern.molr.mole.spawner.RunMoleSpawner;
import cern.molr.mole.spawner.StepMoleSpawner;
import cern.molr.mole.supervisor.MoleSupervisor;
import cern.molr.supervisor.util.EventualExit;
import cern.molr.type.Ack;
import cern.molr.type.either.Either;

/**
 * {@link MoleSupervisorImpl} is the localized (actual) implementation of {@link MoleSupervisor}
 * 
 * @author nachivpn
 */
public class MoleSupervisorImpl implements MoleSupervisor {

    private static final Ack ACK = new Ack();

    private Optional<RunSession<?>> optionalRunSession = Optional.empty();
    private Optional<StepSession> optionalStepSession = Optional.empty();

    public void setStepSession(StepSession session) {
        this.optionalStepSession = Optional.ofNullable(session);
    }

    public void setRunSession(RunSession<?> runSession) {
        this.optionalRunSession = Optional.ofNullable(runSession);
    }

    public RunSession<?> getRunSession() throws ModeMismatchException {
        return optionalRunSession.orElseThrow(() -> new ModeMismatchException("No running mission"));
    }

    public StepSession getStepSession() throws ModeMismatchException {
        return optionalStepSession.orElseThrow(() -> new ModeMismatchException("No stepping mission"));
    }

    @Override
    public <I, O> CompletableFuture<O> run(Mission m, I args, String missionExecutionId) {
        RunSession<O> runSession = () -> CompletableFuture.supplyAsync(() ->{
            try {
                return new RunMoleSpawner<I,O>().spawnMoleRunner(m, args);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
        setRunSession(runSession);
        return runSession.getResult();
    }

    @Override
    public <I, O> CompletableFuture<Either<StepResult, O>> step(Mission m, I args, String missionExecutionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                StepSession ss = new StepMoleSpawner<I,O>().spawnMoleRunner(m, args);
                setStepSession(ss);
                return ss.getController().stepForward();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public <I, O> CompletableFuture<O> resume(Mission m, I args, String missionExecutionId) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        StepSession ss = getStepSession();
                        return ss.getController().resume();
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
                });
    }

    @Override
    public CompletableFuture<Ack> cancel(String missionExecutionId) {
        return CompletableFuture.supplyAsync(
                () -> {
                    return optionalStepSession.map(stepSession -> {
                        stepSession.getController().terminate();
                        return ACK;
                    }).orElseGet(()-> optionalRunSession
                            .map(runSession ->  {
                                new EventualExit().exitIn(30000);
                                return ACK;
                            })
                            .orElseThrow(() -> new CompletionException(new ModeMismatchException("No running or stepping mission"))));
                });
    }

    public static MoleSupervisor getNewMoleSupervisor() {
        return new MoleSupervisorImpl();
    }

}
