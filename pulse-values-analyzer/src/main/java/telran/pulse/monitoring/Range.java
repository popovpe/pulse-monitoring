package telran.pulse.monitoring;
import org.json.*;

public record Range(int min, int max) {
    final private static String MIN_ATTR_NAME = "min";
    final private static String MAX_ATTR_NAME = "max";

    public static Range from(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            Integer min = jsonObject.getInt(MIN_ATTR_NAME);
            Integer max = jsonObject.getInt(MAX_ATTR_NAME);
            return new Range(min, max);
        } catch (JSONException e) {
            throw new IllegalArgumentException(String.format("Impossible to construct Range from json %s", json));
        }
    }
    
    public String getRangeJSON() {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(MIN_ATTR_NAME, min);
        jsonObj.put(MAX_ATTR_NAME, max);
        return jsonObj.toString();
    }

    public boolean notInRange( int value ) {
        return value < min || value > max;
    }

}
