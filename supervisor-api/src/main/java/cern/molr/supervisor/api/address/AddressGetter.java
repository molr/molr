package cern.molr.api.address;


/**
 * It gets IP address and port of the impl in order to send it to MolR serve
 * @author yassine-kr
 */
public interface AddressGetter {
    class Address{
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

        public void setHost(String host) {
            this.host = host;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }

    /**
     *
     * @param listener listener to be notified when the getter gets the address
     */
    void addListener(AddressGetterListener listener);

}
