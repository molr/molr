package cern.molr.supervisor.impl.address;

import cern.molr.supervisor.SupervisorConfig;
import cern.molr.supervisor.api.address.AddressGetter;
import cern.molr.supervisor.api.address.AddressGetterListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.HashSet;

/**
 * An implementation of an address getter. It gets the host and port from the configuration file.
 * If the host or the port are not resolved from the configuration file, this getter uses the
 * {@link SimpleAddressGetter}
 *
 * @author yassine-kr
 */
@Component
public class ConfigurationAddressGetter implements AddressGetter{

    private Address address;
    private HashSet<AddressGetterListener> listeners = new HashSet<>();

    public ConfigurationAddressGetter(SupervisorConfig config, SimpleAddressGetter simpleAddressGetter) {
        address = new Address(config.getSupervisorHost(), config.getSupervisorPort());
        if (!isCorrect()) {
            simpleAddressGetter.addListener(address -> {
                updateAddress(address);
                listeners.forEach((listener) -> listener.onGetAddress(this.address));
            });
        }
    }

    @Override
    public void addListener(AddressGetterListener listener) {
        listeners.add(listener);
        if (isCorrect()) {
            listener.onGetAddress(address);
        }
    }

    private boolean isCorrect() {
        return address.getHost() != null && address.getPort() >= 0;
    }

    private void updateAddress(Address other) {
        if (address.getHost() == null) {
            address.setHost(other.getHost());
        }
        if (address.getPort() < 0) {
            address.setPort(other.getPort());
        }
    }

}