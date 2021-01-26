package io.molr.mole.core.runnable;

import io.molr.commons.domain.Result;
import io.molr.mole.core.tree.TreeStructure;

public class ResultStates extends BlockStates<Result>{

	public ResultStates(TreeStructure structure) {
		super(structure, Result.UNDEFINED);
	}

}
