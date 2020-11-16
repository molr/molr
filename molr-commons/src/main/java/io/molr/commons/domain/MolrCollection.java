package io.molr.commons.domain;

import java.util.Collection;

public interface MolrCollection<T> extends Collection<T>{
	
	Placeholder<T> itemPlaceholder(String name);

}
