package telran.pulse.monitoring;

public interface Constants {
    String PARTITION_KEY_ATTR_NAME = "seqNumber";
    String PULSE_DATA_TABLE_NAME = "pulse-abnormal-values";
    String PATIENTID_ATTR_NAME = "patientId";
    String TIMESTAMP_ATTR_NAME = "timestamp";
    String PULSE_VALUE_ATTR_NAME = "value";
    String RANGE_PROVIDER_ENVAR_NAME = "RANGE_PROVIDER_API";
    int MIN_NORMAL_VALUE = 50;
    int MAX_NORMAL_VALUE = 190;
}
