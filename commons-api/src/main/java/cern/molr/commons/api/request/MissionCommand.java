package cern.molr.commons.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

/**
 * Generic command sent by the client to MolR server then forwarded to a supervisor
 *
 * @author yassine-kr
 */
public interface MissionCommand {

    @JsonProperty(access=READ_ONLY)
    default String getString() {return toString();}
}
