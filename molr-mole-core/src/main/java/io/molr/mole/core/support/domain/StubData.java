package io.molr.mole.core.support.domain;

import io.molr.commons.domain.Mission;
import io.molr.commons.domain.Placeholder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

final class StubData {
    private Mission mission;
    private List<Placeholder<?>> parameters = new ArrayList<Placeholder<?>>();
    private Placeholder<?> returnType;

    public static StubData from(Mission mission) {
        StubData stubData = new StubData();
        stubData.mission = requireNonNull(mission, "mission must not be null");
        return stubData;
    }

    public StubData setMission(Mission mission) {
        this.mission = requireNonNull(mission, "mission must not be null");
        return this;
    }

    public StubData setReturnType(Placeholder<?> returnType) {
        this.returnType = requireNonNull(returnType, "return type must not be null");
        return this;
    }

    public StubData addParameter(Placeholder<?> param) {
        parameters.add(requireNonNull(param, "param must not be null"));
        return this;
    }

    public Placeholder<?> getParameter(int index) {
        return parameters.get(index);
    }

    public Mission getMission() {
        return this.mission;
    }

    public Placeholder<?> getReturnType() {
        return returnType;
    }

    public Map<String, Object> getParametersMap(Object... objects) {
        Map<String, Object> params = new HashMap<String, Object>();
        for (int i = 0; i < objects.length; i++) {
            params.put(parameters.get(i).name(), objects[i]);
        }
        return params;
    }
}
