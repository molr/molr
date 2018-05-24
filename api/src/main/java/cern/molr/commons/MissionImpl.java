/*
 * © Copyright 2016 CERN. This software is distributed under the terms of the Apache License Version 2.0, copied
 * verbatim in the file “COPYING“. In applying this licence, CERN does not waive the privileges and immunities granted
 * to it by virtue of its status as an Intergovernmental Organization or submit itself to any jurisdiction.
 */
package cern.molr.commons;

import cern.molr.mission.Mission;


/**
 * Simple implementation of {@link Mission}
 *
 * @author tiagomr
 * @author mgalilee
 * @author yassine-kr-kr
 *
 */
public class MissionImpl implements Mission {

    /**
     * Name of the mole that can execute the {@link Mission}
     */
    private String moleClassName;

    /**
     * Class used by the exposed {@link Mission}
     */
    private String missionContentClassName;

    public MissionImpl() {
    }

    public MissionImpl(String moleClassName, String missionContentClassName) {
        this.moleClassName = moleClassName;
        this.missionContentClassName = missionContentClassName;
    }

    @Override
    public String getMoleClassName() {
        return moleClassName;
    }

    @Override
    public String getMissionDefnClassName() {
        return missionContentClassName;
    }

    public void setMoleClassName(String moleClassName) {
        this.moleClassName = moleClassName;
    }

    public void setMissionDefnClassName(String missionContentClassName) {
        this.missionContentClassName = missionContentClassName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MissionImpl service = (MissionImpl) o;
        if (moleClassName != null ? !moleClassName.equals(service.moleClassName) : service.moleClassName != null)
            return false;
        return !(missionContentClassName != null ? !missionContentClassName.equals(service.missionContentClassName) :
                service.missionContentClassName != null);

    }

    @Override
    public int hashCode() {
        int result = moleClassName != null ? moleClassName.hashCode() : 0;
        result = 31 * result + (missionContentClassName != null ? missionContentClassName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return getMoleClassName() + ": " + getMissionDefnClassName();
    }
}