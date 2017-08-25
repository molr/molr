/*
 * © Copyright 2016 CERN. This software is distributed under the terms of the Apache License Version 2.0, copied
 * verbatim in the file “COPYING“.ing this licence, CERN does not waive the privileges and immunities granted
 * to it by virtue of its status as an Intergovernmental Organization or submit itself to any jurisdiction.
 */

package cern.molr.inspector.json;

import java.io.IOException;
import java.util.Objects;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import cern.molr.commons.MissionImpl;
import cern.molr.mission.Mission;

/**
 * A GSON serialiser and de-serialiser for {@link Mission} objects.
 */
public class MissionTypeAdapter extends TypeAdapter<Mission> {

    @Override
    public void write(JsonWriter out, Mission mission) throws IOException {
        Objects.requireNonNull(mission.getMoleClassName(), "Agent name cannot be null");
        Objects.requireNonNull(mission.getMissionDefnClassName(), "Class name cannot be null");
        out.beginObject()
                .name("agentName")
                .value(mission.getMoleClassName())
                .name("className")
                .value(mission.getMissionDefnClassName())
                .endObject();
    }

    @Override
    public Mission read(JsonReader in) throws IOException {
        in.beginObject();
        in.nextName();
        final String agentName = in.nextString();
        in.nextName();
        final String className = in.nextString();
        in.endObject();

        return new MissionImpl(agentName, className);
    }
}