package io.molr.mole.core.support;

/**
 * Should be very similar to a OngoingMissionRun (probably inherit or delegate from it), however, the return type is already fixed....
 * @param <R> the returntype of the mission....
 */
public class OngoingReturningMissionRun<R> {

    public OngoingReturningMissionRun<R> and() {
        return  this;
    }

    public void forget() {

    }

    public R awaitReturnValue() {
        return null;
    }


    /*
    ... like without knowing the return type ... (See MissionControlSupport) .... Potentially this might inherit from the other ... (or delegate)
     */
}
