/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {@link Ack} is often used as an acknowledgement return type ( instead of void :) )
 *
 * @author nachivpn
 * @author yassine-kr
 */
public final class Ack {

    private final String message;

    public Ack(@JsonProperty("message") String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Ack ack = (Ack) o;

        return !(message != null ? !message.equals(ack.message) : ack.message != null);

    }

    @Override
    public int hashCode() {
        return message != null ? message.hashCode() : 0;
    }

}
