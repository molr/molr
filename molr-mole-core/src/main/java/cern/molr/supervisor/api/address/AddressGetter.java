package cern.molr.supervisor.api.address;


/**
 * It gets IP address and port of the supervisor in order to send them to MolR server
 *
 * @author yassine-kr
 */
public interface AddressGetter {

    /**
     * @param listener listener to be notified when the getter gets the address
     */
    void addListener(AddressGetterListener listener);

    class Address {
        private String host;
        private int port;

        public Address(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }


}
