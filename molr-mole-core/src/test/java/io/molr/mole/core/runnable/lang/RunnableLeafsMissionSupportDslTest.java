package io.molr.mole.core.runnable.lang;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission;

public class RunnableLeafsMissionSupportDslTest {

	@Test
	public void useSimpleBranch() {
		RunnableLeafsMission mission = new RunnableLeafsMissionSupport() {
			{
				Placeholder<String> namePlaceholder = mandatory(Placeholder.aString("name"));
				
				root("simple").as(rootBranch->{
					rootBranch.branch("A").as(a->{
						a.leaf("a.run1").run(()->{
							
						});
						a.leaf("a.run2").run(name->{
							
						}, namePlaceholder);;
					});
				});	
			}				
		}.build();
		Assertions.assertThat(mission.treeStructure().allBlocks())
			.containsExactlyInAnyOrder(Block.idAndText("0", "simple"), Block.idAndText("0.0", "A"), Block.idAndText("0.0.0", "a.run1"),Block.idAndText("0.0.1", "a.run2"));
	}
	
}
