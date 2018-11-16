package org.molr.mole.core.runnable.lang;

import com.google.common.collect.ImmutableSet;
import org.molr.commons.domain.MissionParameter;
import org.molr.commons.domain.Placeholder;
import org.molr.mole.core.runnable.RunnableLeafsMission;
import org.molr.commons.domain.MissionParameterDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * An abstract class which is intended to describe a tree of runnables, which can be used as simple test case for parallel tree execution.
 */
public abstract class RunnableMissionSupport {

    private final static Logger LOGGER = LoggerFactory.getLogger(RunnableMissionSupport.class);

    private RunnableLeafsMission.Builder builder;
    private ImmutableSet.Builder<MissionParameter<?>> parameterBuilder = ImmutableSet.builder();

    protected void mission(String newName, Consumer<Branch> branchConsumer) {
        if (this.builder != null) {
            throw new IllegalStateException("Root can only be defined once!");
        }

        requireNonNull(newName, "name must not be null.");
        requireNonNull(branchConsumer, "branchConsumer must not be null.");

        Branch rootBranch = root(newName);
        branchConsumer.accept(rootBranch);
    }

    private Branch root(String name) {
        this.builder = RunnableLeafsMission.builder(name);
        return Branch.withParent(builder, builder.root());
    }

    protected <T> Placeholder<T> mandatory(Placeholder<T> placeholder) {
        this.parameterBuilder.add(MissionParameter.required(placeholder));
        return placeholder;
    }

    protected <T> Placeholder<T> mandatory(Placeholder<T> placeholder, T defaultValue) {
        this.parameterBuilder.add(MissionParameter.required(placeholder).withDefault(defaultValue));
        return placeholder;
    }

    protected <T> Placeholder<T> optional(Placeholder<T> placeholder) {
        this.parameterBuilder.add(MissionParameter.optional(placeholder));
        return placeholder;
    }

    protected <T> Placeholder<T> optional(Placeholder<T> placeholder, T defaultValue) {
        this.parameterBuilder.add(MissionParameter.optional(placeholder).withDefault(defaultValue));
        return placeholder;
    }

    public RunnableLeafsMission build() {
        MissionParameterDescription parameterDescription = new MissionParameterDescription(parameterBuilder.build());
        return builder.build(parameterDescription);
    }


}
