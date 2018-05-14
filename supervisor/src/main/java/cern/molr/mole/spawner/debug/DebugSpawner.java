package cern.molr.mole.spawner.debug;


import cern.molr.commons.jvm.JvmSpawnHelper;
import cern.molr.inspector.domain.InstantiationRequest;
import cern.molr.inspector.domain.impl.InstantiationRequestImpl;
import cern.molr.inspector.json.MissionTypeAdapter;
import cern.molr.inspector.remote.SystemMain;
import cern.molr.mission.Mission;
import cern.molr.mole.supervisor.MoleSpawner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A spawner which creates debugging JVM on the mission
 * @author yassine
 */
public class DebugSpawner<I> implements MoleSpawner<I,DebugSession> {

    private static final String CURRENT_CLASSPATH_VALUE = System.getProperty("java.class.path");
    private static final String INSPECTOR_MAIN_CLASS = SystemMain.class.getName();
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Mission.class, new MissionTypeAdapter().nullSafe())
            .create();

    @Override
    public DebugSession spawnMoleRunner(Mission mission, I args) throws Exception {
        if(mission == null) {
            throw new IllegalArgumentException("The mission must not be null");
        }

        InstantiationRequest request = new InstantiationRequestImpl(CURRENT_CLASSPATH_VALUE, mission, args, args.getClass().getCanonicalName());
        String[] completedArgs = new String[1];
        completedArgs[0] = GSON.toJson(request);

        Process process = JvmSpawnHelper.getProcessBuilder(
                JvmSpawnHelper.appendToolsJarToClasspath(request.getClassPath()),
                INSPECTOR_MAIN_CLASS,
                completedArgs).start();
        Runtime.getRuntime().addShutdownHook(new Thread(process::destroy));

        return new DebugSession(mission, new DebugController(process));
    }
}
