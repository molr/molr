/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.sample.mission;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import cern.molr.commons.mission.RunWithMole;
import cern.molr.sample.mole.RunnableMole;

/* 
 * The programmer of a mission only needs to specify the mole type. 
 * Since the programmer need not create/interact with mole objects, 
 * the mole implementation is completely hidden from the programmer.
 */

@RunWithMole(RunnableMole.class)

/*
 * A simple Runnable instance (no molr specific code here)
 */
public class RunnableHelloWriter implements Runnable{

    @Override
    public void run() {
        try (PrintWriter out = new PrintWriter(new FileOutputStream("molr-demo.txt"))){
            out.println("Hello molr!");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
