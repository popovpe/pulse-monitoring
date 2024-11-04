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
import java.util.logging.*;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

public class App {

	private static final String PARTITION_KEY_ATTR_NAME = "seqNumber";
	private static final String PULSE_DATA_TABLE_NAME = "pulse-abnormal-values";
	private static final String PATIENTID_ATTR_NAME = "patientId";
	private static final String TIMESTAMP_ATTR_NAME = "timestamp";
	private static final String PULSE_VALUE_ATTR_NAME = "value";
	public static final int MIN_NORMAL_VALUE = 50;
	public static final int MAX_NORMAL_VALUE = 190;

	public static Logger logger = configureLoggingFramework();


	private static final DynamoDbEnhancedClient DYNAMODB_CLIENT = DynamoDbEnhancedClient.builder().build();
	private static final DynamoDbTable<EnhancedDocument> DYNAMODB_TABLE = DYNAMODB_CLIENT.table(PULSE_DATA_TABLE_NAME,
			TableSchema.documentSchemaBuilder()
					.addIndexPartitionKey(TableMetadata.primaryIndexName(), PARTITION_KEY_ATTR_NAME,
							AttributeValueType.N)
					.attributeConverterProviders(AttributeConverterProvider.defaultProvider())
					.build());

	public void handleRequest(final DynamodbEvent event, final Context context) {
		
		logger.finer("Hadler started");
		logger.finer("Number records in event: " + event.getRecords().size());
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

	private JSONObject getJSONFromMap(Map<String, AttributeValue> map) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(PARTITION_KEY_ATTR_NAME, Long.parseLong(map.get(PARTITION_KEY_ATTR_NAME).getN()));
		jsonObject.put(PATIENTID_ATTR_NAME, Long.parseLong(map.get(PATIENTID_ATTR_NAME).getN()));
		jsonObject.put(TIMESTAMP_ATTR_NAME, Long.parseLong(map.get(TIMESTAMP_ATTR_NAME).getN()));
		jsonObject.put(PULSE_VALUE_ATTR_NAME, Integer.parseInt(map.get(PULSE_VALUE_ATTR_NAME).getN()));
		return jsonObject;
	}

	private static Logger configureLoggingFramework() {
		logger = Logger.getLogger("logger");
		logger.setUseParentHandlers(false);
		logger.setLevel(parseLogLevelOrDefault(System.getenv("LOGGING_LEVEL"), Level.INFO));
		Handler handler = new ConsoleHandler();
		handler.setFormatter(new Formatter() {

			@Override
			public String format(LogRecord record) {
				StringBuilder builder = new StringBuilder();
				builder.append("[").append(record.getLevel()).append("] - ");
				builder.append(record.getSourceMethodName()).append(" - ");
				builder.append(formatMessage(record)).append("\n");
				return builder.toString();
			}
		});
		handler.setLevel(Level.FINEST);
		logger.addHandler(handler);
		return logger;
	}

	private static Level parseLogLevelOrDefault(String varName, Level defaultLevel) {
		Level logLevel = null;
		try {
		} catch (RuntimeException e) {
			logLevel = defaultLevel;
		}
		return logLevel;
	}

}