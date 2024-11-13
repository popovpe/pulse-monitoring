package telran.pulse.monitoring;

import telran.pulse.monitoring.common.Range;

public interface RangeProvider {
    Range getRange( String patiendId );
}
