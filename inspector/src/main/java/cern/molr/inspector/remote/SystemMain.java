/*
 * © Copyright 2016 CERN. This software is distributed under the terms of the Apache License Version 2.0, copied
 * verbatim in the file “COPYING“. In applying this licence, CERN does not waive the privileges and immunities granted
 * to it by virtue of its status as an Intergovernmental Organization or submit itself to any jurisdiction.
 */

package cern.molr.inspector.remote;

import cern.molr.inspector.controller.JdiController;
import cern.molr.inspector.controller.JdiControllerImpl;
import cern.molr.inspector.domain.InstantiationRequest;
import cern.molr.inspector.domain.impl.InstantiationRequestImpl;
import cern.molr.inspector.entry.EntryListener;
import cern.molr.inspector.json.MissionTypeAdapter;
import cern.molr.mission.Mission;

import cern.molr.mole.Mole;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * An entry point for creating a {@link JdiController} which communicates via
 * {@link System#in} and {@link System#out}. {@link System#err} is used to communicate errors from the process.
 * @author ?
 * @author yassine
 */
public class SystemMain implements Closeable {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Mission.class, new MissionTypeAdapter().nullSafe())
            .create();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private EntryListenerWriter entryWriter;

    private final Future<?> loggerTask;

    public SystemMain(JdiControllerImpl controller, EntryListenerWriter entryWriter) {
        this.entryWriter = entryWriter;
        this.loggerTask = executor.submit(() -> {
            InputStream processError = controller.getProcessError();
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(processError))) {
                while (!Thread.interrupted()) {
                    logLine(errorReader);
                }
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        });

        controller.setOnClose(this::close);

    }

    private static void logLine(BufferedReader reader) throws IOException {
        final String line = reader.readLine();
        if (line != null) {
            System.err.println(line);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Expected 1 argument, but received " + args.length);
        } else {
            InstantiationRequest request = GSON.fromJson(args[0], InstantiationRequestImpl.class);
            PrintWriter outputWriter = new PrintWriter(System.out);
            EntryListenerWriter writer = new EntryListenerWriter(outputWriter);


            JdiControllerImpl controller = startJdi(request, writer);
            new JdiControllerReader(new BufferedReader(new InputStreamReader(System.in)), controller);
            new SystemMain(controller, writer);

        }
    }

    private static JdiControllerImpl startJdi(InstantiationRequest request, EntryListener entryListener) throws Exception {
        try {
            return JdiControllerImpl.builder()
                    .setClassPath(request.getClassPath())
                    .setEntryListener(entryListener)
                    .setMission(request.getMission(),request.getInputObject(),Class.forName(request.getInputClassName()))
                    .build();
        } catch (Exception e) {
            System.err.println("Failure when starting JDI instance:" + e);
            throw e;
        }
    }

    @Override
    public void close() {
        entryWriter.close();
        loggerTask.cancel(true);
        executor.shutdown();
        System.exit(0);
    }
}
