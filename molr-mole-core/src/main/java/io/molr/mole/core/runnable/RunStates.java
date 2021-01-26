package io.molr.mole.core.runnable;
import io.molr.commons.domain.RunState;
import io.molr.mole.core.tree.TreeStructure;

public class RunStates extends BlockStates<RunState>{

	public RunStates(TreeStructure structure) {
		super(structure, RunState.NOT_STARTED);
	}

}
