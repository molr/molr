package cern.molr.commons.response;

import cern.molr.commons.trye.TryResponse;
import cern.molr.commons.trye.TryResponseFailure;
import cern.molr.commons.trye.TryResponseSuccess;
import cern.molr.mole.supervisor.MoleExecutionCommandResponse;
import cern.molr.type.Ack;
import cern.molr.type.ManuallySerializable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.HashMap;
import java.util.Map;

public interface CommandResponse extends TryResponse<Ack>,MoleExecutionCommandResponse {

    @JsonDeserialize(as = CommandResponseSuccess.class)
    public static class CommandResponseSuccess extends TryResponseSuccess<Ack> implements CommandResponse {
        /**
         * Constructors are needed for this class because "we" create objects of this type in the code
         */
        public CommandResponseSuccess() {
            super(null);
        }

        public CommandResponseSuccess(Ack r) {
            super(r);
        }
    }

    @JsonDeserialize(as = CommandResponseFailure.class)
    @JsonSerialize(as=CommandResponseFailure.class)
    public static class CommandResponseFailure extends TryResponseFailure<Ack> implements CommandResponse,ManuallySerializable {

        private String message;

        public CommandResponseFailure() {
            super(null);
        }

        /**
         * Constructors are needed for this class because "we" create objects of this type in the code
         */
        public CommandResponseFailure(String message) {
            super(null);
            this.message=message;
        }

        public CommandResponseFailure(Throwable l) {
            super(l);
            this.message=l.getMessage();
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }


        @Override
        public Map<String, String> getJsonMap() {
            Map<String,String> map=new HashMap<>();
            map.put("\"message\"","\""+message+"\"");
            return map;
        }
        
    }

}
