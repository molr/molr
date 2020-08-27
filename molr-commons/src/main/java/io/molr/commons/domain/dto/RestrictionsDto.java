package io.molr.commons.domain.dto;

import java.util.Set;

// for library loggers
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

// for application loggers
//import de.gsi.cs.co.ap.common.gui.elements.logger.AppLogger;

/**
 *
 * @author krepp
 */
public class RestrictionsDto<T> {
    
    public Set<T> allowedValues;
    
    /**
     * TO TYPES allowed values
     * Containing values
     */

    // You can choose a logger (needed imports are given in the import section as comments):
    // for libraries:
    // private static final Logger LOGGER = LoggerFactory.getLogger(RestrictionsDto.class);
    // for applications:
    // private static final AppLogger LOGGER = AppLogger.getLogger();
}

