package cern.molr.server;

/**
 * The server configuration parameters
 * @author yassine-kr
 */
public class ServerConfig {
    /**
     * The default heartbeat interval that the server sends to registered supervisors, in seconds.
     */
    private int heartbeatInterval;

    /**
     * The default duration used by the server to consider that it is not receiving the state from the supervisor.
     */
    private int heartbeatTimeOut;

    /**
     * The max number of consecutive time out before considering that the supervisor is dead.
     */
    private int numMaxTimeOut;

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public int getHeartbeatTimeOut() {
        return heartbeatTimeOut;
    }

    public void setHeartbeatTimeOut(int heartbeatTimeOut) {
        this.heartbeatTimeOut = heartbeatTimeOut;
    }

    public int getNumMaxTimeOut() {
        return numMaxTimeOut;
    }

    public void setNumMaxTimeOut(int numMaxTimeOut) {
        this.numMaxTimeOut = numMaxTimeOut;
    }
}
