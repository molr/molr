package cern.molr.mole.spawner.run;

import cern.molr.mole.supervisor.MoleExecutionCommandStatus;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.type.ManuallySerializable;

import java.util.HashMap;
import java.util.Map;

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

    public static class MissionException implements MoleExecutionEvent,ManuallySerializable {
        private Throwable throwable;
        private String message;

        public MissionException(String message) {
            this.message=message;
        }

        public MissionException(Throwable throwable) {
            this.throwable = throwable;
            this.message=throwable.getMessage();
        }

        public Throwable getThrowable() {
            return throwable;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setThrowable(Throwable throwable) {
            this.throwable = throwable;
        }

        @Override
        public String toString(){
            return throwable.getClass().getName()+": "+throwable.getMessage();
        }

        @Override
        public Map<String, String> getJsonMap() {
            Map<String,String> map=new HashMap<>();
            map.put("\"message\"","\""+message+"\"");
            return map;
        }
    }

    public static class JVMInstantiated implements MoleExecutionEvent{

    }

    public static class JVMDestroyed implements MoleExecutionEvent{

    }

}
