package io.molr.mole.core.runnable.lang;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.Mission;
import io.molr.commons.domain.MissionHandle;
import io.molr.commons.domain.Placeholder;
import io.molr.commons.domain.StrandCommand;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.RunnableLeafsMole;

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
	public void checkAvailabilityOfRunCtxSignatures() {
		RunnableLeafsMission mission = new RunnableLeafsMissionSupport() {
			{
				root("simple").let(Placeholder.aString("p1"), in->"hello").contextual((in)->"simpleContext").sequential().as((rootBranch, ctx_p)->{
					rootBranch.branch("A")./* let(Placeholder.aString("p1"), in->"hello2"). */as(a->{
						int counter = 0;						
						a.leaf("a.run"+counter++).runCtx((String name, String val1)->{
							
						}, "a Value");
						/*
						 * run a method with value from context and two values
						 * NOTE: passed values are evaluated at definition time
						 */
						a.leaf("a.run"+counter++).runCtx((String name, String val1, String val2)->{
							
						}, "anotherValue", "anotherValue");

						a.leaf("a.run"+counter++).runCtx((String name, String val1, String val2)->{
							
						}, Placeholder.aString("A"), "B");
						
						a.leaf("a.run"+counter++).runCtx((String name, String val1, String val2)->{
							
						}, "A", Placeholder.aString("B"));
						
						a.leaf("a.run"+counter++).runCtx((String name, String val1, String val2)->{
							
						}, Placeholder.aString("A"), Placeholder.aString("B"));

						a.leaf("a.run"+counter++).runCtx((String name, String val1, String val2, Double val3)->{
							
						}, Placeholder.aString("A"), Placeholder.aString("B"), Placeholder.aDouble("C"));
						
						a.leaf("a.run"+counter++).runCtx((String name, String val1, String val2, Double val3, Double val4)->{
							
						}, Placeholder.aString("A"), Placeholder.aString("B"), Placeholder.aDouble("C"), Placeholder.aDouble("D"));
					});
				});	
			}				
		}.build();
		RunnableLeafsMole mole = new RunnableLeafsMole(Set.of(mission));
		MissionHandle handle = mole.instantiate(new Mission("simple"), Map.of()).block(Duration.ofMillis(3000));
		mole.instructRoot(handle, StrandCommand.RESUME);
		mole.statesFor(handle).blockLast(Duration.ofMillis(3000));
	}
	
	@Test
	public void asWithoutContextPlaceholderInOngoingContextualBranchWithNewContext() {
		AtomicInteger counter = new AtomicInteger();
		
		RunnableLeafsMission mission = new RunnableLeafsMissionSupport() {
			{
				root("mission").contextual(in->counter).as(branch->{
					branch.leaf("aLeaf").runCtx(ctxObj->{
						counter.incrementAndGet();
					});
				});
			}
		}.build();
			
		executeAndWaitForLastState(mission);
		Assertions.assertThat(counter.get()).isEqualTo(1);
	}
	
	@Test
	public void asWithoutContextPlaceholderInOngoingContextualBranch() {
		AtomicInteger counter = new AtomicInteger();
		
		RunnableLeafsMission mission = new RunnableLeafsMissionSupport() {
			{
				root("mission").contextual(in->counter).as(branch->{
					branch.branch("2ndLevel").as(secondLevelBranch->{
						secondLevelBranch.leaf("aLeaf").runCtx(ctxObj->{
							counter.incrementAndGet();
						});
					});
				});
			}
		}.build();
			
		executeAndWaitForLastState(mission);
		Assertions.assertThat(counter.get()).isEqualTo(1);
	}

	private void executeAndWaitForLastState(RunnableLeafsMission mission) {
		RunnableLeafsMole mole = new RunnableLeafsMole(Set.of(mission));
		MissionHandle handle = mole.instantiate(new Mission(mission.name()), Map.of()).block(Duration.ofMillis(3000));
		mole.instructRoot(handle, StrandCommand.RESUME);
		mole.statesFor(handle).blockLast(Duration.ofMillis(3000));
	}
	
	
	
}
