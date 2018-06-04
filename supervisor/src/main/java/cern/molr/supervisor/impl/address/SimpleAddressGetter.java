package cern.molr.supervisor.impl.address;

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
 * A simple implementation of an address getter. It gets a hostname different from localhost, and the machine
 * port on which the Spring Server is running. If no hostname found, "localhost" is returned
 * This class is not thread safe. The methods addListener and onApplicationEvent should not be called by two threads
 * at the same time.
 *
 * @author yassine-kr
 */
@Component
public class SimpleAddressGetter implements AddressGetter, ApplicationListener<WebServerInitializedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleAddressGetter.class);

    private Address address;
    private HashSet<AddressGetterListener> listeners = new HashSet<>();

    public SimpleAddressGetter() {
        address = new Address("localhost", -1);
        try {
            for (NetworkInterface netInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InetAddress inetAddress : Collections.list(netInterface.getInetAddresses())) {
                    if (!inetAddress.isLinkLocalAddress() && !inetAddress.isLoopbackAddress()) {
                        address.setHost(inetAddress.getHostName());
                        return;
                    }

                }
            }
        } catch (Exception e) {
            LOGGER.error("exception when getting host network interfaces", e);
        }
    }

    @Override
    public void addListener(AddressGetterListener listener) {
        listeners.add(listener);
        if (address.getPort() >= 0)
            listener.onGetAddress(address);
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        address.setPort(event.getWebServer().getPort());
        listeners.forEach((listener) -> listener.onGetAddress(address));
    }

}