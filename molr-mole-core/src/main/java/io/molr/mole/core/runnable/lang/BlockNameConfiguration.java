package io.molr.mole.core.runnable.lang;

import java.util.List;

import com.google.common.collect.ImmutableList;

import io.molr.commons.domain.Placeholder;
import io.molr.commons.domain.Placeholders;

public class BlockNameConfiguration {
	
	private final String name;
	private final List<Placeholder<?>> placeholders;
	
	private BlockNameConfiguration(String text, Placeholder<?>... placeholders) {
		this.name=text;
		this.placeholders = placeholders==null?ImmutableList.of():ImmutableList.copyOf(placeholders);
	}

	public String text() {
		return name;
	}
	
	public List<Placeholder<?>> placeholders(){
		return this.placeholders;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public final static class Builder{
		
		private String name;
		private Placeholder<?> foreachItemPlaceholder;
		private Placeholder<?>[] formatterAruments;
		
		private Builder() {
			
		}
		
		public static Builder builder() {
			return new Builder();
		}
		
		public BlockNameConfiguration build() {
			if(name == null) {
				throw new IllegalArgumentException("Name must not be null");
			}
			if(formatterAruments!=null) {
				Placeholder<?>[] updatedPlaceholders = new Placeholder<?>[formatterAruments.length];
				if(this.foreachItemPlaceholder!=null) {
					for (int i = 0; i < updatedPlaceholders.length; i++) {
						if(formatterAruments[i].equals(Placeholders.LATEST_FOREACH_ITEM_PLACEHOLDER)) {
							updatedPlaceholders[i] = this.foreachItemPlaceholder;
						}
						else {
							updatedPlaceholders[i] = formatterAruments[i];
						}
					}
					return new BlockNameConfiguration(name, updatedPlaceholders);
				}
				return new BlockNameConfiguration(name, this.formatterAruments);
			}
			return new BlockNameConfiguration(name);
		}
		
		public Builder text(String newText) {
			this.name = newText;
			return this;
		}
		
		public Builder foreachItemPlaceholder(Placeholder<?> itemPlaceholder) {
			this.foreachItemPlaceholder = itemPlaceholder;
			return this;
		}
		
		public Builder formatterPlaceholders(Placeholder<?>[] placeholders) {
			this.formatterAruments = placeholders;
			return this;
		}
		
	}
}
