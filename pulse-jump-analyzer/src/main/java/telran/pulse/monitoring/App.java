package telran.pulse.monitoring;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverterProvider;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableMetadata;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import static telran.pulse.monitoring.common.AppLogging.logger;

import static telran.pulse.monitoring.Constants.*;
import static telran.pulse.monitoring.common.Constants.*;
import static telran.pulse.monitoring.common.Util.processInsertedItemsFromDDBEvent;



public class App {

	static LastValue lastValue = new LastValue();

	private static final DynamoDbEnhancedClient DYNAMODB_CLIENT = DynamoDbEnhancedClient.builder().build();
	private static final DynamoDbTable<LastValue> LAST_VALUE_TABLE = DYNAMODB_CLIENT.table(
			LAST_VALUE_TABLE_NAME,
			TableSchema.fromBean(LastValue.class));

	private static final DynamoDbTable<EnhancedDocument> JUMP_VALUE_TABLE = DYNAMODB_CLIENT.table(
			JUMP_VALUE_TABLE_NAME, TableSchema.documentSchemaBuilder()
					.addIndexPartitionKey(TableMetadata.primaryIndexName(), PATIENTID_ATTR_NAME,
							AttributeValueType.N)
					.addIndexSortKey(TableMetadata.primaryIndexName(), TIMESTAMP_ATTR_NAME, AttributeValueType.N)
					.attributeConverterProviders(AttributeConverterProvider.defaultProvider())
					.build());

	public void handleRequest(DynamodbEvent event, Context context) {
		processInsertedItemsFromDDBEvent(event, this::processImage);
	}

	private void processImage( Map<String, AttributeValue> image ) {
		Long patientId = Long.parseLong(image.get(PATIENTID_ATTR_NAME).getN());
		Integer currentValue = Integer.parseInt(image.get(PULSE_VALUE_ATTR_NAME).getN());
		LastValue prevItem = LAST_VALUE_TABLE.getItem(Key.builder().partitionValue(patientId).build());
		if (prevItem == null) {
			logger.finer("Patient doesn't have previous saved pulse value");
		}
		Integer lastValue = (prevItem == null ? currentValue : prevItem.getValue());
		logger.finer(() -> String.format("Got data: patientId=%d, value: (current=%d, last=%d)",
				patientId, currentValue, lastValue));
		if (isJump(currentValue, lastValue)) {
			jumpProcessing(patientId, currentValue, lastValue, image.get("timestamp").getN());
		}
		saveInDb(patientId, lastValue);
    }

	private static void saveInDb(Long patientId, Integer lastValue) {
		App.lastValue.setPatientId(patientId);
		App.lastValue.setValue(lastValue);
		try {
			LAST_VALUE_TABLE.putItem(App.lastValue);
		} catch (RuntimeException e) {
			logger.severe(String.format("Esception occured while call putItem on %s table: %s", LAST_VALUE_TABLE,
					e.getMessage()));

		}
	}

	private void jumpProcessing(Long patientId, Integer currentValue, Integer lastValue, String timestamp) {
		logger.finer(() -> String.format("Saving jump. patientId=%d, timestamp=%s, lastValue=%d, currentValue=%d",
				patientId, timestamp, lastValue, currentValue));
		EnhancedDocument newItem = EnhancedDocument.builder()
				.putNumber(PATIENTID_ATTR_NAME, patientId)
				.putNumber(TIMESTAMP_ATTR_NAME, Long.parseLong(timestamp))
				.putNumber(PREV_VALUE_ATTR_JUMP_VALUE_TABLE, lastValue)
				.putNumber(CURR_VALUE_ATTR_JUMP_VALUE_TABLE, currentValue)
				.build();
		try {
			JUMP_VALUE_TABLE.putItem(newItem);
			logger.info(
					String.format("Jump saved: patientId is %s,lastValue is %d, currentValue is %d, timestamp is %s\n",
							patientId, lastValue, currentValue, timestamp));
		} catch (DynamoDbException e) {
			logger.severe(String.format("Exception occured while putting new item:\n%s", e.getMessage()));
		}
	}

	private boolean isJump(Integer currentValue, Integer lastValue) {
		float factor = ConfigValues.factor;
		return (float) Math.abs(currentValue - lastValue) / lastValue > factor;
	}
}