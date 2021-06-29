package io.molr.mole.core.runnable.lang;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.function.Function;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Out;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;
import io.molr.mole.core.utils.Checkeds;

public class OngoingForeachLeaf<T> extends GenericOngoingLeaf<OngoingForeachLeaf<T>> {

	Placeholder<T> itemPlaceholder;
	
	public OngoingForeachLeaf(BlockNameConfiguration name, Builder builder, Block parent, Placeholder<T> item, Map<Placeholder<?>, Function<In, ?>> mappings) {
		super(name, builder, parent, mappings);
		requireNonNull(item);
		this.itemPlaceholder = item;
	}

	private Placeholder<T> itemPlaceholder() {
		return itemPlaceholder;
	}
	
	private T item(In in, Placeholder<T> itemPlaceholder) {
		return in.get(itemPlaceholder);
	}

	@Deprecated
	public void runFor(Checkeds.CheckedThrowingConsumer<T> runnable) {
        run(in -> {
            T i = in.get(itemPlaceholder());
            runnable.accept(i);
        });
    }

	@Deprecated
	public void runFor(Checkeds.CheckedThrowingBiConsumer<T, In> runnable) {
        run(in -> {
            T item = in.get(itemPlaceholder());
            runnable.accept(item, in);
        });
    }

	@Deprecated
	public void runFor(Checkeds.CheckedThrowingConsumer3<T, In, Out> runnable) {
        run((in, out) -> {
            runnable.accept(item(in, itemPlaceholder()), in, out);
        });
    }

	@Deprecated
	public <P1> void runFor(Checkeds.CheckedThrowingBiConsumer<T, P1> runnable, P1 p1) {
        runFor1(runnable, in -> p1);
    }

	@Deprecated
	public <P1> void runFor(Checkeds.CheckedThrowingBiConsumer<T, P1> runnable, Placeholder<P1> p1) {
        runFor1(runnable, in -> in.get(p1));
    }

	private <P1> void runFor1(Checkeds.CheckedThrowingBiConsumer<T, P1> runnable, Function<In, P1> p1) {
        run(in -> {
            P1 p1Value = p1.apply(in);
            runnable.accept(item(in, itemPlaceholder()), p1Value);
        });
    }

	@Deprecated
	public <P1, P2> void runFor(Checkeds.CheckedThrowingConsumer3<T, P1, P2> runnable, P1 p1, P2 p2) {
        runFor2(runnable, in -> p1, in -> p2);
    }

	@Deprecated
	public <P1, P2> void runFor(Checkeds.CheckedThrowingConsumer3<T, P1, P2> runnable, Placeholder<P1> p1, P2 p2) {
        runFor2(runnable, in -> in.get(p1), in -> p2);
    }

	@Deprecated
	public <P1, P2> void runFor(Checkeds.CheckedThrowingConsumer3<T, P1, P2> runnable, P1 p1, Placeholder<P2> p2) {
        runFor2(runnable, in -> p1, in -> in.get(p2));
    }

	@Deprecated
	public <P1, P2> void runFor(Checkeds.CheckedThrowingConsumer3<T, P1, P2> runnable, Placeholder<P1> p1,
            Placeholder<P2> p2) {
        runFor2(runnable, in -> in.get(p1), in -> in.get(p2));
    }

	private <P1, P2> void runFor2(Checkeds.CheckedThrowingConsumer3<T, P1, P2> runnable, Function<In, P1> p1,
            Function<In, P2> p2) {
        run(in -> {
            runnable.accept(item(in, itemPlaceholder()), p1.apply(in), p2.apply(in));
        });
    }
	
    public void runCtx(Checkeds.CheckedThrowingConsumer<T> runnable) {
        run(in -> {
            T c = in.get(itemPlaceholder);
            runnable.accept(c);
        });
    }

    public void runCtx(Checkeds.CheckedThrowingBiConsumer<T, In> runnable) {
        run(in -> {
            T c = in.get(itemPlaceholder);
            runnable.accept(c, in);
        });
    }

