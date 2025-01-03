package telran.pulse.monitoring;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import java.util.Map;
import static telran.pulse.monitoring.common.Constants.*;
import static telran.pulse.monitoring.Constants.*;

import static telran.pulse.monitoring.common.AppLogging.logger;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import telran.pulse.monitoring.common.Range;

import java.net.URI;
import java.net.http.*;
import java.net.http.HttpResponse.BodyHandlers;
import static telran.pulse.monitoring.common.Util.processInsertedItemsFromDDBEvent;

public class App {
	private static final DynamoDbEnhancedClient DYNAMODB_CLIENT = DynamoDbEnhancedClient.builder().build();
	private static final DynamoDbTable<PulseData> DYNAMODB_TABLE = DYNAMODB_CLIENT.table(PULSE_DATA_TABLE_NAME,
			TableSchema.fromClass(PulseData.class));
	private static HttpClient client = HttpClient.newHttpClient();

	public void handleRequest(final DynamodbEvent event, final Context context) {
		processInsertedItemsFromDDBEvent(event, this::processImage);
	}

	private void processImage(Map<String, AttributeValue> image) {
		int pulse = Integer.parseInt(image.get(PULSE_VALUE_ATTR_NAME).getN());
		String patientId = image.get(PATIENTID_ATTR_NAME).getN();
		logger.finer(() -> String.format("NewImage: Patient ID = %s, timestamp = %s, pulse = %d%n",
				patientId, image.get(TIMESTAMP_ATTR_NAME).getN(), pulse));
		if (isPulseAbnormal(patientId, pulse)) {
			registerAbnormalPulse(image);
		}
	}

	private boolean isPulseAbnormal(String patientId, int pulse) {
		boolean result = false;
		String apiURL = System.getenv(RANGE_PROVIDER_ENVAR_NAME);
		if (apiURL == null) {
			logger.warning(String.format("Impossible to request RangeProvider, enviroment variable %s is undefined",
					RANGE_PROVIDER_ENVAR_NAME));
		} else {
			String url = String.format("%s?%s=%s", apiURL, PATIENTID_ATTR_NAME, patientId);
			logger.finer(() -> String.format("Making request: %s", url));
			try {
				HttpRequest request = HttpRequest.newBuilder(new URI(url)).build();
				HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
				result = Range.from(response.body()).notInRange(pulse);
			} catch (Exception e) {
				logger.warning(String.format(
						"Exception occured while getting and analyzing response from RangeProvider", e.getMessage()));
			}
		}
		logger.finer(String.format("isPulseAbnormal returns %b", result));
		return result;
	}

	private void registerAbnormalPulse(Map<String, AttributeValue> map) {
		logger.info(String.format("Abnormal pulse. Patient ID = %s, pulse = %s%n", map.get(PATIENTID_ATTR_NAME).getN(),
				map.get(PULSE_VALUE_ATTR_NAME).getN()));
		try {
			DYNAMODB_TABLE.putItem(new PulseData(map));
		} catch (DynamoDbException | IllegalStateException e) {
			logger.severe(String.format("Exception occured while putting new item in DDB: %s", e.getMessage()));
		}
	}
}