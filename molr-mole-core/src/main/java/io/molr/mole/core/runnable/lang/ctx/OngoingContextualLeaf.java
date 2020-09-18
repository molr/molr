package io.molr.mole.core.runnable.lang.ctx;

import static io.molr.commons.domain.Placeholders.context;

import java.util.function.Function;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Out;
import io.molr.commons.domain.Placeholder;
import io.molr.commons.domain.Placeholders;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.GenericOngoingLeaf;
import io.molr.mole.core.utils.Checkeds;

public class OngoingContextualLeaf<C> extends GenericOngoingLeaf<OngoingContextualLeaf<C>> {

    public OngoingContextualLeaf(String name, RunnableLeafsMission.Builder builder, Block parent) {
        super(name, builder, parent);
    }

    public void runCtx(Checkeds.CheckedThrowingConsumer<C> runnable) {
        run(in -> {
            C c = in.get(Placeholders.context());
            runnable.accept(c);
        });
    }

    public void runCtx(Checkeds.CheckedThrowingBiConsumer<C, In> runnable) {
        run(in -> {
            C c = in.get(Placeholders.context());
            runnable.accept(c, in);
        });
    }

    public void runCtx(Checkeds.CheckedThrowingConsumer3<C, In, Out> runnable) {
        run((in, out) -> {
            C c = in.get(Placeholders.context());
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
            C c = in.get(Placeholders.context());
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
            C c = in.get(context());
            runnable.accept(c, p1.apply(in), p2.apply(in));
        });
    }

}
