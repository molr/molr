package io.molr.demo.missions;

import org.springframework.context.annotation.Bean;

import io.molr.commons.domain.ListOfStrings;
import io.molr.commons.domain.Placeholder;
import io.molr.commons.domain.Placeholders;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.RunnableLeafsMissionSupport;

public class ForeachMissionDemos {

	@Bean
	RunnableLeafsMission printGivenListElements() {
		return new RunnableLeafsMissionSupport() {
			{
				Placeholder<ListOfStrings> listElementsPlaceholder = mandatory(Placeholder.aListOfStrings("someElements"));
				
				root("Print List Elements").foreach(listElementsPlaceholder).branch("Print {}", Placeholders.LATEST_FOREACH_ITEM_PLACEHOLDER)
				.as((branchDescription, itemPlaceholder)->{
					branchDescription.leaf("Print Item").runCtx(item->{
						System.out.println("devices:"+item);
					});
				});
			}
		}.build();
	}
	
}
