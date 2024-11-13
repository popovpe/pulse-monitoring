package telran.pulse.monitoring;

interface Constants {
    String LAST_VALUE_TABLE_NAME = "pulse-last-value";
    String JUMP_VALUE_TABLE_NAME = "pulse-jump-values";
    String PREV_VALUE_ATTR_JUMP_VALUE_TABLE = "previousValue";
    String CURR_VALUE_ATTR_JUMP_VALUE_TABLE = "currentValue";
    String FACTOR_ENV_VAR = "FACTOR";
    float DEFAULT_FACTOR = 0.2f;
}
