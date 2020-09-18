package io.molr.mole.core.utils.function;

@FunctionalInterface
public interface Consumer3<P1, P2, P3> {

    void accept(P1 p1, P2 p2, P3 p3);

}
