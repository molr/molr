package cern.molr.test.client;

import cern.molr.client.api.ClientMissionController;
import cern.molr.client.api.MissionExecutionService;
import cern.molr.client.api.MissionExecutionServiceException;
import cern.molr.client.impl.MissionExecutionServiceImpl;
import cern.molr.sample.mission.Fibonacci;
import cern.molr.server.ServerMain;
import cern.molr.supervisor.RemoteSupervisorMain;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Class for testing the method {@link cern.molr.client.api.MissionExecutionService#instantiateSync(String, Object)}
 */
public class SynchInstantiationTest {

    private ConfigurableApplicationContext serverContext;
    private ConfigurableApplicationContext supervisorContext;
    private MissionExecutionService service = new MissionExecutionServiceImpl("http://localhost", 8000);

    public void initServers() {
        serverContext = SpringApplication.run(ServerMain.class, "--server.port=8000");

        supervisorContext = SpringApplication.run(RemoteSupervisorMain.class,
                "--server.port=8056", "--molr.host=http://localhost", "--molr.port=8000",
                "--supervisor.host=http://localhost", "--supervisor.port=8056");
    }


    public void exitServers() {
        SpringApplication.exit(supervisorContext);
        SpringApplication.exit(serverContext);

    }

    @Test
    public void syncInstantiationError() {
        try {
            service.instantiateSync(Fibonacci.class.getCanonicalName(), 100);
            Assert.fail();
        } catch (MissionExecutionServiceException error) {
            Assert.assertEquals(error.getMessage(), "Error in the connection with the server");
        }
    }

    @Test
    public void syncInstantiationSuccess() {
        initServers();

        try {
            ClientMissionController controller = service.instantiateSync(Fibonacci.class.getCanonicalName(), 100);
            Assert.assertNotNull(controller);
        } catch (MissionExecutionServiceException error) {
            Assert.fail();
        }

        exitServers();
    }
}
