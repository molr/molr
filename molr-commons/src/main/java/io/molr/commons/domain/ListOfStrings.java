package io.molr.commons.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Currently, the only collection in molar for which placeholders can be created.
 * 
 * @author krepp
 */
/*
 * TODO: This still has to be reviewed, if this is the ideal solution. In particular inheriting from List does not feel
 * good. Potentially, there is a solution using type token or similar...
 */
public class ListOfStrings extends ArrayList<String> implements MolrCollection<String> {

    private static final long serialVersionUID = 1L;

    public static final ListOfStrings empty() {
        /* As the list is mutable, it seems to be advisable to always return a new instance. */
        return new ListOfStrings();
    }

    /*
     * Default constructor required for gson.
     */
    public ListOfStrings() {
        super();
    }
    
    public ListOfStrings(Collection<String> values) {
        super(values);
    }

    public ListOfStrings(String... values) {
        super(Arrays.asList(values));
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        return true;
    }

    @Override
    public Placeholder<String> itemPlaceholder(String name) {
        return Placeholder.aString(name);
    }

}
