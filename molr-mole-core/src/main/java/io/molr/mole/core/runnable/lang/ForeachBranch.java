package io.molr.mole.core.runnable.lang;

import io.molr.commons.domain.Block;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;

public class ForeachBranch extends SimpleBranch{

	protected ForeachBranch(Builder builder, Block parent) {
		super(builder, parent);
	}

	@Override
	public OngoingSimpleBranch branch(String name) {
		// TODO Auto-generated method stub
		return super.branch(name);
	}
}
