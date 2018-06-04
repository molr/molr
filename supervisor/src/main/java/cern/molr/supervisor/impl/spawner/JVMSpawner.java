package cern.molr.supervisor.impl.spawner;


import cern.molr.commons.mission.Mission;
import cern.molr.supervisor.api.session.MissionSession;
import cern.molr.supervisor.api.spawner.MoleSpawner;
import cern.molr.supervisor.impl.session.ControllerImpl;
import cern.molr.supervisor.impl.session.MissionSessionImpl;
import cern.molr.supervisor.impl.session.runner.MoleRunner;
import cern.molr.supervisor.impl.session.runner.MoleRunnerArgument;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A spawner which creates a JVM that executes the mission
 *
 * @author yassine-kr
 */
public class JVMSpawner<I> implements MoleSpawner<I, MissionSession> {

    private static final String CURRENT_CLASSPATH_VALUE = System.getProperty("java.class.path");
    private static final String EXECUTOR_MAIN_CLASS = MoleRunner.class.getName();

    @Override
    public MissionSession spawnMoleRunner(Mission mission, I args) throws Exception {
        if (mission == null) {
            throw new IllegalArgumentException("The mission must not be null");
        }

        ObjectMapper mapper = new ObjectMapper();

        String missionObjString = mapper.writeValueAsString(mission);
        String missionInputObjString = mapper.writeValueAsString(args);

        MoleRunnerArgument argument =
                new MoleRunnerArgument(missionObjString, missionInputObjString,
                        args == null ? Object.class.getName() : args.getClass().getName());

        String[] completedArgs = new String[1];
        completedArgs[0] = mapper.writeValueAsString(argument);

        Process process = JvmSpawnHelper.getProcessBuilder(
                JvmSpawnHelper.appendToolsJarToClasspath(CURRENT_CLASSPATH_VALUE),
                EXECUTOR_MAIN_CLASS,
                completedArgs).start();
        Runtime.getRuntime().addShutdownHook(new Thread(process::destroy));

        return new MissionSessionImpl(mission, new ControllerImpl(process));
    }
}
