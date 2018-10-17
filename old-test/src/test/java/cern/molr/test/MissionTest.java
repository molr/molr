package cern.molr.test;

import cern.molr.commons.api.mission.RunWithMole;
import cern.molr.sample.mole.IntegerFunctionMole;

import java.util.function.Function;

/**
 * A mission test
 *
 * @author yassine-kr
 */
@RunWithMole(IntegerFunctionMole.class)
public class MissionTest implements Function<Integer, Integer> {

    @Override
    public Integer apply(Integer v) {
        try {
            method1();
        } catch (InterruptedException error) {
            error.printStackTrace();
        }
        return v * 2;
    }

    private void method1() throws InterruptedException {
        Thread.sleep(2000);
        method2();
    }

    private void method2() throws InterruptedException {
        Thread.sleep(2000);
        method3();
    }

    private void method3() throws InterruptedException {
        Thread.sleep(2000);
        method4();
    }

    private void method4() throws InterruptedException {
        Thread.sleep(2000);
    }


}
