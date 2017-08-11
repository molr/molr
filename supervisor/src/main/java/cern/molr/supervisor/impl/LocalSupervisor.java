/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.supervisor.impl;

import java.util.Date;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import cern.molr.exception.ModeMismatchException;
import cern.molr.mission.Mission;
import cern.molr.mission.run.RunSession;
import cern.molr.mission.step.StepResult;
import cern.molr.mission.step.StepSession;
import cern.molr.mole.Mole;
import cern.molr.mole.supervisor.MoleSpawner;
import cern.molr.mole.supervisor.MoleSupervisor;
import cern.molr.type.Ack;
import cern.molr.type.either.Either;

/**
 * Sample implementation and usage of the supervisor's interfaces to demonstrate communication
 * 
 * @author nachivpn
 */
public class LocalSupervisor {

    private static final Ack ACK = new Ack();

    public static class LocalSampleMoleSupervisor implements MoleSupervisor{

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
        public CompletableFuture<Ack> cancel() {
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

    }

    /**
     * 
     * Mole spawner impl for run mode
     * @author nachivpn 
     * @param <I>
     * @param <O>
     */
    public static class RunMoleSpawner<I,O> implements MoleSpawner<I,O,O>{

        @Override
        public O spawnMoleRunner(Mission m, I args) throws Exception{
            try {
                String moleClassName = m.getMoleClassName();
                @SuppressWarnings("unchecked")
                Class<Mole<I, O>> moleClass = (Class<Mole<I, O>>) Class.forName(moleClassName);
                Mole<I,O> mole = moleClass.getConstructor().newInstance();
                return mole.run(m, args);
            } catch (ClassCastException e) {
                throw new CompletionException("BAD argument! Wrong type?",e);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }

    }

    /**
     * Mole spawner impl for step mode
     * 
     * @author nachivpn 
     * @param <I>
     * @param <O>
     */
    public static class StepMoleSpawner<I,O> implements MoleSpawner<I,O,StepSession>{
        @Override
        public StepSession spawnMoleRunner(Mission m, I args) throws Exception{
            return null;
        }
    }

    public static class EventualExit extends TimerTask{

        Timer timer = new Timer();

        public void exitIn(long timeInMillis) {
            timer.schedule(this, new Date(System.currentTimeMillis()+timeInMillis));
        }

        @Override
        public void run() {
            System.exit(1);
        }
    }
    
    public static MoleSupervisor getNewMoleSupervisor() {
        return new LocalSampleMoleSupervisor();
    }

}
