/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

import cern.molr.commons.RunWithMole;
import cern.molr.sample.mole.IntegerFunctionMole;

import java.util.function.Function;

@RunWithMole(IntegerFunctionMole.class)
public class MissionTest implements Function<Integer,Integer>{

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
        //System.out.println("method1");
        Thread.sleep(2000);
        method2();
    }

    private void method2() throws InterruptedException {
        //System.out.println("method2");
        Thread.sleep(2000);
        method3();
    }

    private void method3() throws InterruptedException {
        //System.out.println("method3");
        Thread.sleep(2000);
        method4();
    }

    private void method4() throws InterruptedException {
        //System.out.println("method4");
        Thread.sleep(2000);
    }


}
