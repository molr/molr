package cern.molr.inspector.domain;

/**
 * Interface for fetching the source code of classes
 */
public interface SourceFetcher {
    String getSource(String classname);
}
