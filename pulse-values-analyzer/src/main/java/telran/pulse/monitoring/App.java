package telran.pulse.monitoring;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import org.json.JSONObject;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverterProvider;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableMetadata;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument;

import java.util.Map;
import static telran.pulse.monitoring.Constants.*;
import static telran.pulse.monitoring.AppLogging.logger;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

public class App {




	private static final DynamoDbEnhancedClient DYNAMODB_CLIENT = DynamoDbEnhancedClient.builder().build();
	private static final DynamoDbTable<EnhancedDocument> DYNAMODB_TABLE = DYNAMODB_CLIENT.table(PULSE_DATA_TABLE_NAME,
			TableSchema.documentSchemaBuilder()
					.addIndexPartitionKey(TableMetadata.primaryIndexName(), PARTITION_KEY_ATTR_NAME,
							AttributeValueType.N)
					.attributeConverterProviders(AttributeConverterProvider.defaultProvider())
					.build());

	public void handleRequest(final DynamodbEvent event, final Context context) {
		
		event.getRecords().forEach(r -> {
			var map = r.getDynamodb().getNewImage();
			int pulse = Integer.parseInt(map.get(PULSE_VALUE_ATTR_NAME).getN());
			if (map != null) {
				logger.finer(() -> {
					return String.format("Getting pulse. Patient ID = %s, timestamp = %s, pulse = %d%n",
							map.get(PATIENTID_ATTR_NAME).getN(),
							map.get(TIMESTAMP_ATTR_NAME).getN(), pulse);
				});
				if (pulse < MIN_NORMAL_VALUE || pulse > MAX_NORMAL_VALUE) {
					registerAbnormalPulse(map);
				}

			}
		});
	}

	private void registerAbnormalPulse(Map<String, AttributeValue> map) {
		logger.info(String.format("Abnormal pulse. Patient ID = %s, pulse = %s%n", map.get(PATIENTID_ATTR_NAME).getN(),
				map.get(PULSE_VALUE_ATTR_NAME).getN()));
		JSONObject jsonObject = getJSONFromMap(map);
		try {
			DYNAMODB_TABLE.putItem(EnhancedDocument.fromJson(jsonObject.toString()));
		} catch (DynamoDbException e) {
			logger.severe(String.format("Exception occured while putting new item:\n%s", e.getMessage()));
		}
	}

	private static JSONObject getJSONFromMap(Map<String, AttributeValue> map) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(PARTITION_KEY_ATTR_NAME, Long.parseLong(map.get(PARTITION_KEY_ATTR_NAME).getN()));
		jsonObject.put(PATIENTID_ATTR_NAME, Long.parseLong(map.get(PATIENTID_ATTR_NAME).getN()));
		jsonObject.put(TIMESTAMP_ATTR_NAME, Long.parseLong(map.get(TIMESTAMP_ATTR_NAME).getN()));
		jsonObject.put(PULSE_VALUE_ATTR_NAME, Integer.parseInt(map.get(PULSE_VALUE_ATTR_NAME).getN()));
		return jsonObject;
	}
}