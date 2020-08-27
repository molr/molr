package io.molr.commons.domain;

// for library loggers
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

// for application loggers
//import de.gsi.cs.co.ap.common.gui.elements.logger.AppLogger;

/**
 *
 * @author krepp
 */
public class ParameterRestriction {

    public String name;
    
    public ParameterRestriction(String name) {
        this.name = name;
    }
    
    public ParameterRestriction() {
        
    }
    // You can choose a logger (needed imports are given in the import section as comments):
    // for libraries:
    // private static final Logger LOGGER = LoggerFactory.getLogger(ParameterRestriction.class);
    // for applications:
    // private static final AppLogger LOGGER = AppLogger.getLogger();
}

