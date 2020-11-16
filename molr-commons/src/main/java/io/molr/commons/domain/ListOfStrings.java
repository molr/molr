package io.molr.commons.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author krepp
 */
public class ListOfStrings extends ArrayList<String> implements MolrCollection<String>{

    private static final long serialVersionUID = 1L;

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
	
	/*
	 * public static Placeholder<String> itemPlaceholderS(String name) { return
	 * Placeholder.aString(name); }
	 */

	

}

