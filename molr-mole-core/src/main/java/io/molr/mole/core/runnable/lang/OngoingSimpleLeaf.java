package io.molr.mole.core.runnable.lang;

import java.util.Map;
import java.util.function.Function;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission;

public class OngoingSimpleLeaf extends GenericOngoingLeaf<OngoingSimpleLeaf> {

    public OngoingSimpleLeaf(BlockNameConfiguration name, RunnableLeafsMission.Builder builder, Block parent, Map<Placeholder<?>, Function<In, ?>> mappings) {
        super(name,builder,parent, mappings);
    }

}
