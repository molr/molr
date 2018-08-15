/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.client.impl;

import cern.molr.client.api.*;
import cern.molr.commons.api.web.SimpleSubscriber;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Implementation used by the operator to interact with the server
 * The constructor only constructs the {@link #client}, it does not perform any attempt to check the connection to the
 * server.
 *
 * @author yassine-kr
 */
public class MissionExecutionServiceImpl implements MissionExecutionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MissionExecutionServiceImpl.class);

    private MolrClientToServer client;

    /**
     * This constructor searches for MolR address (host and port) in "config.properties"
     * The default values are "http://localhost" and "8000"
     * Throws a runtime exception when the port property exists but cannot be parsed to an integer
     */
    public MissionExecutionServiceImpl() {

        String host = "http://localhost";
        int port = 8000;
        try (InputStream input = MissionExecutionServiceImpl.class.getClassLoader()
                .getResourceAsStream("config.properties")) {

            Properties properties = new Properties();

            properties.load(input);

            if ((host = properties.getProperty("host")) == null) {
                LOGGER.warn("host property not found, using the default value");
                host = "http://localhost";
            }

            String sPort;
            if ((sPort = properties.getProperty("port")) == null) {
                LOGGER.warn("port property not found, using the default value");
            } else {
                port = Integer.parseInt(sPort);
            }

        } catch (NumberFormatException error) {
            LOGGER.error("error while parsing the port string property into an integer", error);
            throw new RuntimeException(error);
        } catch (Exception error) {
            LOGGER.error("error while trying to get client config.properties file host and port, using the default " +
                    "values", error);
        }

        client = new MolrClientToServerImpl(host, port);
    }

    public MissionExecutionServiceImpl(String host, int port) {
        client = new MolrClientToServerImpl(host, port);
    }

    @Override
    public <I> Publisher<ClientMissionController> instantiate(String missionName, I missionArguments) {

        return client.instantiate(missionName, missionArguments, missionId -> new StandardController(new ClientControllerData(client,
                missionName, missionId)));
    }

    @Override
    public <I> ClientMissionController instantiateSync(String missionName, I missionArguments) throws MissionExecutionServiceException {
        final ClientMissionController[] clientMissionController = new ClientMissionController[1];
        final Throwable[] streamError = new Throwable[1];
        CountDownLatch countDownLatch = new CountDownLatch(1);
        SimpleSubscriber<ClientMissionController> subscriber = new SimpleSubscriber<ClientMissionController>() {
            @Override
            public void consume(ClientMissionController controller) {
                clientMissionController[0] = controller;
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable error) {
                streamError[0] = error;
                countDownLatch.countDown();
            }

            @Override
            public void onComplete() {
                countDownLatch.countDown();
            }
        };

        instantiate(missionName, missionArguments).subscribe(subscriber);

        try {
            if (!countDownLatch.await(30, TimeUnit.SECONDS)) {
                subscriber.cancel();
                throw new MissionExecutionServiceException("Time out reached");

            }
        } catch (InterruptedException error) {
            LOGGER.error("error while waiting a response from the MolR server", error);
        }

        if (streamError[0] != null) {
            throw new MissionExecutionServiceException("Error in the connection with the server", streamError[0]);
        }

        if (clientMissionController[0] == null) {
            throw new MissionExecutionServiceException("The server response is empty");
        }

        return clientMissionController[0];

    }

    @Override
    public <I, C extends ClientMissionController> Publisher<C> instantiateCustomController(String missionName,
                                                                                           I missionArguments,
                                                                                           Function<ClientControllerData, C> controllerConstructor) {
        return client.instantiate(missionName, missionArguments, missionId -> controllerConstructor.apply(new ClientControllerData(client,
                missionName, missionId)));
    }
}
