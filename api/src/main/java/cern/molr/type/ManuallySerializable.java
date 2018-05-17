package cern.molr.type;

import java.util.Map;

/**
 * Classes which has to be manually serializable. If the automatic serialization using Jackson library fails, the manual serialization is used
 * @author yassine
 */
public interface ManuallySerializable {

    /**
     * Method which returns the map representing the json object.
     * @return
     */
    Map<String,String> getJsonMap();

    /**
     * Serialization method which writes (as an array of two values) the object class name before writing the json map.
     * This way of serialization is an option of Jackson library.
     * @param object
     * @return
     */
    static String serializeArray(ManuallySerializable object){
        StringBuilder json=new StringBuilder("[\"").append(object.getClass().getCanonicalName()).append("\",{");
        object.getJsonMap().forEach((key,value)->{
            json.append(key).append(":").append(value).append(",");
        });
        return json.deleteCharAt(json.length()-1).append("}]").toString();
    }
}