    public void runCtx(Checkeds.CheckedThrowingConsumer3<T, In, Out> runnable) {
        run((in, out) -> {
            T c = in.get(itemPlaceholder);
            runnable.accept(c, in, out);
        });
    }

    public <P1> void runCtx(Checkeds.CheckedThrowingBiConsumer<T, P1> runnable, P1 p1) {
        runCtx1(runnable, in -> p1);
    }

    public <P1> void runCtx(Checkeds.CheckedThrowingBiConsumer<T, P1> runnable, Placeholder<P1> p1) {
        runCtx1(runnable, in -> in.get(p1));
    }

    private <P1> void runCtx1(Checkeds.CheckedThrowingBiConsumer<T, P1> runnable, Function<In, P1> p1) {
        run(in -> {
            T c = in.get(itemPlaceholder);
            P1 p1Value = p1.apply(in);
            runnable.accept(c, p1Value);
        });
    }

    public <P1, P2> void runCtx(Checkeds.CheckedThrowingConsumer3<T, P1, P2> runnable, P1 p1, P2 p2) {
        runCtx2(runnable, in -> p1, in -> p2);
    }

    public <P1, P2> void runCtx(Checkeds.CheckedThrowingConsumer3<T, P1, P2> runnable, Placeholder<P1> p1, P2 p2) {
        runCtx2(runnable, in -> in.get(p1), in -> p2);
    }

    public <P1, P2> void runCtx(Checkeds.CheckedThrowingConsumer3<T, P1, P2> runnable, P1 p1, Placeholder<P2> p2) {
        runCtx2(runnable, in -> p1, in -> in.get(p2));
    }

    public <P1, P2> void runCtx(Checkeds.CheckedThrowingConsumer3<T, P1, P2> runnable, Placeholder<P1> p1,
                                Placeholder<P2> p2) {
        runCtx2(runnable, in -> in.get(p1), in -> in.get(p2));
    }

    private <P1, P2> void runCtx2(Checkeds.CheckedThrowingConsumer3<T, P1, P2> runnable, Function<In, P1> p1,
                                  Function<In, P2> p2) {
        run(in -> {
            T c = in.get(itemPlaceholder);
            runnable.accept(c, p1.apply(in), p2.apply(in));
        });
    }

    public <P1, P2, P3> void runCtx(Checkeds.CheckedThrowingConsumer4<T, P1, P2, P3> runnable, Placeholder<P1> p1,
                                    Placeholder<P2> p2, Placeholder<P3> p3) {
        runCtx3(runnable, in -> in.get(p1), in -> in.get(p2), in -> in.get(p3));
    }

    private <P1, P2, P3> void runCtx3(Checkeds.CheckedThrowingConsumer4<T, P1, P2, P3> runnable, Function<In, P1> p1,
                                      Function<In, P2> p2, Function<In, P3> p3) {
        run(in -> {
            T c = in.get(itemPlaceholder);
            runnable.accept(c, p1.apply(in), p2.apply(in), p3.apply(in));
        });
    }

    public <P1, P2, P3, P4> void runCtx(Checkeds.CheckedThrowingConsumer5<T, P1, P2, P3, P4> runnable, Placeholder<P1> p1,
                                        Placeholder<P2> p2, Placeholder<P3> p3, Placeholder<P4> p4) {
        runCtx4(runnable, in -> in.get(p1), in -> in.get(p2), in -> in.get(p3), in -> in.get(p4));
    }

    private <P1, P2, P3, P4> void runCtx4(Checkeds.CheckedThrowingConsumer5<T, P1, P2, P3, P4> runnable, Function<In, P1> p1,
                                          Function<In, P2> p2, Function<In, P3> p3, Function<In, P4> p4) {
        run(in -> {
            T c = in.get(itemPlaceholder);
            runnable.accept(c, p1.apply(in), p2.apply(in), p3.apply(in), p4.apply(in));
        });
    }
	
}
