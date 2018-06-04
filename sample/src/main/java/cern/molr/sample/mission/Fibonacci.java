/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.sample.mission;

import cern.molr.commons.mission.RunWithMole;
import cern.molr.sample.mole.IntegerFunctionMole;

import java.util.function.Function;


/*
 * The programmer of a mission only needs to specify the mole type.
 * Since the programmer need not create/interact with mole objects,
 * the mole implementation is completely hidden from the programmer.
 */

@RunWithMole(IntegerFunctionMole.class)

public class Fibonacci implements Function<Integer, Integer> {

    @Override
    public Integer apply(Integer v) {
        return fib(v);
    }

    /**
     * @param v
     *
     * @return
     */
    private Integer fib(Integer v) {
        if (v <= 1)
            return 1;
        else
            return fib(v - 1) + fib(v - 2);
    }


}
