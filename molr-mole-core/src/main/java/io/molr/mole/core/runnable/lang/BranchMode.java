package io.molr.mole.core.runnable.lang;

public class BranchMode {

	public enum Mode{PARALLEL, SEQUENTIAL}
	private final int maxConcurrency;
	private final Mode mode;

	public static BranchMode SEQUENTIAL = new BranchMode(Mode.SEQUENTIAL, 1);
	public static BranchMode PARALLEL = newParallel(Integer.MAX_VALUE);
			
	private BranchMode(Mode mode, int maxConcurrency) {
		this.maxConcurrency = maxConcurrency;
		this.mode = mode;
	}
	
	public static BranchMode newParallel(int maxConcurrency) {
		return new BranchMode(Mode.PARALLEL, maxConcurrency);
	}
	
	public Mode mode() {
		return mode;
	}
	
	public int maxConcurrency() {
		return maxConcurrency;
	}

}
