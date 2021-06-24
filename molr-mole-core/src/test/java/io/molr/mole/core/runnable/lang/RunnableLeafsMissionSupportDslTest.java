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
	
	@Test
	public void contextualLeafs() {
		RunnableLeafsMission mission = new RunnableLeafsMissionSupport() {
			{
				root("simple").contextual((in)->"simpleContext").parallel(2).as((rootBranch, ctx_p)->{
					rootBranch.branch("A").as(a->{
						/*
						 * run a method with value from context and two values
						 * NOTE: passed values are evaluated at definition time
						 */
						a.leaf("a.run1").runCtx((String name, String val1, String val2)->{
							
						}, "anotherValue", "anotherValue");

						a.leaf("a.run2").runCtx((String name, String val1, String val2)->{
							
						}, Placeholder.aString("A"), Placeholder.aString("B"));

						a.leaf("a.run3").runCtx((String name, String val1, String val2, Double val3)->{
							
						}, Placeholder.aString("A"), Placeholder.aString("B"), Placeholder.aDouble("C"));
						
						a.leaf("a.run4").runCtx((String name, String val1, String val2, Double val3, Double val4)->{
							
						}, Placeholder.aString("A"), Placeholder.aString("B"), Placeholder.aDouble("C"), Placeholder.aDouble("D"));
					});
				});	
			}				
		}.build();
		Assertions.assertThat(mission.treeStructure().allBlocks())
			.containsExactlyInAnyOrder(Block.idAndText("0", "simple"), 
					Block.idAndText("0.0", "A"), Block.idAndText("0.0.0", "a.run1"),
					Block.idAndText("0.0.1", "a.run2"), Block.idAndText("0.0.2", "a.run3"),
					Block.idAndText("0.0.3", "a.run4")
					);
	}
	
}
