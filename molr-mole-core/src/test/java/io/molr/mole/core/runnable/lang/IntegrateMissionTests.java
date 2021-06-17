package io.molr.mole.core.runnable.lang;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.Mission;
import io.molr.commons.domain.MissionHandle;
import io.molr.commons.domain.MissionOutput;
import io.molr.commons.domain.Placeholder;
import io.molr.commons.domain.StrandCommand;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.RunnableLeafsMole;

public class IntegrateMissionTests {
	
	private final static String FIRST_NAME = "firstName";
	private final static String SECOND_NAME = "secondName";
	private final static String THIRD_NAME = "thirdName";
	
	private final static Placeholder<String> FIRST_NAME_P = Placeholder.aString(FIRST_NAME);
	private final static Placeholder<String>SECOND_NAME_P = Placeholder.aString(SECOND_NAME);
	private final static Placeholder<String>THIRD_NAME_P = Placeholder.aString(THIRD_NAME);

	RunnableLeafsMission twoMandatoryOneOptionalParameterMission() {
		return new RunnableLeafsMissionSupport() {
			{
				Placeholder<String> firstName = mandatory(FIRST_NAME_P);
				Placeholder<String> secondName = mandatory(SECOND_NAME_P);
				Placeholder<String> thirdName = optional(THIRD_NAME_P);
				root("IntegratedFirstName").as(branch->{
					branch.leaf("Print").run((in,out)->{
						out.emit("firstName", in.get(firstName));
						out.emit("secondName", in.get(secondName));
						String third = in.get(thirdName);
						/*
						 * NOTE emit in RunnableLeafsMission will have none effect if value is null, is this intuitive?
						 */
						out.emit("thirdName", third);
					});
				});
			}
		}.build();
	}
	
	RunnableLeafsMission singleMandatoryParameterMission() {
		return new RunnableLeafsMissionSupport() {
			{
				Placeholder<String> someName = mandatory(Placeholder.aString("firstName"));
				root("IntegratedFirstName").as(branch->{
					branch.leaf("Print").run(in->{
						System.out.println("hello "+in.get(someName));
					});
				});
			}
		}.build();
	}
	
	RunnableLeafsMission parameterlessMission() {
		return new RunnableLeafsMissionSupport() {
			{
				root("IntegratedParameterless").as(branch->{
					branch.leaf("Print").run(in->{
						System.out.println("hello ");
					});
				});
			}
		}.build();
	}
	
	@Test
	public void integrate_whenIntegratedMissionIsParameterless_treeIsBuildAsIntended() {
		RunnableLeafsMission mission = new RunnableLeafsMissionSupport() {
			{
				root("Integrating").as(topLevel->{
					topLevel.branch("level2Branch").as(level2->{
						level2.integrate(parameterlessMission());
					});
				});
			}
		}.build();
		Assertions.assertThat(mission.treeStructure().childrenOf(Block.idAndText("0.0", "level2Branch"))).contains(Block.idAndText("0.0.0", "IntegratedParameterless"));
		System.out.println(mission.treeStructure().allBlocks());
	}
	
	
	@Test
	public void integrate_whenMandatoryParameterIsNotMapped_exceptionIsThrown() {
		Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(()->{
			new RunnableLeafsMissionSupport() {
				{
					root("Integrating").as(branchDescription->{
						branchDescription.integrate(singleMandatoryParameterMission());
					});
				}
			}.build();
		});
	}
	
	@Test
	public void integrate_whenOnlyOneOfTwoMandatoryIsMapped_exceptionIsThrown() {
		Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(()->{
			new RunnableLeafsMissionSupport() {
				{
					mandatory(FIRST_NAME_P);
					root("Integrating").as(branchDescription->{
						branchDescription.integrate(twoMandatoryOneOptionalParameterMission(), FIRST_NAME_P, FIRST_NAME_P);
					});
				}
			}.build();
		});
	}
	
	@Test
	public void integrate_whenTwoOfTwoMandatoryIsMappedIdentity_() {
		RunnableLeafsMission mission = new RunnableLeafsMissionSupport() {
			{
				mandatory(FIRST_NAME_P);
				mandatory(SECOND_NAME_P);
				root("Integrating").as(branchDescription -> {
					branchDescription.integrate(twoMandatoryOneOptionalParameterMission(), FIRST_NAME_P, FIRST_NAME_P,
							SECOND_NAME_P, SECOND_NAME_P);
				});
			}
		}.build();
		
		RunnableLeafsMole mole = new RunnableLeafsMole(Set.of(mission));
		Map<String, Object> params = Map.of(FIRST_NAME, "MyFirst", SECOND_NAME, "MySecond");
		MissionHandle handle = mole.instantiate(new Mission(mission.name()), params).block(Duration.ofMillis(100));
		mole.instructRoot(handle, StrandCommand.RESUME);
		MissionOutput lastoutput = mole.outputsFor(handle).blockLast(Duration.ofMillis(100));

		Assertions.assertThat(lastoutput.content().get("0.0.0")).contains(Map.entry("firstName", "MyFirst"));
		Assertions.assertThat(lastoutput.content().get("0.0.0")).contains(Map.entry("secondName", "MySecond"));
	}
	
	@Test
	public void integrate_whenTwoOfTwoMandatoryAreMappedAndSwitched_integratedAsExpected() {
		RunnableLeafsMission mission = new RunnableLeafsMissionSupport() {
			{
				mandatory(FIRST_NAME_P);
				mandatory(SECOND_NAME_P);
				root("Integrating").let(FIRST_NAME_P, in->in.get(FIRST_NAME_P)).as(branchDescription -> {
					branchDescription.integrate(twoMandatoryOneOptionalParameterMission(), FIRST_NAME_P, SECOND_NAME_P,
							SECOND_NAME_P, FIRST_NAME_P);
				});
			}
		}.build();
		
		RunnableLeafsMole mole = new RunnableLeafsMole(Set.of(mission));
		Map<String, Object> params = Map.of(FIRST_NAME, "MyFirst", SECOND_NAME, "MySecond");
		MissionHandle handle = mole.instantiate(new Mission(mission.name()), params).block(Duration.ofMillis(100));
		mole.instructRoot(handle, StrandCommand.RESUME);
		MissionOutput lastoutput = mole.outputsFor(handle).blockLast(Duration.ofMillis(100));
		
		Assertions.assertThat(lastoutput.content().get("0.0.0")).contains(Map.entry("firstName", "MySecond"));
		Assertions.assertThat(lastoutput.content().get("0.0.0")).contains(Map.entry("secondName", "MyFirst"));
	}

}
