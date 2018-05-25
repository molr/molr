package cern.molr.commons.response;

import cern.molr.commons.trye.TryResponse;
import cern.molr.commons.trye.TryResponseFailure;
import cern.molr.commons.trye.TryResponseSuccess;
import cern.molr.mole.supervisor.MoleExecutionCommandResponse;
import cern.molr.type.Ack;
import cern.molr.type.ManuallySerializable;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.HashMap;
import java.util.Map;

/**
 * A command response sent back to the MolR server by the supervisor then to the client after sending a command request
 * @author ?
 * @author yassine-kr
 */
public interface CommandResponse extends TryResponse<Ack>,MoleExecutionCommandResponse {

    class CommandResponseSuccess extends TryResponseSuccess<Ack> implements CommandResponse {
        public CommandResponseSuccess() {
            super(null);
        }

        public CommandResponseSuccess(Ack ack) {
            super(ack);
        }
    }

    class CommandResponseFailure extends TryResponseFailure<Ack> implements CommandResponse,ManuallySerializable {

        private String message;

        public CommandResponseFailure() {
            super(null);
        }

        public CommandResponseFailure(String message) {
            super(null);
            this.message=message;
        }

        public CommandResponseFailure(Throwable throwable) {
            super(throwable);
            this.message=throwable.getMessage();
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
