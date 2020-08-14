package io.molr.mole.core.runnable.lang;

import com.google.common.collect.ImmutableSet;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.MissionParameter;
import io.molr.commons.domain.MissionParameterDescription;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

import static io.molr.mole.core.runnable.lang.BranchMode.SEQUENTIAL;
import static java.util.Objects.requireNonNull;

import java.util.Set;

/**
 * An abstract class which is intended to describe a tree of runnables, which can be used as simple test case for
 * parallel tree execution.
 */
public abstract class RunnableLeafsMissionSupport {

    private final static Logger LOGGER = LoggerFactory.getLogger(RunnableLeafsMissionSupport.class);

    private RunnableLeafsMission.Builder builder;
    private ImmutableSet.Builder<MissionParameter<?>> parameterBuilder = ImmutableSet.builder();

    protected OngoingRootBranch root(String missionName) {
        requireNonNull(missionName, "name must not be null.");
        assertNoBuilderYet();
        this.builder = RunnableLeafsMission.builder();
        return new OngoingRootBranch(missionName, builder, null, SEQUENTIAL);
    }

    @Deprecated
    protected void sequential(String newName, Consumer<SimpleBranch> branchConsumer) {
        root(newName).sequential().as(branchConsumer);
    }

    @Deprecated
    protected void parallel(String newName, Consumer<SimpleBranch> branchConsumer) {
        root(newName).parallel().as(branchConsumer);
    }

    private void assertNoBuilderYet() {
        if (this.builder != null) {
            throw new IllegalStateException("Root can only be defined once! Use either sequential() or parallel().");
        }
    }

    /*
     * TODO We should consider a custom builder for the dsl or use the MissionParameterBuilder to avoid multiple method signatures for
     * optional and mandatory
     * we could also discuss a more flexible parameter validator approach instead of allowed values
     */

    protected <T> Placeholder<T> mandatory(Placeholder<T> placeholder) {
        this.parameterBuilder.add(MissionParameter.required(placeholder));
        return placeholder;
    }

    protected <T> Placeholder<T> mandatory(Placeholder<T> placeholder, Set<T> allowedValues) {
        this.parameterBuilder.add(MissionParameter.required(placeholder).withAllowed(allowedValues));
        return placeholder;
    }

    protected <T> Placeholder<T> mandatory(Placeholder<T> placeholder, T defaultValue) {
        this.parameterBuilder.add(MissionParameter.required(placeholder).withDefault(defaultValue));
        return placeholder;
    }

    protected <T> Placeholder<T> mandatory(Placeholder<T> placeholder, T defaultValue, Set<T> allowedValues) {
        this.parameterBuilder.add(MissionParameter.required(placeholder).withDefault(defaultValue).withAllowed(allowedValues));
        return placeholder;
    }

    protected <T> Placeholder<T> optional(Placeholder<T> placeholder) {
        this.parameterBuilder.add(MissionParameter.optional(placeholder));
        return placeholder;
    }

    protected <T> Placeholder<T> optional(Placeholder<T> placeholder, Set<T> allowedValues) {
        this.parameterBuilder.add(MissionParameter.optional(placeholder).withAllowed(allowedValues));
        return placeholder;
    }

    protected <T> Placeholder<T> optional(Placeholder<T> placeholder, T defaultValue) {
        this.parameterBuilder.add(MissionParameter.optional(placeholder).withDefault(defaultValue));
        return placeholder;
    }

    protected <T> Placeholder<T> optional(Placeholder<T> placeholder, T defaultValue, Set<T> allowedValues) {
        this.parameterBuilder.add(MissionParameter.optional(placeholder).withDefault(defaultValue).withAllowed(allowedValues));
        return placeholder;
    }

    /**
     * Retrieves the most recently created block. NOTE: This is only intended for internal testing purposes!
     *
     * @return the most recently added block.
     */
    protected Block latestBlock() {
        return builder.latest();
    }

    public RunnableLeafsMission build() {
        MissionParameterDescription parameterDescription = new MissionParameterDescription(parameterBuilder.build());
        return builder.build(parameterDescription);
    }


}
