package org.molr.commons.util;

/**
 * Utility methods for {@link org.molr.commons.domain.Strand}s
 */
public final class Strands {

    private static final String ROOT_NAME_PLACEHOLDER = "root";

    /**
     * Does the provided name indicates a root strand?
     * @param strandName the name to compare
     * @return whether or not the name matches the root convention
     */
    public static boolean isRootStrandPlaceholder(String strandName) {
        return ROOT_NAME_PLACEHOLDER.equals(strandName);
    }

    /**
     *
     * @return the a placeholder string that represents the root strand
     */
    public static String rootStrandPlaceholder() {
        return ROOT_NAME_PLACEHOLDER;
    }

    private Strands() {
        /* static methods */
    }
}
