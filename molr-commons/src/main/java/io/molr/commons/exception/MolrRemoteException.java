package io.molr.commons.exception;

public class MolrRemoteException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public MolrRemoteException() {
    }

    public MolrRemoteException(String message) {
        super(message);
    }

    public MolrRemoteException(String message, Throwable cause) {
        super(message, cause);
    }

    public MolrRemoteException(Throwable cause) {
        super(cause);
    }
}
