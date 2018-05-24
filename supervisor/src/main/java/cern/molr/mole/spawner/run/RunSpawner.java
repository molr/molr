package cern.molr.mole.spawner.run;


import cern.molr.commons.jvm.JvmSpawnHelper;
import cern.molr.mission.Mission;
import cern.molr.mole.spawner.run.jvm.MoleRunner;
import cern.molr.mole.spawner.run.jvm.MoleRunnerArgument;
import cern.molr.mole.supervisor.MoleSpawner;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A spawner which creates a JVM that executes the mission
 * @author yassine-kr
 */
public class RunSpawner<I> implements MoleSpawner<I,RunSession> {

    private static final String CURRENT_CLASSPATH_VALUE = System.getProperty("java.class.path");
    private static final String EXECUTOR_MAIN_CLASS = MoleRunner.class.getName();

    @Override
    public RunSession spawnMoleRunner(Mission mission, I args) throws Exception {
        if(mission == null) {
            throw new IllegalArgumentException("The mission must not be null");
        }

        ObjectMapper mapper = new ObjectMapper();

        String missionObjString = mapper.writeValueAsString(mission);
        String missionInputObjString = mapper.writeValueAsString(args);

        MoleRunnerArgument argument =
                new MoleRunnerArgument(missionObjString, missionInputObjString,
                        args==null?Object.class.getName():args.getClass().getName());

        String[] completedArgs = new String[1];
        completedArgs[0] = mapper.writeValueAsString(argument);

        Process process = JvmSpawnHelper.getProcessBuilder(
                JvmSpawnHelper.appendToolsJarToClasspath(CURRENT_CLASSPATH_VALUE),
                EXECUTOR_MAIN_CLASS,
                completedArgs).start();
        Runtime.getRuntime().addShutdownHook(new Thread(process::destroy));

        return new RunSession(mission, new RunController(process));
    }
}
