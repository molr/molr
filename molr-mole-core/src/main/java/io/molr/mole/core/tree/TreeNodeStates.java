package io.molr.mole.core.tree;

import io.molr.mole.core.runnable.ResultStates;
import io.molr.mole.core.runnable.RunStates;

public class TreeNodeStates {
	
	private final RunStates runStates;
	private final ResultStates resultStates;
	
	public TreeNodeStates(TreeStructure structure) {
		runStates = new RunStates(structure);
		resultStates = new ResultStates(structure);
	}

	public RunStates getRunStates() {
		return runStates;
	}

	public ResultStates getResultStates() {
		return resultStates;
	}

}
