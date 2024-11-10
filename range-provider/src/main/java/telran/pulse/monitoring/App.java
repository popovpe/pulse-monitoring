package telran.pulse.monitoring;

import static telran.pulse.monitoring.AppLogging.logger;

import java.util.HashMap;
import java.util.Map;
import org.json.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import static telran.pulse.monitoring.Constants.*;
import static telran.pulse.monitoring.RangeMapImpl.rangeProvider;


public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
   

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, String> mapParameters = input.getQueryStringParameters();
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);

        try {
            String patientIdStr = mapParameters.get(PATIENT_ID_ATTR);
            if (patientIdStr == null) {
                telran.pulse.monitoring.AppLogging.logger.warning(String.format("Request doesn't contain %s value", PATIENT_ID_ATTR));
                throw new IllegalArgumentException("no patientId parameter");
            }
            logger.finer(()->String.format("Processing request for patientId=%s", patientIdStr));
            Range range = rangeProvider.getRange(patientIdStr);
            if (range == null) {
                logger.warning(String.format("RandeProvider didn't return range for patientId=%s", patientIdStr));
                throw new IllegalStateException(patientIdStr + " not found in ranges map");
            }
            
            response.withStatusCode(200)
                    .withBody(range.getRangeJSON());
        } catch (IllegalArgumentException e) {
            String errorJSON = getErrorJSON(e.getMessage());
            response
                    .withBody(errorJSON)
                    .withStatusCode(400);
        } catch (IllegalStateException e) {
            String errorJSON = getErrorJSON(e.getMessage());
            response
                    .withBody(errorJSON)
                    .withStatusCode(404);
        }
        logger.finer(()->String.format("Returning response: %s", response.getBody()));
        return response;
    }

    private String getErrorJSON(String message) {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("error", message);
        return jsonObj.toString();
    }
}