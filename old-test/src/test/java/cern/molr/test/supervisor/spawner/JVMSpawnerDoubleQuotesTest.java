package cern.molr.test.supervisor.spawner;

import cern.molr.supervisor.impl.spawner.JvmSpawnHelper;
import org.junit.Assert;
import org.junit.Test;

public class JVMSpawnerDoubleQuotesTest {

    @Test
    public void escapingIsDoneRight() {
        String aString = "{\"aString\":\"{\\\"anInsideField\\\":\\\"an Inside Value\\\"}\"}";
        String expected = "{\\\"aString\\\":\\\"{\\\\\\\"anInsideField\\\\\\\":\\\\\\\"an Inside Value\\\\\\\"}\\\"}";
        String replaced = JvmSpawnHelper.asWindowsArgument(aString);

        Assert.assertEquals(expected, replaced);
        System.out.println(aString);
        System.out.println(replaced);
    }

}
