package telran.pulse.monitoring;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import static telran.pulse.monitoring.common.AppLogging.logger;
import static telran.pulse.monitoring.Constants.*;
import static telran.pulse.monitoring.common.Constants.*;

import static telran.pulse.monitoring.common.Util.processInsertedItemsFromDDBEvent;

public class App {

    final String topicARN = System.getenv(TOPIC_ARN_ENV_NAME);
    static SnsClient snsClient;
    static {
        snsClient = SnsClient.builder().region(Region.of(System.getenv("AWS_REGION"))).build();
        logger.finer("SNS client instance created");
    }

    public void handleRequest(DynamodbEvent event, Context context) {
        if (topicARN != null) {
            processInsertedItemsFromDDBEvent(event, this::processImage);
        } else {
            logger.severe(String.format("Impossible to handle requests: env var %s is not set", TOPIC_ARN_ENV_NAME));
        }
    }

    private void processImage( Map<String, AttributeValue> image ) {
        String message = getMessage(image);
        logger.finer(()->String.format("Message for publishing:%s", message));
        publishMessage(message);
    }

    private void publishMessage(String message) {
        PublishRequest request = PublishRequest.builder()
                .message(message)
                .topicArn(topicARN)
                .build();
                
        try {
            snsClient.publish(request);
        } catch (RuntimeException e) {
            logger.severe("Error occured while publishing request view SNS");
            throw new RuntimeException(e);
        } 

    }

    private String getMessage(Map<String, AttributeValue> image) {
        String patientId = image.get(PATIENTID_ATTR_NAME).getN();
        String value = image.get(PULSE_VALUE_ATTR_NAME).getN();
        Instant instant = Instant.ofEpochMilli(Long.parseLong(image.get(TIMESTAMP_ATTR_NAME).getN()));
        String timestampStr = LocalDateTime.ofInstant(instant, ZoneId.of("Z")).toString();
        return String.format("Abnormal pulse detected%n%s=%s,%s=%s,%s=%s", PATIENTID_ATTR_NAME, patientId, PULSE_VALUE_ATTR_NAME,
                value, TIMESTAMP_ATTR_NAME, timestampStr);
    }

}