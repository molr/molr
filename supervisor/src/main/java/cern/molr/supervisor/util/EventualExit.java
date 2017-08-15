/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.supervisor.util;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Kills the host JVM is specified amount of time
 * 
 * @author nachivpn
 */
public class EventualExit extends TimerTask{

    Timer timer = new Timer();

    public void exitIn(long timeInMillis) {
        timer.schedule(this, new Date(System.currentTimeMillis()+timeInMillis));
    }

    @Override
    public void run() {
        System.exit(1);
    }
}