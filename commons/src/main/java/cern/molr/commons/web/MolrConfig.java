package cern.molr.commons.web;

/**
 * A class wrapping the MolR configuration properties
 *
 * @author yassine-kr
 * TODO the configuration must be loaded from a properties file
 */
public class MolrConfig {

    public static final String INSTANTIATE_PATH = "/instantiate";
    public static final String EVENTS_STREAM_PATH = "/getEventsStream";
    public static final String STATES_STREAM_PATH = "/getStatesStream";
    public static final String INSTRUCT_PATH = "/instruct";
    public static final String GET_STATE_PATH = "/getState";
    public static final String REGISTER_PATH = "/register";
    public static final String UNREGISTER_PATH = "/unregister";

    private MolrConfig() {
    }

}
