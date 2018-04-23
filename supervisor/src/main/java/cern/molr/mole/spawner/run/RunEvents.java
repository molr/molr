package cern.molr.mole.spawner.run;

import cern.molr.mole.supervisor.MoleExecutionCommandStatus;
import cern.molr.mole.supervisor.MoleExecutionEvent;

/**
 * Events sent by JVM while running a mission
 * @author yassine
 */
public abstract class RunEvents {

    public static class MissionStarted implements MoleExecutionEvent{
        private String missionClassName;
        private Object argument;
        private String moleClassName;

        public MissionStarted() {
        }

        public MissionStarted(String missionClassName, Object argument, String moleClassName) {
            this.missionClassName = missionClassName;
            this.argument = argument;
            this.moleClassName = moleClassName;
        }

        public String getMissionClassName() {
            return missionClassName;
        }

        public Object getArgument() {
            return argument;
        }

        public String getMoleClassName() {
            return moleClassName;
        }

    }

    public static class MissionFinished implements MoleExecutionEvent{
        private String missionClassName;
        private Object result;
        private String moleClassName;

        public MissionFinished() {
        }

        public MissionFinished(String missionClassName, Object result, String moleClassName) {
            this.missionClassName = missionClassName;
            this.result = result;
            this.moleClassName = moleClassName;
        }

        public String getMissionClassName() {
            return missionClassName;
        }

        public Object getResult() {
            return result;
        }

        public String getMoleClassName() {
            return moleClassName;
        }
    }

    public static class CommandStatus extends MoleExecutionCommandStatus {
        public CommandStatus() {
        }

        public CommandStatus(boolean accepted, String reason) {
            super(accepted, reason);
        }
    }

    public static class MissionException implements MoleExecutionEvent{
        private Throwable throwable;

        public MissionException() {
        }

        public MissionException(Throwable throwable) {
            this.throwable = throwable;
        }

        public Throwable getThrowable() {
            return throwable;
        }
    }

    public static class JVMInstantiated implements MoleExecutionEvent{

    }

}
