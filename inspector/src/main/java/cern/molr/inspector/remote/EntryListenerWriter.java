package cern.molr.inspector.remote;

import cern.molr.inspector.entry.EntryListener;
import cern.molr.inspector.entry.EntryState;
import com.google.gson.Gson;

import java.io.PrintWriter;

/**
 * An implementation of an {@link EntryListener} which relays listener calls to a {@link java.io.PrintWriter}.
 */
public class EntryListenerWriter implements EntryListener, AutoCloseable {

    private final PrintWriter writer;
    private final Gson gson = new Gson();

    /**
     * Creates a writer which uses the given {@link PrintWriter} to forward listener calls.
     *
     * @param writer The writer to send calls to.
     */
    public EntryListenerWriter(PrintWriter writer) {
        this.writer = writer;
    }

    @Override
    public void onLocationChange(EntryState state) {
        writer.println(EntryListenerMethod.ON_LOCATION_CHANGE.ordinal());
        writer.println(gson.toJson(state));
        writer.flush();
    }

    @Override
    public void onInspectionEnd(EntryState state) {
        writer.println(EntryListenerMethod.ON_INSPECTION_END.ordinal());
        writer.println(gson.toJson(state));
        writer.flush();
    }

    @Override
    public void onVmDeath() {
        writer.println(EntryListenerMethod.ON_VM_DEATH.ordinal());
        writer.flush();
    }

    @Override
    public void close() {
        writer.close();
    }

}
