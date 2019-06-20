package io.molr.mole.core.support;

import io.molr.commons.domain.Placeholder;
import io.molr.commons.domain.Placeholders;
import io.molr.mole.core.support.domain.*;
import org.junit.Ignore;
import org.junit.Test;

import static io.molr.commons.domain.Placeholder.aDouble;
import static io.molr.commons.domain.Placeholder.aString;

public class MissionStubsTest {

    private final MissionStubSupport support = null;

    @Ignore("Not yet implemented")
    @Test
    public void someExamples() {

        /*
        Creating stubs...
         */

        /* basic (no params, void returning*/
        VoidStub0 voidFalcon = MissionStubs.stub("land the falcon");

        /* with one parameter and return type*/
        MissionStub1<Double, Boolean> falcon0 = MissionStubs.stub("land the falcon")
                .returning(Boolean.class)
                .withParameters(aDouble("attackAngle"));

        /* also the other order should work */
        MissionStub1<Double, Boolean> falcon1 = MissionStubs.stub("land the falcon")
                .withParameters(aDouble("attackAngle"))
                .returning(Boolean.class);

        /* more parameters ...*/
        MissionStub2<Double, String, Boolean> falcon2 = MissionStubs.stub("land the falcon")
                .withParameters(aDouble("angle"), aString("final message"))
                .returning(Boolean.class);


        /*
            using the stubs
        */

        support.start(voidFalcon).and().forget();

        /* parameters enforced by compiler - the following does not compile*/
        //support.start(falcon2);

        /* A somehow full fledged call*/
        Boolean success = support.start(falcon2, 0.1, "We did it!")
                .and().awaitReturnValue();

    }
}
