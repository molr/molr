package cern.molr.commons.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

/**
 * Generic event triggered by a mission execution.
 *
 * @author yassine-kr
 */
public interface MissionEvent {

    @JsonProperty(access=READ_ONLY)
    default String getString() {return toString();}

}
