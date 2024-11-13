package telran.pulse.monitoring.common;

import java.util.logging.Level;

public interface Constants {
    String PARTITION_KEY_ATTR_NAME = "seqNumber";
    String PATIENTID_ATTR_NAME = "patientId";
    String TIMESTAMP_ATTR_NAME = "timestamp";
    String PULSE_VALUE_ATTR_NAME = "value";
    String LOGGING_LEVEL_ENV_VAR = "LOGGING_LEVEL";
    Level DEFAULT_LOGGING_LEVEL = Level.INFO;
}
