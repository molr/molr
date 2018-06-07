/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.response;

/**
 * {@link Ack} is often used as an acknowledgement return type ( instead of void :) )
 *
 * @author nachivpn
 * @author yassine-kr
 */
public final class Ack {

    private final String message;

    public Ack() {
        this.message = "ACK";
    }

    public Ack(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
