/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.mole.spawner.run.jvm;

/**
 * Input argument type for {@link MoleRunner}
 * Should be serialized and passed as arg[0]
 * @author nachivpn
 * @author yassine-kr
 */
public class MoleRunnerArgument {

    private String missionObjString;
    private String missionInputObjString;
    private String missionInputClassName;

    public MoleRunnerArgument() {
    }

    public MoleRunnerArgument(String missionObjString,
                              String missionInputObjString, String missionInputClassName) {
        this.setMissionObjString(missionObjString);
        this.setMissionInputObjString(missionInputObjString);
        this.setMissionInputClassName(missionInputClassName);
    }
    
    public String getMissionObjString() {
        return missionObjString;
    }
    public void setMissionObjString(String missionObjString) {
        this.missionObjString = missionObjString;
    }
    public String getMissionInputObjString() {
        return missionInputObjString;
    }
    public void setMissionInputObjString(String missionInputObjString) {
        this.missionInputObjString = missionInputObjString;
    }
    public String getMissionInputClassName() {
        return missionInputClassName;
    }
    public void setMissionInputClassName(String missionInputClassName) {
        this.missionInputClassName = missionInputClassName;
    }
    
}
