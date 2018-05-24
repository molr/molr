package cern.molr.mole.spawner.debug;

import cern.molr.mole.supervisor.MoleExecutionEvent;

/**
 * Events sent by JVM while debugging
 * @author yassine-kr
 */
public abstract class DebugEvents {

    public static class LocationChanged implements MoleExecutionEvent{
        private final String className;
        private final String methodName;
        private final int position;

        public LocationChanged(String className, String methodName, int position) {
            this.className = className;
            this.methodName = methodName;
            this.position = position;
        }

        public String getClassName() {
            return className;
        }

        public String getMethodName() {
            return methodName;
        }

        public int getPosition() {
            return position;
        }

        @Override
        public String toString(){
            return "Location changed event: "+className+"."+methodName+":"+position;
        }
    }

    public static class InspectionEnded implements MoleExecutionEvent{
        private final String className;
        private final String methodName;
        private final int position;

        public InspectionEnded(String className, String methodName, int position) {
            this.className = className;
            this.methodName = methodName;
            this.position = position;
        }

        public String getClassName() {
            return className;
        }

        public String getMethodName() {
            return methodName;
        }

        public int getPosition() {
            return position;
        }

        @Override
        public String toString(){
            return "Inspection ended event: "+className+"."+methodName+":"+position;
        }

    }

    public static class VmDeath implements MoleExecutionEvent{
        @Override
        public String toString(){
            return "VM Death event";
        }
    }
}
