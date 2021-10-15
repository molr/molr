package io.molr.mole.core.support;

import static io.molr.commons.domain.Placeholder.aBoolean;
import static io.molr.commons.domain.Placeholder.aDouble;
import static io.molr.commons.domain.Placeholder.aString;
import static io.molr.commons.domain.Placeholder.anInteger;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import io.molr.mole.core.support.domain.MissionStub0;
import io.molr.mole.core.support.domain.MissionStub1;
import io.molr.mole.core.support.domain.MissionStub2;
import io.molr.mole.core.support.domain.VoidStub0;
import io.molr.mole.core.support.domain.VoidStub1;
import io.molr.mole.core.support.domain.VoidStub2;

@SuppressWarnings("static-method")
public class MissionStubsTest {

    @Test
    public void testVoidStub0() {
        VoidStub0 voidFalcon0 = MissionStubs.stub("land the falcon");
        assertNotNull("void stub should not be null", voidFalcon0);
        assertThat(voidFalcon0, instanceOf(VoidStub0.class));
        assertNotNull("stub mission should not be null", voidFalcon0.mission());
        assertNotNull("stub return type should not be null", voidFalcon0.returnType());
        assertEquals(Void.class, voidFalcon0.returnType());

        Map<String, Object> parametersMap = voidFalcon0.parameters();
        assertNotNull("stub parameters map should not be null", parametersMap);
        assertTrue("stub parameters map should be empty", parametersMap.isEmpty());

        assertThat(voidFalcon0.returning(Boolean.class), instanceOf(MissionStub0.class));
        assertThat(voidFalcon0.withParameters(aDouble("double param")), instanceOf(VoidStub1.class));
        assertThat(voidFalcon0.withParameters(aDouble("double param"), aString("string param")),
                instanceOf(VoidStub2.class));
    }

    @Test
    public void testVoidStub1() {
        VoidStub1<Integer> voidFalcon1 = MissionStubs.stub("land the falcon").withParameters(anInteger("int param"));
        assertNotNull("void stub should not be null", voidFalcon1);
        assertThat(voidFalcon1, instanceOf(VoidStub1.class));
        assertNotNull("stub mission should not be null", voidFalcon1.mission());
        assertNotNull("stub return type should not be null", voidFalcon1.returnType());
        assertEquals(Void.class, voidFalcon1.returnType());

        Map<String, Object> parametersMap = voidFalcon1.parameters(10);
        assertNotNull("stub parameters map should not be null", parametersMap);
        assertEquals("stub parameters map should have size 1", 1, parametersMap.size());

        assertThat(voidFalcon1.returning(Boolean.class), instanceOf(MissionStub1.class));
    }

    @Test
    public void testVoidStub2() {
        VoidStub2<Integer, Boolean> voidFalcon2 = MissionStubs.stub("land the falcon")
                .withParameters(anInteger("int param"), aBoolean("bool param"));
        assertNotNull("void stub should not be null", voidFalcon2);
        assertThat(voidFalcon2, instanceOf(VoidStub2.class));
        assertNotNull("stub mission should not be null", voidFalcon2.mission());
        assertNotNull("stub return type should not be null", voidFalcon2.returnType());
        assertEquals(Void.class, voidFalcon2.returnType());

        Map<String, Object> parametersMap = voidFalcon2.parameters(10, false);
        assertNotNull("stub parameters map should not be null", parametersMap);
        assertEquals("stub parameters map should have size 2", 2, parametersMap.size());

        assertThat(voidFalcon2.returning(Boolean.class), instanceOf(MissionStub2.class));
    }

    @Test
    public void testMissionStub0() {
        MissionStub0<Integer> missionFalcon0 = MissionStubs.stub("land the falcon", Integer.class);
        assertNotNull("mission stub should not be null", missionFalcon0);
        assertThat(missionFalcon0, instanceOf(MissionStub0.class));
        assertNotNull("stub mission should not be null", missionFalcon0.mission());
        assertNotNull("stub return type should not be null", missionFalcon0.returnType());
        assertEquals(Integer.class, missionFalcon0.returnType());

        Map<String, Object> parametersMap = missionFalcon0.parameters();
        assertNotNull("stub parameters map should not be null", parametersMap);
        assertTrue("stub parameters map should be empty", parametersMap.isEmpty());

        assertThat(missionFalcon0.withParameters(aDouble("double param")), instanceOf(MissionStub1.class));
        assertThat(missionFalcon0.withParameters(aDouble("double param"), aString("string param")),
                instanceOf(MissionStub2.class));
    }

    @Test
    public void testMissionStub1() {
        MissionStub1<Integer, Boolean> missionFalcon1 = MissionStubs.stub("land the falcon")
                .withParameters(anInteger("int param")).returning(Boolean.class);
        assertNotNull("mission stub should not be null", missionFalcon1);
        assertThat(missionFalcon1, instanceOf(MissionStub1.class));
        assertNotNull("stub mission should not be null", missionFalcon1.mission());
        assertNotNull("stub return type should not be null", missionFalcon1.returnType());
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
        assertThat(missionFalcon2, instanceOf(MissionStub2.class));
        assertNotNull("stub mission should not be null", missionFalcon2.mission());
        assertNotNull("stub return type should not be null", missionFalcon2.returnType());
        assertEquals(Boolean.class, missionFalcon2.returnType());

        Map<String, Object> parametersMap = missionFalcon2.parameters(10, false);
        assertNotNull("stub parameters map should not be null", parametersMap);
        assertEquals("stub parameters map should have size 2", 2, parametersMap.size());
    }
}
