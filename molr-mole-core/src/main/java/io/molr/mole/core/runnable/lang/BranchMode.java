package io.molr.mole.core.runnable.lang;

public class BranchMode {

    public static enum Mode {
        PARALLEL,
        SEQUENTIAL
    }

    private final int maxConcurrency;
    private final Mode mode;

    private static final BranchMode DEFAULT_SEQUENTIAL = new BranchMode(Mode.SEQUENTIAL, 1);
    private static final BranchMode MAX_PARALLEL = parallel(Integer.MAX_VALUE);

    private BranchMode(Mode mode, int maxConcurrency) {
        this.maxConcurrency = maxConcurrency;
        this.mode = mode;
    }

    public static BranchMode parallel(int maxConcurrency) {
        return new BranchMode(Mode.PARALLEL, maxConcurrency);
    }

    public Mode mode() {
        return mode;
    }

    public int maxConcurrency() {
        return maxConcurrency;
    }

    public static BranchMode sequential() {
        return DEFAULT_SEQUENTIAL;
    }

    public static BranchMode parallel() {
        return MAX_PARALLEL;
    }

}
