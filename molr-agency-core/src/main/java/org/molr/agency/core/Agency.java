/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.agency.core;

import org.molr.commons.domain.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * This is the central place to interact with molr missions from the client side.
 * <p>
 * It provides methods to query what missions are available, instantiate and run/step through them. It depends on the
 * implementation, if there is network communication behind or not. In general, the agency holds a state. However, all
 * the returned streams shall cache the last values, so that whenever a new client subscribes, it gets an immediate
 * update of the actual value.
 * <p>
 * Note that the full agency is designed in an asynchronous manner!
 */
public interface Agency {

    /**
     * Retrieves a stream of the actual {@link AgencyState}. This state contains information about all the available
     * missions and those which are currently instantiated. The stream shall emit a new item whenever the state changes
     * and shall be long-living, so basically never complete during the lifetime of the agency.
     *
     * @return a stream of states of the agency
     */
    Flux<AgencyState> states();

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

    /**
     * Instructs the mission instance identified by the given handle to execute the given command on the given strand.
     * The allowed commands can be 'guessed' by using the information from {@link #statesFor(MissionHandle)}. However,
     * due to the asynchronous nature of the whole framework, it can never be guaranteed that the command is still
     * allowed when sent. For that reason, non-allowed commands shall be ignored by the implementations of an agency and
     * underlying moles.
     *
     * @param handle  a handle representing the mission instance on which the command shall be executed
     * @param strand  the strand of the mission instance on which the command shall be executed
     * @param command the command to execute
     * @see StrandCommand
     * @see Strand
     */
    void instruct(MissionHandle handle, Strand strand, StrandCommand command);


    /**
     * Retrieves the initial (!) representation of a mission, meaning when the mission is not instantiated/running. This
     * representation contains for example the tree structure of the mission. In many (or even most) cases, this
     * information will be static (so not change over the lifecycle of a mission). So, for specific clients - when they
     * know that the structure does not change - it might be sufficient to use this method only to determine the
     * structure. However, for the general case (imagine for example a mission like a parametrized junit test), it is
     * recommended to use this method only to get the initial representation (when no handle exists yet), and then
     * subscribe to the stream provided {@link #representationsFor(MissionHandle)}, to be notified of any changes in the
     * representation.
     *
     * @param mission the mission for which to retrieve the initial (!) representation
     * @return a mono, emitting the initial representation as soon as it is available (usually instantly).
     */
    Mono<MissionRepresentation> representationOf(Mission mission);

    /**
     * Delivers updates of the representation of the mission. Implementations of the agency (and underlying moles)
     * shall guarantee that the returned stream emits at least once for a newly subscribed client.
     *
     * @param handle the handle of the mission for which to retrieve representation updates
     * @return a stream emitting whenever the representation changes, and at least once on subscription.
     */
    Flux<MissionRepresentation> representationsFor(MissionHandle handle);

    /**
     * Retrieves information on what parameters a mission expects (name, type, optional or mandatory). For interactive
     * clients it is recommended to use this information to help the user to input only valid values.
     *
     * @param mission the mission for which the information shall be queried.
     * @return a stream emitting the information about expected parameters as soon as it is available (usually
     * instantly).
     */
    Mono<MissionParameterDescription> parameterDescriptionOf(Mission mission);

    /**
     * Retrieves a stream emitting new items anytime the state of the mission instance changes. Agency implementations
     * (and underlying moles) shall guarantee that the stream emits at least once on new subscriptions. The mission
     * state contains runtime information of the mission instance (for example, strands, cursor positions, run states of
     * each strand and block).
     *
     * @param handle a handle representing the mission instance for which the states shall be retrieved
     * @return a stream of mission states, emitting each time when the state changes and being completed only at the end
     * of the lifecycle of the mission instance.
     */
    Flux<MissionState> statesFor(MissionHandle handle);

    /**
     * Retrieves a stream of the actual output of one mission instance. The items in the stream represent a 'snapshot'
     * of the actually available output of the whole mission. The returned stream shall emit each time the output
     * changes and at least once on subscription. An output basically contains a map of output values per block.
     *
     * @param handle a handle representing the mission instance for which the output shall be queried.
     * @return a stream of the actually available output for the mission instance.
     */
    Flux<MissionOutput> outputsFor(MissionHandle handle);



    default void instructRoot(MissionHandle handle, StrandCommand command) {

    }


    default Mono<MissionHandle> start(Mission mission, Map<String, Object> missionParameters) {
            Mono<MissionHandle> handle = instantiate(mission, missionParameters);
            handle.subscribe(h -> instructRoot(h, StrandCommand.RESUME));
            return handle;
    }

    default Mono<Result> awaitFinished(MissionHandle handle) {
        return null;
    }

    default Mono<Result> run(Mission mission, Map<String, Object> missionParameters) {
        return start(mission, missionParameters).flatMap(this::awaitFinished);
    }

}
