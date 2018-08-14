package cern.molr.commons.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

/**
 * Generic event triggered by a mission execution.
 *
 * @author yassine-kr
 */
public interface MissionEvent {

    /**
     * Used for the Web Gui
     * @return the string to be displayed by the web gui
     */
    @JsonProperty(access=READ_ONLY)
    default String getString() {return toString();}

}
