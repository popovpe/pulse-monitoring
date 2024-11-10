package telran.pulse.monitoring;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;
import static telran.pulse.monitoring.Constants.*;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;

@DynamoDbBean
public class PulseData {

    private Long patientId;
    private Integer value;
    private Long timestamp;
    
    public PulseData() {
    	
    }
    
    public PulseData(Map<String, AttributeValue> map) {
        AttributeValue patientIdAttr = getAttributeValue(map, PATIENTID_ATTR_NAME);
        AttributeValue timestampIdAttr = getAttributeValue(map, TIMESTAMP_ATTR_NAME);
        AttributeValue valueAttr = getAttributeValue(map, PULSE_VALUE_ATTR_NAME);
        this.patientId = Long.parseLong(patientIdAttr.getN());
        this.timestamp = Long.parseLong(timestampIdAttr.getN());
        this.value = Integer.parseInt(valueAttr.getN());
    }

    private AttributeValue getAttributeValue(Map<String, AttributeValue> map, String attributeName) {
        AttributeValue attributeValue = map.get(attributeName);
        if (attributeValue == null) {
            throw new IllegalStateException(String.format("Attribute %s is absent in NewImage map", attributeName));
        }
        return attributeValue;
    }

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
    
    @DynamoDbSortKey
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
    
}
