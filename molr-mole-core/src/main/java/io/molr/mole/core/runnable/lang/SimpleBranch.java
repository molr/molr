package io.molr.mole.core.runnable.lang;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.MolrCollection;
import io.molr.commons.domain.Out;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static io.molr.mole.core.runnable.lang.BranchMode.SEQUENTIAL;

public class SimpleBranch extends AbstractBranch {

    protected SimpleBranch(RunnableLeafsMission.Builder builder, Block parent) {
        super(builder, parent);
    }

    static SimpleBranch withParent(RunnableLeafsMission.Builder builder, Block parent) {
        return new SimpleBranch(builder, parent);
    }

    @Override
    public OngoingSimpleBranch branch(String name) {
        return new OngoingSimpleBranch(name, builder(), parent(), SEQUENTIAL);
    }

    @Override
    public OngoingSimpleLeaf leaf(String name) {
        return new OngoingSimpleLeaf(name, builder(), parent());
    }

    @Deprecated
    public void sequential(String name, Consumer<SimpleBranch> branchDefiner) {
        this.branch(name).sequential().as(branchDefiner);
    }

    @Deprecated
    public void parallel(String name, Consumer<SimpleBranch> branchDefiner) {
        branch(name).parallel().as(branchDefiner);
    }

    /*
     * just for demonstration
     * TODO rename, restructure, parallel/sequential
     */
    public <T,U> void leafForEach(String name, Placeholder<T> devicesPlaceholder, Placeholder<U> itemPlaceholder, BiConsumer<In, Out> itemConsumer) {
        builder().forEach(name, parent(), devicesPlaceholder,itemPlaceholder,itemConsumer);        
    }
    
    
    public <T> ForeachBranchRoot<T> foreach(Placeholder<? extends MolrCollection<T>> itemsPlaceholder, String name) {
        return new ForeachBranchRoot<>(name, builder(), parent(), BranchMode.SEQUENTIAL, itemsPlaceholder);
    }




}
