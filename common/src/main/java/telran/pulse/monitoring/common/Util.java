package telran.pulse.monitoring.common;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;

import java.util.Map;
import java.util.function.Consumer;
import static telran.pulse.monitoring.common.AppLogging.logger;

public class Util {
    public static void processInsertedItemsFromDDBEvent(DynamodbEvent event,
            Consumer<Map<String, AttributeValue>> consumer) {
        event.getRecords().forEach(r -> {
            String eventName = r.getEventName();
            logger.finer(() -> String.format("eventName=%s", eventName));
            if (r.getEventName().equals("INSERT")) {
                logger.finer("Processing NewImage");
                Map<String, AttributeValue> image = r.getDynamodb().getNewImage();
                consumer.accept(image);
            }
        });
    }
}
