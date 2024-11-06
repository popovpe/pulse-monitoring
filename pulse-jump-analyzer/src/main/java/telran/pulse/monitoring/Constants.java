package telran.pulse.monitoring;

import java.util.logging.Level;

interface Constants {
    String PATIENT_ID_ATTR= "patientId";
    String LAST_VALUE_TABLE_NAME = "pulse-last-value";
    String JUMP_VALUE_TABLE_NAME = "pulse-jump-values";
    String TIMESTAMP_ATTR = "timestamp";
    String PREV_VALUE_ATTR_JUMP_VALUE_TABLE = "previousValue";
    String CURR_VALUE_ATTR_JUMP_VALUE_TABLE = "currentValue";
    String PULSE_ATTR_NAME = "value";
    String FACTOR_ENV_VAR = "FACTOR";
    float DEFAULT_FACTOR = 0.2f;
    String LOGGING_LEVEL_ENV_VAR = "LOGGING_LEVEL";
    Level DEFAULT_LOGGING_LEVEL = Level.INFO;
}
