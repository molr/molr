/*
 * © Copyright 2016 CERN. This software is distributed under the terms of the Apache License Version 2.0, copied
 * verbatim in the file “COPYING“. In applying this licence, CERN does not waive the privileges and immunities granted
 * to it by virtue of its status as an Intergovernmental Organization or submit itself to any jurisdiction.
 */
package cern.molr.commons.mission;


/**
 * Simple implementation of {@link Mission}
 *
 * @author tiagomr
 * @author mgalilee
 * @author yassine-kr
 */
public class MissionImpl implements Mission {

    /**
     * Name of the mole that can execute the {@link Mission}
     */
    private String moleClassName;

    /**
     * Class used by the exposed {@link Mission}
     */
    private String missionName;

    public MissionImpl() {
    }

    public MissionImpl(String moleClassName, String missionName) {
        this.moleClassName = moleClassName;
        this.missionName = missionName;
    }

    @Override
    public String getMoleClassName() {
        return moleClassName;
    }

    public void setMoleClassName(String moleClassName) {
        this.moleClassName = moleClassName;
    }

    @Override
    public String getMissionName() {
        return missionName;
    }

    public void setMissionName(String missionName) {
        this.missionName = missionName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MissionImpl mission = (MissionImpl) o;
        if (moleClassName != null ? !moleClassName.equals(mission.moleClassName) : mission.moleClassName != null)
            return false;
        return !(missionName != null ? !missionName.equals(mission.missionName) :
                mission.missionName != null);

    }

    @Override
    public int hashCode() {
        int result = moleClassName != null ? moleClassName.hashCode() : 0;
        result = 31 * result + (missionName != null ? missionName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return getMoleClassName() + ": " + getMissionName();
    }
}