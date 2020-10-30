package io.molr.mole.core.runnable.lang.ctx;

import java.util.function.Function;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Out;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.GenericOngoingLeaf;
import io.molr.mole.core.utils.Checkeds;

public class OngoingContextualLeaf<C> extends GenericOngoingLeaf<OngoingContextualLeaf<C>> {

	Placeholder<C> contextPlaceholder;
	
    public OngoingContextualLeaf(String name, RunnableLeafsMission.Builder builder, Block parent, Placeholder<C> contextPlaceholder) {
        super(name, builder, parent);
        if(contextPlaceholder == null) throw new IllegalArgumentException("ph bull");
        this.contextPlaceholder = contextPlaceholder;
    }

    public void runCtx(Checkeds.CheckedThrowingConsumer<C> runnable) {
        run(in -> {
        	System.out.println(contextPlaceholder);
            C c = in.get(contextPlaceholder);
            runnable.accept(c);
        });
    }

    public void runCtx(Checkeds.CheckedThrowingBiConsumer<C, In> runnable) {
        run(in -> {
            C c = in.get(contextPlaceholder);
            runnable.accept(c, in);
        });
    }

    public void runCtx(Checkeds.CheckedThrowingConsumer3<C, In, Out> runnable) {
        run((in, out) -> {
            C c = in.get(contextPlaceholder);
            runnable.accept(c, in, out);
        });
    }

    // one arg
    public <P1> void runCtx(Checkeds.CheckedThrowingBiConsumer<C, P1> runnable, P1 p1) {
        runCtx1(runnable, in -> p1);
    }

    public <P1> void runCtx(Checkeds.CheckedThrowingBiConsumer<C, P1> runnable, Placeholder<P1> p1) {
        runCtx1(runnable, in -> in.get(p1));
    }

    private <P1> void runCtx1(Checkeds.CheckedThrowingBiConsumer<C, P1> runnable, Function<In, P1> p1) {
        run(in -> {
            C c = in.get(contextPlaceholder);
            P1 p1Value = p1.apply(in);
            runnable.accept(c, p1Value);
        });
    }

    // 2 args
    public <P1, P2> void runCtx(Checkeds.CheckedThrowingConsumer3<C, P1, P2> runnable, P1 p1, P2 p2) {
        runCtx2(runnable, in -> p1, in -> p2);
    }

    public <P1, P2> void runCtx(Checkeds.CheckedThrowingConsumer3<C, P1, P2> runnable, Placeholder<P1> p1, P2 p2) {
        runCtx2(runnable, in -> in.get(p1), in -> p2);
    }

    public <P1, P2> void runCtx(Checkeds.CheckedThrowingConsumer3<C, P1, P2> runnable, P1 p1, Placeholder<P2> p2) {
        runCtx2(runnable, in -> p1, in -> in.get(p2));
    }

    public <P1, P2> void runCtx(Checkeds.CheckedThrowingConsumer3<C, P1, P2> runnable, Placeholder<P1> p1,
                                Placeholder<P2> p2) {
        runCtx2(runnable, in -> in.get(p1), in -> in.get(p2));
    }

    private <P1, P2> void runCtx2(Checkeds.CheckedThrowingConsumer3<C, P1, P2> runnable, Function<In, P1> p1,
                                  Function<In, P2> p2) {
        run(in -> {
            C c = in.get(contextPlaceholder);
            runnable.accept(c, p1.apply(in), p2.apply(in));
        });
    }

    public <P1, P2, P3> void runCtx(Checkeds.CheckedThrowingConsumer4<C, P1, P2, P3> runnable, Placeholder<P1> p1,
                                    Placeholder<P2> p2, Placeholder<P3> p3) {
        runCtx3(runnable, in -> in.get(p1), in -> in.get(p2), in -> in.get(p3));
    }

    private <P1, P2, P3> void runCtx3(Checkeds.CheckedThrowingConsumer4<C, P1, P2, P3> runnable, Function<In, P1> p1,
                                      Function<In, P2> p2, Function<In, P3> p3) {
        run(in -> {
            C c = in.get(contextPlaceholder);
            runnable.accept(c, p1.apply(in), p2.apply(in), p3.apply(in));
        });
    }

    public <P1, P2, P3, P4> void runCtx(Checkeds.CheckedThrowingConsumer5<C, P1, P2, P3, P4> runnable, Placeholder<P1> p1,
                                        Placeholder<P2> p2, Placeholder<P3> p3, Placeholder<P4> p4) {
        runCtx4(runnable, in -> in.get(p1), in -> in.get(p2), in -> in.get(p3), in -> in.get(p4));
    }

    private <P1, P2, P3, P4> void runCtx4(Checkeds.CheckedThrowingConsumer5<C, P1, P2, P3, P4> runnable, Function<In, P1> p1,
                                          Function<In, P2> p2, Function<In, P3> p3, Function<In, P4> p4) {
        run(in -> {
            C c = in.get(contextPlaceholder);
            runnable.accept(c, p1.apply(in), p2.apply(in), p3.apply(in), p4.apply(in));
        });
    }


}
