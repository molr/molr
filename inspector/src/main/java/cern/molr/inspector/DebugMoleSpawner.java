package cern.molr.inspector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cern.molr.commons.jvm.JvmSpawnHelper;
import cern.molr.inspector.controller.StatefulJdiControllerImpl;
import cern.molr.inspector.domain.InstantiationRequest;
import cern.molr.inspector.domain.StepSession;
import cern.molr.inspector.domain.impl.InstantiationRequestImpl;
import cern.molr.inspector.domain.impl.StepSessionImpl;
import cern.molr.inspector.json.MissionTypeAdapter;
import cern.molr.inspector.remote.SystemMain;
import cern.molr.mission.Mission;
import cern.molr.mole.supervisor.MoleSpawner;

/**
 * @author timartin
 * @author yassine-kr
 */
public class DebugMoleSpawner<I,T> implements MoleSpawner<I,StepSession> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DebugMoleSpawner.class);
    private static final String CURRENT_CLASSPATH_VALUE = System.getProperty("java.class.path");
    private static final String INSPECTOR_MAIN_CLASS = SystemMain.class.getName();

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Mission.class, new MissionTypeAdapter().nullSafe())
            .create();

    @Override
    public StepSession spawnMoleRunner(Mission mission, I args) throws Exception {
        if(mission == null) {
            throw new IllegalArgumentException("The mission must not be null");
        }
        InstantiationRequest request = new InstantiationRequestImpl(CURRENT_CLASSPATH_VALUE, mission, args, args.getClass().getCanonicalName());
        String[] completedArgs = new String[1];
        completedArgs[0] = GSON.toJson(request);

        Process process = JvmSpawnHelper.getProcessBuilder(
                JvmSpawnHelper.appendToolsJarToClasspath(request.getClassPath()),
                INSPECTOR_MAIN_CLASS,
                completedArgs).inheritIO().start();
        Runtime.getRuntime().addShutdownHook(new Thread(process::destroy));

        return new StepSessionImpl(mission, new StatefulJdiControllerImpl(process));
    }
}
