package io.molr.mole.core.runnable.lang;

import io.molr.commons.domain.Block;
import io.molr.mole.core.runnable.RunnableLeafsMission;

public class OngoingSimpleLeaf extends GenericOngoingLeaf<OngoingSimpleLeaf> {

    public OngoingSimpleLeaf(BlockNameConfiguration name, RunnableLeafsMission.Builder builder, Block parent) {
        super(name,builder,parent);
    }

}
