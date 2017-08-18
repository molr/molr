/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import cern.molr.commons.trye.TryResponse;
import cern.molr.commons.trye.TryResponseDeserializer;
import cern.molr.commons.trye.TryResponseFailure;
import cern.molr.commons.trye.TryResponseSuccess;
import cern.molr.type.Ack;

@JsonDeserialize(using = MissionCancelResponse.MissionCancelResponseDeserializer.class)
public interface MissionCancelResponse extends TryResponse<Ack>{

    public static class MissionCancelResponseDeserializer extends TryResponseDeserializer<MissionCancelResponse>{

        @Override
        public Class<? extends MissionCancelResponse> getSuccessDeserializer() {
            return MissionCancelResponseSuccess.class;
        }

        @Override
        public Class<? extends MissionCancelResponse> getFailureDeserializer() {
            return MissionCancelResponseFailure.class;
        }

    }

    @JsonDeserialize(as = MissionCancelResponseSuccess.class)
    public class MissionCancelResponseSuccess extends TryResponseSuccess<Ack> implements MissionCancelResponse{
        /**
         * Constructors are needed for this class because "we" create objects of this type in the code
         */
        public MissionCancelResponseSuccess() {
            super(null);
        }

        public MissionCancelResponseSuccess(Ack r) {
            super(r);
        }
    }

    @JsonDeserialize(as = MissionCancelResponseFailure.class)
    public class MissionCancelResponseFailure extends TryResponseFailure<Ack> implements MissionCancelResponse{
        /**
         * Constructors are needed for this class because "we" create objects of this type in the code
         */
        public MissionCancelResponseFailure() {
            super(null);
        }

        public MissionCancelResponseFailure(Throwable l) {
            super(l);
        }
    }

}
