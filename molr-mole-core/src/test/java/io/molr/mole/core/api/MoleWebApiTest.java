package io.molr.mole.core.api;

import org.assertj.core.api.Assertions;
import org.junit.Test;

@SuppressWarnings("static-method")
public class MoleWebApiTest {

    @Test
    public void missionRepresentationUrl() {
        Assertions.assertThat(MoleWebApi.MISSION_REPRESENTATION_PATH).isEqualTo("/mission/{missionName}/representation");
        Assertions.assertThat(MoleWebApi.missionRepresentationUrl("myMission")).isEqualTo("/mission/myMission/representation");
    }

    @Test
    public void missionParameterDesciptionUrl() {
        Assertions.assertThat(MoleWebApi.MISSION_PARAMETER_DESCRIPTION_PATH).isEqualTo("/mission/{missionName}/parameterDescription");
        Assertions.assertThat(MoleWebApi.missionParameterDescriptionUrl("myMission")).isEqualTo("/mission/myMission/parameterDescription");
    }

    @Test
    public void instructInstance() {
        Assertions.assertThat(MoleWebApi.INSTANCE_INSTRUCT_PATH).isEqualTo("/instance/{missionHandle}/{strandId}/instruct/{commandName}");
        Assertions.assertThat(MoleWebApi.instructInstance("myMissionHandle","myStrand","RUN")).isEqualTo("/instance/myMissionHandle/myStrand/instruct/RUN");
    }

}