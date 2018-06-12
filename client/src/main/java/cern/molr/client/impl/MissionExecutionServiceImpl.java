/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.client.impl;

import cern.molr.client.api.ClientMissionController;
import cern.molr.client.api.MissionExecutionService;
import cern.molr.commons.api.request.MissionCommand;
import cern.molr.commons.api.response.CommandResponse;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.api.web.MolrWebClient;
import cern.molr.commons.api.web.MolrWebSocketClient;
import cern.molr.commons.impl.web.MolrWebClientImpl;
import cern.molr.commons.impl.web.MolrWebSocketClientImpl;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

/**
 * Implementation used by the operator to interact with the server
 * The constructor only constructs the client, it does not perform any attempt to check the connection to the server.
 *
 * @author yassine-kr
 */
public class MissionExecutionServiceImpl implements MissionExecutionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MissionExecutionServiceImpl.class);

    private MolrWebClient client;
    private MolrWebSocketClient clientSocket;

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

        client = new MolrWebClientImpl(host, port);
        clientSocket = new MolrWebSocketClientImpl(host, port);
    }

    public MissionExecutionServiceImpl(String host, int port) {
        client = new MolrWebClientImpl(host, port);
        clientSocket = new MolrWebSocketClientImpl(host, port);
    }

    @Override
    public <I> Publisher<ClientMissionController> instantiate(String missionName, I missionArguments) {

        return client.instantiate(missionName, missionArguments, missionId -> new ClientMissionController() {
            @Override
            public Publisher<MissionEvent> getEventsStream() {
                return clientSocket.getEventsStream(missionName, missionId);
            }

            @Override
            public Publisher<CommandResponse> instruct(MissionCommand command) {
                return clientSocket.instruct(missionName, missionId, command);
            }
        });
    }
}
