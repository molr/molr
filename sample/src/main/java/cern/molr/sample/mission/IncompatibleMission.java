package cern.molr.sample.mission;

import cern.molr.commons.RunWithMole;
import cern.molr.sample.mole.IntegerFunctionMole;
import cern.molr.sample.mole.RunnableMole;

import java.util.function.Function;

/**
 * A testing mission, the interface of the mission is not compatible with the defined mole
 * @author yassine
 */
@RunWithMole(RunnableMole.class)
public class IncompatibleMission implements Function<Integer,Integer>{

    @Override
    public Integer apply(Integer v) {
        try {
            method1();
        } catch (InterruptedException e) {
            e.printStackTrace();
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
