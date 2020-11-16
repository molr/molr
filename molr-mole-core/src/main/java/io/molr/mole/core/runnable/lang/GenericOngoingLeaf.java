package io.molr.mole.core.runnable.lang;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Out;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.utils.Checkeds;

import static java.util.Objects.requireNonNull;

public abstract class GenericOngoingLeaf<L extends GenericOngoingLeaf<L>> extends OngoingNode<L> {

    public GenericOngoingLeaf(BlockNameConfiguration name, RunnableLeafsMission.Builder builder, Block parent) {
        super(
                requireNonNull(name, "leafName must not be null"),
                requireNonNull(builder, "builder must not be null"),
                requireNonNull(parent, "parent must not be null")
        );
    }
    
    public void run(Runnable runnable) {
        run((in, out) -> runnable.run());
    }

    public void run(Checkeds.CheckedThrowingRunnable runnable) {
        run((in, out) -> runnable.run());
    }

    public void run(Checkeds.CheckedThrowingConsumer<In> runnable) {
        run((in, out) -> runnable.accept(in));
    }

    public void run(Checkeds.CheckedThrowingBiConsumer<In, Out> runnable) {
        builder().leafChild(parent(), name(), runnable, blockAttributes());
    }

    public  <P1> void run(Checkeds.CheckedThrowingConsumer<P1> runnable, Placeholder<P1> p1) {
    	run(in->{
    		runnable.accept(in.get(p1));
    	});
    }
    
    public  <P1, P2> void run(Checkeds.CheckedThrowingBiConsumer<P1, P2> runnable, Placeholder<P1> p1, Placeholder<P2> p2) {
    	run(in->{
    		runnable.accept(in.get(p1), in.get(p2));
    	});
    }
    
    public  <P1, P2, P3> void run(Checkeds.CheckedThrowingConsumer3<P1, P2, P3> runnable, Placeholder<P1> p1, Placeholder<P2> p2, Placeholder<P3> p3) {
    	run(in->{
    		runnable.accept(in.get(p1), in.get(p2), in.get(p3));
    	});
    }
}
