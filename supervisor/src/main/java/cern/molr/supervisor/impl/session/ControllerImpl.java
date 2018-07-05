package cern.molr.supervisor.impl.session;

import cern.molr.commons.api.request.MissionCommand;
import cern.molr.commons.api.response.Ack;
import cern.molr.commons.api.response.CommandResponse;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.supervisor.api.session.EventsListener;
import cern.molr.supervisor.api.session.MoleController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A controller which controls running of a mission
 * It communicates with MoleRunner using output and input streams
 *
 * @author yassine-kr
 */
public class ControllerImpl implements MoleController, EventsListener, Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerImpl.class);

    private final Set<EventsListener> listeners = new HashSet<>();
    private final PrintWriter printWriter;
    private final EventsReader reader;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Future<?> loggerTask;

    private ObjectMapper mapper = new ObjectMapper();

    /**
     * Event sent by the MoleRunner when it verifies the command.
     * Must be volatile (not cached) because it is accessible by two threads in the same time
     * Can't manage multiple commands in the same time
     */
    private volatile CommandStatus commandStatus = null;

    public ControllerImpl(Process process) {
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        printWriter = new PrintWriter(process.getOutputStream());
        reader = new EventsReader(new BufferedReader(new InputStreamReader(process.getInputStream())), this);

        this.loggerTask = executor.submit(() -> {
            InputStream processError = process.getErrorStream();
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(processError))) {
                while (!Thread.interrupted()) {
                    Thread.sleep(10000);
                    logLine(errorReader);
                }
            } catch (IOException error) {
                LOGGER.error("Error while trying to read the MoleRunner error stream", error);
            } catch (InterruptedException ignored) {

            }
        });


    }

    private static void logLine(BufferedReader reader) throws IOException {
        final String line = reader.readLine();
        if (line != null) {
            System.err.println(line);
        }
    }

    @Override
    public void addMoleExecutionListener(EventsListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeMoleExecutionListener(EventsListener listener) {
        listeners.remove(listener);
    }


    /**
     * Need to be "synchronized" to avoid sending many commands at the same time
     * The next command is executed after returning current command result
     *
     * @param command
     *
     * @return the command response; whether the command was accepted by the MoleRunner
     */
    @Override
    synchronized public CommandResponse sendCommand(MissionCommand command) {
        try {
            printWriter.println(mapper.writeValueAsString(command));
            printWriter.flush();


            while (commandStatus == null) {
            }
            if (commandStatus.isAccepted()) {
                String message = commandStatus.getReason();
                commandStatus = null;
                return new CommandResponse(new Ack(message));
            } else {
                CommandResponse response =
                        new CommandResponse(commandStatus.getException());
                commandStatus = null;
                return response;
            }
        } catch (JsonProcessingException error) {
            commandStatus = null;
            LOGGER.error("unable to serialize a command [{}]", command, error);
            return new CommandResponse(error);
        }
    }


    @Override
    public void onEvent(MissionEvent event) {
        if (event instanceof CommandStatus) {
            commandStatus = (CommandStatus) event;
        } else {
            listeners.forEach((l) -> l.onEvent(event));
        }
    }

    @Override
    public void close() {
        loggerTask.cancel(true);
        executor.shutdown();
        printWriter.close();
        reader.close();
    }
}
