package io.molr.commons.domain;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;

public interface MolrCollection<T> extends Collection<T>{
	
	Placeholder<T> itemPlaceholder(String name);

	static <T> Placeholder<T> itemPlaceholderForCollectionPlaceholder(Placeholder<? extends MolrCollection<T>> collectionPlaceholder, String name) {
        for(Constructor<?> constructor : collectionPlaceholder.type().getDeclaredConstructors()) {
        	try {
				if(constructor.getParameterCount()==0) {
					try {
						@SuppressWarnings("unchecked")
						MolrCollection<T> instance = (MolrCollection<T>)constructor.newInstance();
						Placeholder<T> itemPlaceholder = instance.itemPlaceholder(name);
						return itemPlaceholder;
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch ( SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        };
        
//        for(Method method : collectionPlaceholder.type().getMethods()) {
//        	System.out.println(method.getName());
//        	if(method.getName().equals("itemPlaceholderS")) {
//        		try {
//					System.out.println(method.invoke(null, "foreachPlaceholder"));
//				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//        	}
//        }
//        System.out.println(collectionPlaceholder.type().getDeclaredMethods().length);
//        System.out.println(collectionPlaceholder.getClass().getTypeParameters()[0]);
//        ParameterizedType type = (ParameterizedType)collectionPlaceholder.type().getGenericSuperclass();
//        System.out.println(type.getActualTypeArguments()[0]);
//        ArrayList<String> strings = new ArrayList<>();
//        ParameterizedType typeOfStrings = (ParameterizedType) strings.getClass().getGenericSuperclass();
//        System.out.println(typeOfStrings.getActualTypeArguments().length);
//        System.out.println(typeOfStrings.getActualTypeArguments()[0]);
//        System.out.println(strings.getClass().getTypeParameters()[0]);
        
        throw new IllegalStateException("Cannot create item placeholder since default constructor for enclosing collection is not availabe");
	}
	
}
