package io.molr.mole.core.runnable.lang;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.BlockAttribute;
import io.molr.commons.domain.Mission;
import io.molr.commons.domain.MissionHandle;
import io.molr.commons.domain.MissionOutput;
import io.molr.commons.domain.MissionRepresentation;
import io.molr.commons.domain.Placeholder;
import io.molr.commons.domain.StrandCommand;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.RunnableLeafsMole;

public class IntegrateMissionTests {
	
	private final static long DEFAULT_TIMEOUT = 1000;
	
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
				root("IntegratedFirstName").parallel(2).as(branch->{
					branch.leaf("Print").perDefault(BlockAttribute.ON_ERROR_SKIP_SEQUENTIAL_SIBLINGS).run((in,out)->{
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
	public void embed_whenIntegratedMissionIsParameterless_treeIsBuildAsIntended() {
		RunnableLeafsMission mission = new RunnableLeafsMissionSupport() {
			{
				root("Integrating").as(topLevel->{
					topLevel.branch("level2Branch").as(level2->{
						level2.embed(parameterlessMission());
					});
				});
			}
		}.build();
		Assertions.assertThat(mission.treeStructure().childrenOf(Block.idAndText("0.0", "level2Branch"))).contains(Block.idAndText("0.0.0", "IntegratedParameterless"));
		System.out.println(mission.treeStructure().allBlocks());
	}
	
	
	@Test
	public void embed_whenMandatoryParameterIsNotMapped_exceptionIsThrown() {
		Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(()->{
			new RunnableLeafsMissionSupport() {
				{
					root("Integrating").as(branchDescription->{
						branchDescription.embed(singleMandatoryParameterMission());
					});
				}
			}.build();
		});
	}
	
	@Test
	public void embed_whenOnlyOneOfTwoMandatoryIsMapped_exceptionIsThrown() {
		Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(()->{
			new RunnableLeafsMissionSupport() {
				{
					mandatory(FIRST_NAME_P);
					root("Integrating").as(branchDescription->{
						branchDescription.embed(twoMandatoryOneOptionalParameterMission(), FIRST_NAME_P, FIRST_NAME_P);
					});
				}
			}.build();
		});
	}
	
	@Test
	public void embed_whenTwoOfTwoMandatoryIsMappedIdentity_() {
		RunnableLeafsMission mission = new RunnableLeafsMissionSupport() {
			{
				mandatory(FIRST_NAME_P);
				mandatory(SECOND_NAME_P);
				root("Integrating").as(branchDescription -> {
					branchDescription.embed(twoMandatoryOneOptionalParameterMission(), FIRST_NAME_P, FIRST_NAME_P,
							SECOND_NAME_P, SECOND_NAME_P);
				});
			}
		}.build();
		
		MissionRepresentation representation = mission.treeStructure().missionRepresentation();
		Block printBlock = representation.blockOfId("0.0.0").get();
		List<BlockAttribute> attributes = representation.blockAttributes().get(printBlock);
		Assertions.assertThat(attributes).containsExactly(BlockAttribute.ON_ERROR_SKIP_SEQUENTIAL_SIBLINGS);
		
		Block integratedRoot = representation.blockOfId("0.0").get();
		Assertions.assertThat(mission.treeStructure().isParallel(integratedRoot)).isTrue();
		
		RunnableLeafsMole mole = new RunnableLeafsMole(Set.of(mission));
		Map<String, Object> params = Map.of(FIRST_NAME, "MyFirst", SECOND_NAME, "MySecond");
		MissionHandle handle = mole.instantiate(new Mission(mission.name()), params).block(Duration.ofMillis(DEFAULT_TIMEOUT));
		mole.instructRoot(handle, StrandCommand.RESUME);
		MissionOutput lastoutput = mole.outputsFor(handle).blockLast(Duration.ofMillis(DEFAULT_TIMEOUT));

		Assertions.assertThat(lastoutput.content().get("0.0.0")).contains(Map.entry("firstName", "MyFirst"));
		Assertions.assertThat(lastoutput.content().get("0.0.0")).contains(Map.entry("secondName", "MySecond"));
	}
	
	@Test
	public void embed_whenTwoOfTwoMandatoryAreMappedAndSwitched_integratedAsExpected() {
		RunnableLeafsMission mission = new RunnableLeafsMissionSupport() {
			{
				mandatory(FIRST_NAME_P);
				mandatory(SECOND_NAME_P);
				root("Integrating").let(FIRST_NAME_P, in->in.get(FIRST_NAME_P)).as(branchDescription -> {
					branchDescription.embed(twoMandatoryOneOptionalParameterMission(), FIRST_NAME_P, SECOND_NAME_P,
							SECOND_NAME_P, FIRST_NAME_P);
				});
			}
		}.build();
		
		RunnableLeafsMole mole = new RunnableLeafsMole(Set.of(mission));
		Map<String, Object> params = Map.of(FIRST_NAME, "MyFirst", SECOND_NAME, "MySecond");
		MissionHandle handle = mole.instantiate(new Mission(mission.name()), params).block(Duration.ofMillis(DEFAULT_TIMEOUT));
		mole.instructRoot(handle, StrandCommand.RESUME);
		MissionOutput lastoutput = mole.outputsFor(handle).blockLast(Duration.ofMillis(DEFAULT_TIMEOUT));
		
		Assertions.assertThat(lastoutput.content().get("0.0.0")).contains(Map.entry("firstName", "MySecond"));
		Assertions.assertThat(lastoutput.content().get("0.0.0")).contains(Map.entry("secondName", "MyFirst"));
	}

}
