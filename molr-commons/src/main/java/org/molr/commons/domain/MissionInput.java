package org.molr.commons.domain;

import java.util.Map;

import static java.util.Collections.emptyMap;

public class MissionInput extends AbstractTypedValueContainer implements In {

    private MissionInput(Map<String, Object> values) {
        super(values);
    }

    public static final MissionInput empty() {
        return from(emptyMap());
    }

    public static final MissionInput from(Map<String, Object> values) {
        return new MissionInput(values);
    }

}
