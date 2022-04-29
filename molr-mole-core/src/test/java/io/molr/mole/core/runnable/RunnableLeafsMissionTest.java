package io.molr.mole.core.runnable;

import org.junit.Test;

import io.molr.mole.core.runnable.lang.RunnableLeafsMissionSupport;

public class RunnableLeafsMissionTest {
	
	@Test(expected = IllegalArgumentException.class)
	public void constructor_whenLeafWithoutRunnableProvided_illgealParameterException() {
		new RunnableLeafsMissionSupport() {
			{
				root("missionName").as(rootBranch->{
					rootBranch.leaf("runnable");
				});
			}
		}.build();
	}
	
}

