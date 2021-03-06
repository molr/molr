package io.molr.mole.core.runnable.lang;

import static java.util.Objects.requireNonNull;
import java.util.function.Function;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Out;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;
import io.molr.mole.core.utils.Checkeds;

public class OngoingForeachLeaf<T> extends GenericOngoingLeaf<OngoingForeachLeaf<T>> {

	Placeholder<T> itemPlaceholder;
	
	public OngoingForeachLeaf(BlockNameConfiguration name, Builder builder, Block parent, Placeholder<T> item) {
		super(name, builder, parent);
		requireNonNull(item);
		this.itemPlaceholder = item;
	}

	private Placeholder<T> itemPlaceholder() {
		return itemPlaceholder;
	}
	
	private T item(In in, Placeholder<T> itemPlaceholder) {
		return in.get(itemPlaceholder);
	}

	public void runFor(Checkeds.CheckedThrowingConsumer<T> runnable) {
        run(in -> {
            T i = in.get(itemPlaceholder());
            runnable.accept(i);
        });
    }

	public void runFor(Checkeds.CheckedThrowingBiConsumer<T, In> runnable) {
        run(in -> {
            T item = in.get(itemPlaceholder());
            runnable.accept(item, in);
        });
    }

	public void runFor(Checkeds.CheckedThrowingConsumer3<T, In, Out> runnable) {
        run((in, out) -> {
            runnable.accept(item(in, itemPlaceholder()), in, out);
        });
    }

	public <P1> void runFor(Checkeds.CheckedThrowingBiConsumer<T, P1> runnable, P1 p1) {
        runFor1(runnable, in -> p1);
    }

	public <P1> void runFor(Checkeds.CheckedThrowingBiConsumer<T, P1> runnable, Placeholder<P1> p1) {
        runFor1(runnable, in -> in.get(p1));
    }

	private <P1> void runFor1(Checkeds.CheckedThrowingBiConsumer<T, P1> runnable, Function<In, P1> p1) {
        run(in -> {
            P1 p1Value = p1.apply(in);
            runnable.accept(item(in, itemPlaceholder()), p1Value);
        });
    }

	public <P1, P2> void runFor(Checkeds.CheckedThrowingConsumer3<T, P1, P2> runnable, P1 p1, P2 p2) {
        runFor2(runnable, in -> p1, in -> p2);
    }

	public <P1, P2> void runFor(Checkeds.CheckedThrowingConsumer3<T, P1, P2> runnable, Placeholder<P1> p1, P2 p2) {
        runFor2(runnable, in -> in.get(p1), in -> p2);
    }

	public <P1, P2> void runFor(Checkeds.CheckedThrowingConsumer3<T, P1, P2> runnable, P1 p1, Placeholder<P2> p2) {
        runFor2(runnable, in -> p1, in -> in.get(p2));
    }

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
	
}
