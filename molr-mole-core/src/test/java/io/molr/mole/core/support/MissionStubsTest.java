package io.molr.mole.core.support;

import io.molr.mole.core.support.domain.*;
import org.junit.Test;

import java.util.Map;

import static io.molr.commons.domain.Placeholder.*;
import static org.junit.Assert.*;

public class MissionStubsTest {

    private final MissionStubSupport support = null;

    @Test
    public void testVoidStub0() {
        VoidStub0 voidFalcon0 = MissionStubs.stub("land the falcon");
        assertNotNull("void stub should not be null", voidFalcon0);
        assertNotNull("stub mission should not be null", voidFalcon0.getMission());
        assertNotNull("stub return type should not be null", voidFalcon0.getReturnType());
        assertEquals(VoidStub0.class, voidFalcon0.getClass());
        assertEquals(Void.class, voidFalcon0.getReturnType().type());

        Map<String, Object> parametersMap = voidFalcon0.getParametersMap();
        assertNotNull("stub parameters map should not be null", parametersMap);
        assertTrue("stub parameters map should be empty", parametersMap.isEmpty());

        assertEquals(MissionStub0.class, voidFalcon0.returning(Boolean.class).getClass());
        assertEquals(VoidStub1.class, voidFalcon0.withParameters(aDouble("double param")).getClass());
        assertEquals(VoidStub2.class, voidFalcon0.withParameters(aDouble("double param"),
                aString("string param")).getClass());
    }

    @Test
    public void testVoidStub1() {
        VoidStub1 voidFalcon1 = MissionStubs.stub("land the falcon").withParameters(anInteger("int param"));
        assertNotNull("void stub should not be null", voidFalcon1);
        assertNotNull("stub mission should not be null", voidFalcon1.getMission());
        assertNotNull("stub return type should not be null", voidFalcon1.getReturnType());
        assertEquals(VoidStub1.class, voidFalcon1.getClass());
        assertEquals(Void.class, voidFalcon1.getReturnType().type());

        Map<String, Object> parametersMap = voidFalcon1.getParametersMap(10);
        assertNotNull("stub parameters map should not be null", parametersMap);
        assertTrue("stub parameters map should have size 1", parametersMap.size() == 1);

        assertEquals(MissionStub1.class, voidFalcon1.returning(Boolean.class).getClass());
    }

    @Test
    public void testVoidStub2() {
        VoidStub2 voidFalcon2 = MissionStubs.stub("land the falcon")
                .withParameters(anInteger("int param"), aBoolean("bool param"));
        assertNotNull("void stub should not be null", voidFalcon2);
        assertNotNull("stub mission should not be null", voidFalcon2.getMission());
        assertNotNull("stub return type should not be null", voidFalcon2.getReturnType());
        assertEquals(VoidStub2.class, voidFalcon2.getClass());
        assertEquals(Void.class, voidFalcon2.getReturnType().type());

        Map<String, Object> parametersMap = voidFalcon2.getParametersMap(10, false);
        assertNotNull("stub parameters map should not be null", parametersMap);
        assertTrue("stub parameters map should have size 2", parametersMap.size() == 2);

        assertEquals(MissionStub2.class, voidFalcon2.returning(Boolean.class).getClass());
    }

    @Test
    public void testMissionStub0() {
        MissionStub0<Integer> missionFalcon0 = MissionStubs.stub("land the falcon", Integer.class);
        assertNotNull("mission stub should not be null", missionFalcon0);
        assertNotNull("stub mission should not be null", missionFalcon0.getMission());
        assertNotNull("stub return type should not be null", missionFalcon0.getReturnType());
        assertEquals(MissionStub0.class, missionFalcon0.getClass());
        assertEquals(Integer.class, missionFalcon0.getReturnType().type());

        Map<String, Object> parametersMap = missionFalcon0.getParametersMap();
        assertNotNull("stub parameters map should not be null", parametersMap);
        assertTrue("stub parameters map should be empty", parametersMap.isEmpty());

        assertEquals(MissionStub1.class, missionFalcon0.withParameters(aDouble("double param")).getClass());
        assertEquals(MissionStub2.class, missionFalcon0.withParameters(aDouble("double param"),
                aString("string param")).getClass());
    }

    @Test
    public void testMissionStub1() {
        MissionStub1<Integer, Boolean> missionFalcon1 = MissionStubs.stub("land the falcon")
                .withParameters(anInteger("int param")).returning(Boolean.class);
        assertNotNull("mission stub should not be null", missionFalcon1);
        assertNotNull("stub mission should not be null", missionFalcon1.getMission());
        assertNotNull("stub return type should not be null", missionFalcon1.getReturnType());
        assertEquals(MissionStub1.class, missionFalcon1.getClass());
        assertEquals(Boolean.class, missionFalcon1.getReturnType().type());

        Map<String, Object> parametersMap = missionFalcon1.getParametersMap(10);
        assertNotNull("stub parameters map should not be null", parametersMap);
        assertTrue("stub parameters map should have size 1", parametersMap.size() == 1);
    }

    @Test
    public void testMissionStub2() {
        MissionStub2<Integer, Boolean, Boolean> missionFalcon2 = MissionStubs.stub("land the falcon")
                .returning(Boolean.class).withParameters(anInteger("int param"), aBoolean("bool param"));
        assertNotNull("mission stub should not be null", missionFalcon2);
        assertNotNull("stub mission should not be null", missionFalcon2.getMission());
        assertNotNull("stub return type should not be null", missionFalcon2.getReturnType());
        assertEquals(MissionStub2.class, missionFalcon2.getClass());
        assertEquals(Boolean.class, missionFalcon2.getReturnType().type());

        Map<String, Object> parametersMap = missionFalcon2.getParametersMap(10, false);
        assertNotNull("stub parameters map should not be null", parametersMap);
        assertTrue("stub parameters map should have size 2", parametersMap.size() == 2);
    }
}
