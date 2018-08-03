package cern.molr.test.server;

import cern.molr.commons.api.request.server.SupervisorsInfoRequest;
import cern.molr.commons.api.response.SupervisorInfo;
import cern.molr.commons.web.MolrConfig;
import cern.molr.commons.web.WebFluxRestClient;
import cern.molr.commons.web.WebFluxWebSocketClient;
import cern.molr.server.ServerMain;
import cern.molr.supervisor.RemoteSupervisorMain;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Class for testing getting the supervisors info stream from the MolR server.
 */
public class SupervisorsInfoTest {

    private ConfigurableApplicationContext serverContext;
    private ConfigurableApplicationContext supervisorContext;
    private WebFluxRestClient client = new WebFluxRestClient("http://localhost", 8000);
    private WebFluxWebSocketClient clientSocket = new WebFluxWebSocketClient("http://localhost", 8000);

    @Before
    public void initServers() {
        serverContext = SpringApplication.run(ServerMain.class, "--server.port=8000");

        supervisorContext = SpringApplication.run(RemoteSupervisorMain.class,
                "--server.port=8056", "--molr.host=http://localhost", "--molr.port=8000",
                "--supervisor.host=http://localhost", "--supervisor.port=8056");
    }

    @After
    public void exitServers() {
        SpringApplication.exit(supervisorContext);
        SpringApplication.exit(serverContext);
    }

    @Test
    public void instantiateTest() throws Exception {

        CountDownLatch endSignal = new CountDownLatch(1);

        List<SupervisorInfo> infos = new ArrayList<>();

        SupervisorsInfoRequest request = new SupervisorsInfoRequest();


        clientSocket.receiveFlux(MolrConfig.SUPERVISORS_INFO_PATH, SupervisorInfo.class, request)
                .doOnError(Throwable::printStackTrace).subscribe(
                (info) -> {
                    System.out.println("info: " + info);
                    infos.add(info);
                    endSignal.countDown();
                });


        endSignal.await();

        Assert.assertEquals(1, infos.size());

    }
}
