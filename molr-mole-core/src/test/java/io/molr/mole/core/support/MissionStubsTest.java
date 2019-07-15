package io.molr.mole.core.support;

import io.molr.mole.core.support.domain.*;
import org.junit.Test;

import java.util.Map;

import static io.molr.commons.domain.Placeholder.*;
import static org.junit.Assert.*;

public class MissionStubsTest {

    @Test
    public void testVoidStub0() {
        VoidStub0 voidFalcon0 = MissionStubs.stub("land the falcon");
        assertNotNull("void stub should not be null", voidFalcon0);
        assertNotNull("stub mission should not be null", voidFalcon0.mission());
        assertNotNull("stub return type should not be null", voidFalcon0.returnType());
        assertEquals(VoidStub0.class, voidFalcon0.getClass());
        assertEquals(Void.class, voidFalcon0.returnType());

        Map<String, Object> parametersMap = voidFalcon0.parameters();
        assertNotNull("stub parameters map should not be null", parametersMap);
        assertTrue("stub parameters map should be empty", parametersMap.isEmpty());

        assertEquals(MissionStub0.class, voidFalcon0.returning(Boolean.class).getClass());
        assertEquals(VoidStub1.class, voidFalcon0.withParameters(aDouble("double param")).getClass());
        assertEquals(VoidStub2.class, voidFalcon0.withParameters(aDouble("double param"),
                aString("string param")).getClass());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testVoidStub1() {
        VoidStub1 voidFalcon1 = MissionStubs.stub("land the falcon").withParameters(anInteger("int param"));
        assertNotNull("void stub should not be null", voidFalcon1);
        assertNotNull("stub mission should not be null", voidFalcon1.mission());
        assertNotNull("stub return type should not be null", voidFalcon1.returnType());
        assertEquals(VoidStub1.class, voidFalcon1.getClass());
        assertEquals(Void.class, voidFalcon1.returnType());

        Map<String, Object> parametersMap = voidFalcon1.parameters(10);
        assertNotNull("stub parameters map should not be null", parametersMap);
        assertEquals("stub parameters map should have size 1", 1, parametersMap.size());

        assertEquals(MissionStub1.class, voidFalcon1.returning(Boolean.class).getClass());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testVoidStub2() {
        VoidStub2 voidFalcon2 = MissionStubs.stub("land the falcon")
                .withParameters(anInteger("int param"), aBoolean("bool param"));
        assertNotNull("void stub should not be null", voidFalcon2);
        assertNotNull("stub mission should not be null", voidFalcon2.mission());
        assertNotNull("stub return type should not be null", voidFalcon2.returnType());
        assertEquals(VoidStub2.class, voidFalcon2.getClass());
        assertEquals(Void.class, voidFalcon2.returnType());

        Map<String, Object> parametersMap = voidFalcon2.parameters(10, false);
        assertNotNull("stub parameters map should not be null", parametersMap);
        assertEquals("stub parameters map should have size 2", 2, parametersMap.size());

        assertEquals(MissionStub2.class, voidFalcon2.returning(Boolean.class).getClass());
    }

    @Test
    public void testMissionStub0() {
        MissionStub0<Integer> missionFalcon0 = MissionStubs.stub("land the falcon", Integer.class);
        assertNotNull("mission stub should not be null", missionFalcon0);
        assertNotNull("stub mission should not be null", missionFalcon0.mission());
        assertNotNull("stub return type should not be null", missionFalcon0.returnType());
        assertEquals(MissionStub0.class, missionFalcon0.getClass());
        assertEquals(Integer.class, missionFalcon0.returnType());

        Map<String, Object> parametersMap = missionFalcon0.parameters();
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
        assertNotNull("stub mission should not be null", missionFalcon1.mission());
        assertNotNull("stub return type should not be null", missionFalcon1.returnType());
        assertEquals(MissionStub1.class, missionFalcon1.getClass());
        assertEquals(Boolean.class, missionFalcon1.returnType());

        Map<String, Object> parametersMap = missionFalcon1.parameters(10);
        assertNotNull("stub parameters map should not be null", parametersMap);
        assertEquals("stub parameters map should have size 1", 1, parametersMap.size());
    }

    @Test
    public void testMissionStub2() {
        MissionStub2<Integer, Boolean, Boolean> missionFalcon2 = MissionStubs.stub("land the falcon")
                .returning(Boolean.class).withParameters(anInteger("int param"), aBoolean("bool param"));
        assertNotNull("mission stub should not be null", missionFalcon2);
        assertNotNull("stub mission should not be null", missionFalcon2.mission());
        assertNotNull("stub return type should not be null", missionFalcon2.returnType());
        assertEquals(MissionStub2.class, missionFalcon2.getClass());
        assertEquals(Boolean.class, missionFalcon2.returnType());

        Map<String, Object> parametersMap = missionFalcon2.parameters(10, false);
        assertNotNull("stub parameters map should not be null", parametersMap);
        assertEquals("stub parameters map should have size 2", 2, parametersMap.size());
    }
}
