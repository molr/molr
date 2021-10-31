package io.molr.mole.core.api;

import static java.lang.String.format;

/**
 * Contains all the REST API endpoints for a mole
 */
public final class MoleWebApi {

	public static final String AGENCY_STATES = "/states";
    public static final String MISSION_HEADER = "/mission/";
    public static final String INSTANCE_HEADER = "/instance/";
    public static final String MISSION_NAME = "missionName";
    public static final String MISSION_HANDLE = "missionHandle";
    public static final String STRAND_ID = "strandId";
    public static final String COMMAND_NAME = "commandName";
    public static final String BLOCK_ID = "blockId";
    public static final String MISSION_REPRESENTATION_PATH = MISSION_HEADER + "{" + MISSION_NAME + "}/representation";
    public static final String MISSION_PARAMETER_DESCRIPTION_PATH = MISSION_HEADER + "{" + MISSION_NAME + "}/parameterDescription";
    public static final String INSTANCE_STATES_PATH = INSTANCE_HEADER + "{" + MISSION_HANDLE + "}/states";
    public static final String INSTANCE_OUTPUTS_PATH = INSTANCE_HEADER + "{" + MISSION_HANDLE + "}/outputs";
    public static final String INSTANCE_REPRESENTATIONS_PATH = INSTANCE_HEADER + "{" + MISSION_HANDLE + "}/representations";
    public static final String INSTANTIATE_MISSION_PATH = MISSION_HEADER + "{" + MISSION_NAME + "}/instantiate";
    public static final String INSTANCE_INSTRUCT_PATH = INSTANCE_HEADER + "{" + MISSION_HANDLE + "}/{" + STRAND_ID + "}/instruct/{" + COMMAND_NAME+ "}";
    public static final String INSTANCE_INSTRUCT_ROOT_PATH = INSTANCE_HEADER + "{" + MISSION_HANDLE + "}/instructRoot/{" + COMMAND_NAME+ "}";
    public static final String INSTANCE_INSTRUCT_BLOCK_PATH = INSTANCE_HEADER + "{" + MISSION_HANDLE + "}/{" + BLOCK_ID + "}/instructBlock/{" + COMMAND_NAME+ "}";
    public static final String INSTANCE_INSTRUCT_MISSION_PATH = INSTANCE_HEADER + "{" + MISSION_HANDLE + "}/instructMission/{" + COMMAND_NAME+ "}";

    public static String missionRepresentationUrl(String missionName) {
        return format(MISSION_HEADER + "%s/representation", missionName);
    }

    public static String missionParameterDescriptionUrl(String missionName) {
        return format(MISSION_HEADER + "%s/parameterDescription", missionName);
    }

    public static String instanceStatesUrl(String missionHandle){
       return  format(INSTANCE_HEADER + "%s/states", missionHandle);
    }

    public static String instanceOutputsUrl(String missionHandle){
        return  format(INSTANCE_HEADER + "%s/outputs", missionHandle);
    }

    public static String instanceRepresentationsUrl(String missionHandle){
        return  format(INSTANCE_HEADER + "%s/representations", missionHandle);
    }

    public static String instantiateMission(String missionName){
        return  format(MISSION_HEADER + "%s/instantiate", missionName);
    }

    public static String instructInstance(String missionHandle,String strandId, String commandName){
        return  format(INSTANCE_HEADER + "%s/%s/instruct/%s", missionHandle,strandId,commandName);
    }

    public static String instructRootInstance(String missionHandle, String commandName){
        return  format(INSTANCE_HEADER + "%s/instructRoot/%s", missionHandle,commandName);
    }

    public static String instructBlockInstance(String missionHandle, String blockId, String commandName){
        return  format(INSTANCE_HEADER + "%s/%s/instructBlock/%s", missionHandle,blockId,commandName);
    }
    
    public static String instructMission(String missionHandle, String commandName) {
        return format(INSTANCE_HEADER + "%s/instructMission/%s", missionHandle, commandName);
    }

    private MoleWebApi() {
        /* only static methods */
    }
}
