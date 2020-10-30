package io.molr.mole.core.runnable.lang.ctx;

import java.util.function.Function;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Out;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;
import io.molr.mole.core.runnable.lang.GenericOngoingLeaf;
import io.molr.mole.core.utils.Checkeds;

public class ContextualOngoingForeachLeaf<C, T> extends GenericOngoingLeaf<ContextualOngoingForeachLeaf<C, T>> {

	Placeholder<T> itemPlaceholder;
	Placeholder<C> contextPlaceholder;
	
	public ContextualOngoingForeachLeaf(String name, Builder builder, Block parent, Placeholder<C> context, Placeholder<T> item) {
		super(name, builder, parent);
		this.contextPlaceholder = context;
		this.itemPlaceholder = item;
	}

	private Placeholder<T> itemPlaceholder() {
		return itemPlaceholder;
	}
	
	private T item(In in) {
		return in.get(itemPlaceholder);
	}
	
	public void runCtxFor(Checkeds.CheckedThrowingBiConsumer<C, T> runnable) {
		run(in -> {
			C context = in.get(contextPlaceholder);
			T item = item(in);
			runnable.accept(context, item);
		});
	}
	
	public void runCtxFor(Checkeds.CheckedThrowingConsumer3<C, T, In> runnable) {
		run(in -> {
			C context = in.get(contextPlaceholder);
			T item = item(in);
			runnable.accept(context, item, in);
		});
	}
	
	public void runCtxFor(Checkeds.CheckedThrowingConsumer4<C, T, In, Out> runnable) {
		run((in, out) -> {
			C context = in.get(contextPlaceholder);
			T item = item(in);
			runnable.accept(context, item, in, out);
		});
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
            runnable.accept(item(in), in, out);
        });
    }
	
    // one arg
	public <P1> void runFor(Checkeds.CheckedThrowingBiConsumer<T, P1> runnable, P1 p1) {
        runFor1(runnable, in -> p1);
    }

	public <P1> void runFor(Checkeds.CheckedThrowingBiConsumer<T, P1> runnable, Placeholder<P1> p1) {
        runFor1(runnable, in -> in.get(p1));
    }

    //private  <P1> void runFor1(Checkeds.CheckedThrowingBiConsumer<T, P1> runnable, Function<In, P1> p1) {
	private <P1> void runFor1(Checkeds.CheckedThrowingBiConsumer<T, P1> runnable, Function<In, P1> p1) {
        run(in -> {
            P1 p1Value = p1.apply(in);
            runnable.accept(item(in), p1Value);
        });
    }

    // 2 args
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

    //private default <P1, P2> void runFor2(Checkeds.CheckedThrowingConsumer3<T, P1, P2> runnable, Function<In, P1> p1,
     //       Function<In, P2> p2) {
	private <P1, P2> void runFor2(Checkeds.CheckedThrowingConsumer3<T, P1, P2> runnable, Function<In, P1> p1,
            Function<In, P2> p2) {
        run(in -> {
            runnable.accept(item(in), p1.apply(in), p2.apply(in));
        });
    }
	
}
