package cern.molr.client.api;

/**
 * Data used to create a client controller
 * @author yassine-kr
 */
public class ClientControllerData {
    /**
     * The client used to perform network connections
     */
    private final MolrClientToServer client;

    private final String missionName;
    private final String missionId;

    public ClientControllerData(MolrClientToServer client, String missionName, String missionId) {
        this.client = client;
        this.missionName = missionName;
        this.missionId = missionId;
    }

    public MolrClientToServer getClient() {
        return client;
    }

    public String getMissionName() {
        return missionName;
    }

    public String getMissionId() {
        return missionId;
    }
}
