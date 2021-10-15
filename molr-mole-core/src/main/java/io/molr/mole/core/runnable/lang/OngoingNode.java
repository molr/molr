package io.molr.mole.core.runnable.lang;

import static java.util.Objects.requireNonNull;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.BlockAttribute;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission;

public class OngoingNode<N extends OngoingNode<N>> {

    private final BlockNameConfiguration name;
    private final RunnableLeafsMission.Builder builder;
    private final Block parent;
    
    private Map<Placeholder<?>, Function<In, ?>> mappings;

    private final EnumSet<BlockAttribute> attributes = EnumSet.noneOf(BlockAttribute.class);

    public OngoingNode(BlockNameConfiguration name, RunnableLeafsMission.Builder builder, Block parent, Map<Placeholder<?>, Function<In, ?>> mappings) {
        this.name = name;
        this.builder = builder;
        this.parent = parent;
        this.mappings = mappings;
        //mappings= new HashMap<>();
    }

    public N perDefault(BlockAttribute attribute) {
        requireNonNull(attribute, "attribute must not be null");
        attributes.add(attribute);
        return (N) this;
    }

    public N perDefaultDont(BlockAttribute attribute) {
        requireNonNull(attribute, "attribute must not be null");
        attributes.remove(attribute);
        return (N) this;
    }

    protected RunnableLeafsMission.Builder builder() {
        return this.builder;
    }

    protected BlockNameConfiguration name() {
        return this.name;
    }

    protected Block parent() {
        return this.parent;
    }

    protected Set<BlockAttribute> blockAttributes() {
        return ImmutableSet.copyOf(this.attributes);
    }

	public Map<Placeholder<?>, Function<In, ?>> getMappings() {
		return mappings;
	}
	
	public N let(Placeholder<?> p, Function<In, ?> fun) {
		ImmutableMap.Builder<Placeholder<?>, Function<In, ?>> builder = ImmutableMap.builder();
		mappings = builder.putAll(mappings).put(p, fun).build();
		
		return (N) this;
	}
}
