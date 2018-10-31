import org.molr.commons.domain.StrandCommand;
import org.molr.mole.core.runnable.RunnableLeafsMission;
import org.molr.mole.core.runnable.exec.RunnableBlockExecutor;
import org.molr.mole.core.runnable.lang.RunnableBranchSupport;
import org.molr.mole.core.runnable.lang.RunnableMissionSupport;
import org.molr.mole.core.tree.LeafExecutor;
import org.molr.mole.core.tree.TreeMissionExecutor;
import org.molr.mole.core.tree.TreeResultTracker;
import org.molr.mole.core.tree.TreeStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.molr.mole.core.utils.TreeUtils.logResultsOf;

public class SequenceTry {

    private static final Logger LOGGER = LoggerFactory.getLogger(SequenceTry.class);

    public static void main(String[] args) throws IOException {

        RunnableLeafsMission data = new RunnableMissionSupport() {
            {
                mission("Root", root -> {

                    root.sequential("First", b -> {
                        b.run(log("First A"));
                        b.run(log("First B"));
                    });

                    root.sequential("Second", b -> {
                        b.run(log("second A"));
                        b.run(log("second B"));
                    });

                    root.run(log("Third"));

                    root.parallel("Parallel", b -> {
                        b.run(log("Parallel A"));
                        b.run(log("parallel B"));
                    });

                });
            }
        }.build();


        TreeStructure treeStructure = data.treeStructure();
        TreeResultTracker resultTracker = new TreeResultTracker(treeStructure.missionRepresentation());
        LeafExecutor leafExecutor = new RunnableBlockExecutor(resultTracker, data.runnables());
        TreeMissionExecutor mission = new TreeMissionExecutor(treeStructure, leafExecutor, resultTracker);

        mission.instruct(mission.getRootStrand(), StrandCommand.STEP_INTO);
        mission.instruct(mission.getRootStrand(), StrandCommand.STEP_OVER);
        mission.instruct(mission.getRootStrand(), StrandCommand.STEP_OVER);
        mission.instruct(mission.getRootStrand(), StrandCommand.STEP_OVER);
        // mission.instruct(mission.getRootStrand(), StrandCommand.STEP_OVER);
//        mission.instruct(null, StrandCommand.RESUME);
//        mission.instruct(mission.getRootStrand(), StrandCommand.STEP_INTO);
//        mission.instruct(mission.getRootStrand(), StrandCommand.STEP_OVER);
//        mission.instruct(mission.getRootStrand(), StrandCommand.STEP_OVER);

//        mission.instruct(mission.getRootStrand(), StrandCommand.STEP_OVER);
//        mission.instruct(mission.getRootStrand(), StrandCommand.STEP_OVER);
//        mission.instruct(mission.getRootStrand(), StrandCommand.STEP_OVER);

        logResultsOf(resultTracker, treeStructure);
    }

    private static RunnableBranchSupport.Task log(String text) {
        return new RunnableBranchSupport.Task(text, () -> LOGGER.info("{} executed", text));
    }


}
