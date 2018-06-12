package cern.molr.supervisor.api.address;

/**
 * Represents an address getter listener
 *
 * @author yassine-kr
 */
public interface AddressGetterListener {
    /**
     * Triggered when the host address is retrieved, or when the listener is added to the notifier and the address
     * was already retrieved. This method should be called one time
     *
     * @param address the retrieved address
     */
    void onGetAddress(AddressGetter.Address address);
}
