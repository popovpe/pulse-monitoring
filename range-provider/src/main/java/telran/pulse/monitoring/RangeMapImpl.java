package telran.pulse.monitoring;

import java.util.HashMap;

public class RangeMapImpl implements RangeProvider {

      static HashMap<String, Range> ranges = new HashMap<>() {
        {
            put("1", new Range(60, 150));
            put("2", new Range(70, 160));
            put("3", new Range(50, 250));

        }
    };
    static RangeProvider rangeProvider = new RangeMapImpl();

    @Override
    public Range getRange(String patientId) {
        return ranges.get(patientId);
    }
    
}
