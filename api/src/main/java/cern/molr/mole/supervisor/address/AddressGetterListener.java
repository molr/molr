package cern.molr.mole.supervisor.address;

import java.util.concurrent.ExecutionException;

public interface AddressGetterListener {
    void onGetAddress(AddressGetter.Address address);
}
