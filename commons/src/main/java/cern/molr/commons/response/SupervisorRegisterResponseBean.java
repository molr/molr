package cern.molr.commons.response;

/**
 * Bean representing a response to a register request
 *
 * @author yassine
 */
public class SupervisorRegisterResponseBean {

    private String supervisorId;

    /**
     */
    public SupervisorRegisterResponseBean() {
    }
    
    /**
     * @param supervisorId
     */
    public SupervisorRegisterResponseBean(String supervisorId) {
        this.supervisorId = supervisorId;
    }

    public String getSupervisorId() {
        return supervisorId;
    }

    public void setSupervisorId(String supervisorId) {
        this.supervisorId = supervisorId;
    }

}
