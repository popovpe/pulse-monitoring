package telran.pulse.monitoring;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class LastValue {

    private Long patientId;
    private Integer value;

    @DynamoDbPartitionKey
    public Long getPatientId() {
        return patientId;
    }
    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }
    public Integer getValue() {
        return value;
    }
    public void setValue(Integer number) {
        this.value = number;
    }
}