package org.molr.mole.core.single;

import com.google.common.collect.ImmutableSet;
import com.sun.org.omg.CORBA.ParameterDescription;
import org.molr.commons.domain.*;
import org.molr.mole.core.utils.Checkeds;

import java.lang.reflect.Parameter;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static org.molr.commons.domain.MissionParameter.required;

public class SingleNodeMission<R> {

    private final String name;
    private final Checkeds.CheckedThrowingBiFunction<In, Out, R> executable;
    private final Class<R> returnType;
    private final MissionParameterDescription parameterDescription;

    public SingleNodeMission(Class<R> returnType, Checkeds.CheckedThrowingBiFunction<In, Out, R> executable, String name, MissionParameterDescription parameterDescription) {
        this.name = requireNonNull(name, "name must not be null");
        this.executable = requireNonNull(executable, "executable must not be null");
        this.returnType = requireNonNull(returnType, "returnType must not be null");
        this.parameterDescription = requireNonNull(parameterDescription, "parameterDescription must not be null");
    }



    public static <R> SingleNodeMission<R> from(Class<R> returnType, Checkeds.CheckedThrowingBiFunction<In, Out, R> executable) {
        return new SingleNodeMission<>(returnType, executable, Objects.toString(executable), MissionParameterDescription.empty());
    }

    public static <R> SingleNodeMission<R> from(Class<R> returnType, BiFunction<In, Out, R> executable) {
        return from(returnType, (in, out) -> executable.apply(in, out));
    }



    public static <R> SingleNodeMission<R> from(Class<R> returnType, Checkeds.CheckedThrowingFunction<In, R> executable) {
        return from(returnType, (in, out) -> executable.apply(in));
    }

    public static <R> SingleNodeMission<R> from(Class<R> returnType, Function<In, R> executable) {
        return from(returnType, (in, out) -> executable.apply(in));
    }



    public static <R> SingleNodeMission<R> from(Class<R> returnType, Checkeds.CheckedThrowingCallable<R> executable) {
        return from(returnType, (in, out) -> executable.call());
    }

    public static <R> SingleNodeMission<R> from(Class<R> returnType, Callable<R> executable) {
        return from(returnType, (in, out) -> executable.call());
    }


    public static SingleNodeMission<Void> from(Checkeds.CheckedThrowingBiConsumer<In, Out> executable) {
        return from(Void.class, (in, out) -> {
            executable.accept(in, out);
            return null;
        });
    }

    public static SingleNodeMission<Void> from(BiConsumer<In, Out> executable) {
        return from(Void.class, (in, out) -> {
            executable.accept(in, out);
            return null;
        });
    }


    public static SingleNodeMission<Void> from(Checkeds.CheckedThrowingConsumer<In> executable) {
        return from((in, out) -> executable.accept(in));
    }

    public static SingleNodeMission<Void> from(Consumer<In> executable) {
        return from((in, out) -> executable.accept(in));
    }


    public static SingleNodeMission<Void> from(Checkeds.CheckedThrowingRunnable executable) {
        return from((in, out) -> executable.run());
    }

    public static SingleNodeMission<Void> from(Runnable executable) {
        return from((in, out) -> executable.run());
    }


    public static <P1> SingleNodeMission<Void> from(Checkeds.CheckedThrowingConsumer<P1> executable, Placeholder<P1> param1) {
        return from((in, out) -> executable.accept(in.get(param1))).withParameters(required(param1));
    }

    public static <P1> SingleNodeMission<Void> from(Consumer<P1> executable, Placeholder<P1> param1) {
        return from((in, out) -> executable.accept(in.get(param1))).withParameters(required(param1));
    }


    public static <P1, P2> SingleNodeMission<Void> from(Checkeds.CheckedThrowingBiConsumer<P1, P2> executable, Placeholder<P1> param1, Placeholder<P2> param2) {
        return from((in, out) -> executable.accept(in.get(param1), in.get(param2))).withParameters(required(param1), required(param2));
    }

    public static <P1, P2> SingleNodeMission<Void> from(BiConsumer<P1, P2> executable, Placeholder<P1> param1, Placeholder<P2> param2) {
        return from((in, out) -> executable.accept(in.get(param1), in.get(param2))).withParameters(required(param1), required(param2));
    }


    public static <P1, P2, R> SingleNodeMission<R> from(Class<R> returnType, Checkeds.CheckedThrowingBiFunction<P1, P2, R> executable, Placeholder<P1> param1, Placeholder<P2> param2) {
        return from(returnType, (in, out) -> executable.apply(in.get(param1), in.get(param2))).withParameters(required(param1), required(param2));
    }

    public static <P1, P2, R> SingleNodeMission<R> from(Class<R> returnType, BiFunction<P1, P2, R> executable, Placeholder<P1> param1, Placeholder<P2> param2) {
        return from(returnType, (in, out) -> executable.apply(in.get(param1), in.get(param2))).withParameters(required(param1), required(param2));
    }


    public static <P1, R> SingleNodeMission<R> from(Class<R> returnType, Checkeds.CheckedThrowingFunction<P1, R> executable, Placeholder<P1> param1) {
        return from(returnType, (in, out) -> executable.apply(in.get(param1))).withParameters(required(param1));
    }

    public static <P1, R> SingleNodeMission<R> from(Class<R> returnType, Function<P1, R> executable, Placeholder<P1> param1) {
        return from(returnType, (in, out) -> executable.apply(in.get(param1))).withParameters(required(param1));
    }


    public SingleNodeMission<R> withParameters(Set<MissionParameter<?>> parameters) {
        return new SingleNodeMission<>(this.returnType, this.executable, this.name, new MissionParameterDescription(parameters));
    }

    public SingleNodeMission<R> withParameters(MissionParameter<?>... parameters) {
        return withParameters(ImmutableSet.copyOf(parameters));
    }

    public SingleNodeMission<R> withName(String newName) {
        return new SingleNodeMission<>(this.returnType, this.executable, newName, this.parameterDescription);
    }

    public String name() {
        return name;
    }

    public Class<R> returnType() {
        return this.returnType;
    }

    public MissionParameterDescription parameterDescription() {
        return this.parameterDescription;
    }

    public Checkeds.CheckedThrowingBiFunction<In, Out, R> executable() {
        return this.executable;
    }

    @Override
    public String toString() {
        return "SingleNodeMission{" +
                "name='" + name + '\'' +
                ", executable=" + executable +
                ", returnType=" + returnType +
                ", parameterDescription=" + parameterDescription +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SingleNodeMission<?> that = (SingleNodeMission<?>) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(executable, that.executable) &&
                Objects.equals(returnType, that.returnType) &&
                Objects.equals(parameterDescription, that.parameterDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, executable, returnType, parameterDescription);
    }
}
