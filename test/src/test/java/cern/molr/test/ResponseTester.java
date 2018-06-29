package cern.molr.test;

import cern.molr.commons.api.response.CommandResponse;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.events.MissionControlEvent;
import org.junit.Assert;

import java.util.List;

/**
 * Class for testing the responses returned while executing a mission
 *
 * @author yassine-kr
 */
public abstract class ResponseTester {

    public static void testInstantiationEvent(MissionEvent actual) {
        Assert.assertEquals(MissionControlEvent.class, actual.getClass());
        Assert.assertEquals(MissionControlEvent.Event.SESSION_INSTANTIATED, ((MissionControlEvent) actual).getEvent());
    }

    public static void testStartedEvent(MissionEvent actual) {
        Assert.assertEquals(MissionControlEvent.class, actual.getClass());
        Assert.assertEquals(MissionControlEvent.Event.MISSION_STARTED, ((MissionControlEvent) actual).getEvent());
    }

    public static void testTerminatedEvent(MissionEvent actual) {
        Assert.assertEquals(MissionControlEvent.class, actual.getClass());
        Assert.assertEquals(MissionControlEvent.Event.SESSION_TERMINATED, ((MissionControlEvent) actual).getEvent());
    }

    public static void testCommandResponseSuccess(CommandResponse actual) {
        Assert.assertEquals(CommandResponse.CommandResponseSuccess.class, actual.getClass());
    }

    public static void testCommandResponseFailure(CommandResponse actual) {
        Assert.assertEquals(CommandResponse.CommandResponseFailure.class, actual.getClass());
    }

    /**
     * Test that events list contains SESSION_INSTANTIATED, MISSION_STARTED and SESSION_TERMINATED and command
     * responses contain two success responses
     *
     * @param events
     * @param commandResponses
     */
    public static void testInstantiateStartTerminate(List<MissionEvent> events, List<CommandResponse>
            commandResponses) {
        Assert.assertEquals(3, events.size());
        testInstantiationEvent(events.get(0));
        testStartedEvent(events.get(1));
        testTerminatedEvent(events.get(2));
        Assert.assertEquals(2, commandResponses.size());
        testCommandResponseSuccess(commandResponses.get(0));
        testCommandResponseSuccess(commandResponses.get(1));
    }
}
