/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.client.impl;

import cern.molr.client.api.ClientMissionController;
import cern.molr.client.api.MissionExecutionService;
import cern.molr.commons.request.MissionCommand;
import cern.molr.commons.response.CommandResponse;
import cern.molr.commons.response.MissionEvent;
import cern.molr.commons.web.MolrWebClientImpl;
import cern.molr.commons.web.MolrWebSocketClient;
import cern.molr.commons.web.MolrWebSocketClientImpl;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * Implementation used by the operator to interact with the server
 * The constructor searches for MolR address (host and port) in "config.properties"
 * The default values are "http://localhost" and "8000"
 *
 * @author yassine-kr
 */
public class MissionExecutionServiceImpl implements MissionExecutionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MissionExecutionServiceImpl.class);

    private MolrWebClientImpl client;
    private MolrWebSocketClient clientSocket;

    public MissionExecutionServiceImpl() {
        try (InputStream input = MissionExecutionServiceImpl.class.getClassLoader()
                .getResourceAsStream("config.properties")) {

            Properties properties = new Properties();

            properties.load(input);

            String host = properties.getProperty("host");
            Objects.requireNonNull(host);

            int port = Integer.parseInt(properties.getProperty("port"));

            client = new MolrWebClientImpl(host, port);
            clientSocket = new MolrWebSocketClientImpl(host, port);

        } catch (Exception error) {
            LOGGER.error("error while trying to get client properties", error);
            client = new MolrWebClientImpl("http://localhost", 8000);
            clientSocket = new MolrWebSocketClientImpl("http://localhost", 8000);
        }
    }

    public MissionExecutionServiceImpl(String host, int port) {
        client = new MolrWebClientImpl(host, port);
        clientSocket = new MolrWebSocketClientImpl(host, port);
    }

    @Override
    public <I> Publisher<ClientMissionController> instantiate(String missionName, I args) {


        return client.instantiate(missionName, args, missionExecutionId -> new ClientMissionController() {
                            @Override
                            public Publisher<MissionEvent> getEventsStream() {
                                return clientSocket.getEventsStream(missionName,missionExecutionId);
                            }
                            @Override
                            public Publisher<CommandResponse> instruct(MissionCommand command) {
                                return clientSocket.instruct(missionName,missionExecutionId,command);
                            }
                        });
    }
}
