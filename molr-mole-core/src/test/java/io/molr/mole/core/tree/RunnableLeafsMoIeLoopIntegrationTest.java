package io.molr.mole.core.tree;


import com.google.common.collect.Sets;

import io.molr.commons.domain.ExecutionStrategy;
import io.molr.commons.domain.ListOfStrings;
import io.molr.commons.domain.Placeholder;
import io.molr.commons.domain.Placeholders;

import io.molr.mole.core.api.Mole;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.RunnableLeafsMole;
import io.molr.mole.core.runnable.lang.RunnableLeafsMissionSupport;

/**
 * @author krepp
 */
public class RunnableLeafsMoIeLoopIntegrationTest {

    RunnableLeafsMission mission() {

        return new RunnableLeafsMissionSupport() {
            {

                Placeholder<ListOfStrings> collectionPlaceholder = mandatory(Placeholder.aListOfStrings("deviceNames"));
                Placeholder<String> itemPlaceholder = Placeholder.aString("aForEachLoop.deviceName");
                optional(Placeholders.EXECUTION_STRATEGY, ExecutionStrategy.ABORT_ON_ERROR.name());
                
                root("root1").sequential().as(missionRoot -> {// 0
                    
                    missionRoot.leafForEach("aForEachLoop", collectionPlaceholder, itemPlaceholder, (in, out) -> {
                        String deviceName = in.get(itemPlaceholder);
                        System.out.println("deviceName: " + deviceName);
                    });
                    
                    missionRoot.branch("main1").parallel().as(main1Root -> {// 1

                        main1Root.leaf("main1Sub1").run(() -> {// 2
                            System.out.println("main1Sub1");
                        });

                        main1Root.branch("main1Sub2").sequential().as(main1Sub2 -> {// 3

                            main1Sub2.leaf("main1Sub2Sub1").run(() -> {// 4
                                System.out.println("main1Sub2Task1");
                            });

                            main1Sub2.leaf("main1Sub2Sub2").run(() -> {// 5
                                System.out.println("main1Sub2Task2");
                            });

                            main1Sub2.leaf("main1Sub2Sub3").run(() -> {// 6
                                System.out.println("main1Sub2Task3");
                            });
                        });

                        main1Root.leaf("main1Sub3").run(() -> {// 7
                            System.out.println("main1Sub1");
                        });
                    });

                    missionRoot.leaf("main2").run((in, out) -> {// 8
                        System.out.println("hello1");
                    });
                    missionRoot.leaf("main3").run((in, out) -> {// 9
                        System.out.println("hello2");
                    });
                });
            }
        }.build();

    }

    Mole testMoole() {
        return new RunnableLeafsMole(Sets.newHashSet(mission()));
    }

}
