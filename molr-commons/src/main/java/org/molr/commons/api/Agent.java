package org.molr.commons.api;

import org.molr.commons.domain.*;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Ongoing work: We are refactoring mole and Agency into a common API. This is temporarily representing this common
 * API.
 */
public interface Agent {

    /**
     * Instantiates the given mission with the given parameters (Currently only string and integer parameters are
     * supported). The returned mono shall only emit, as soon as the mission is available and more detailed states can
     * be retrieved by other methods of the agency, if queried with the given handle. As soon as the mission is
     * instantiated (the returned mono emits), then the mission instance shall be in a paused state with the cursor of
     * the root {@link Strand} on its first (or only) {@link Block}.
     * <p>
     * At this point in time, commands can be sent to the mission instance through the {@link #instruct(MissionHandle,
     * Strand, StrandCommand)} method (non-allowed commands shall be ignored by the agency and the underlying moles).
     * The allowed commands (and other information) can be queried through the {@link #statesFor(MissionHandle)}
     * method.
     *
     * @param mission the mission which shall be instantiated
     * @param params  a map from parameter name to parameter value to instantiate the mission
     * @return a mono of a new handle (unique throughout the lifetime of the agency), representing the instance of the
     * mission and being emitted as soon as the mission instance is ready to receive commands.
     */
    Mono<MissionHandle> instantiate(Mission mission, Map<String, Object> params);
}
