package cern.molr.mole.supervisor.address;

/**
 * It gets IP address and port of the supervisor in order to send it to MolR serve
 * @author yassine
 */
public interface AddressGetter {
    static public class Address{
        private String host;
        private int port;

        public Address(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }
    }

}
