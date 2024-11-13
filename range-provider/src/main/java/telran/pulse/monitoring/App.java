package telran.pulse.monitoring;

import java.util.HashMap;
import java.util.Map;
import org.json.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import telran.pulse.monitoring.common.Range;

import static telran.pulse.monitoring.RangeMapImpl.rangeProvider;
import static telran.pulse.monitoring.common.AppLogging.logger;
import static telran.pulse.monitoring.common.Constants.*;


public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
   

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, String> mapParameters = input.getQueryStringParameters();
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);

        try {
            String patientIdStr = mapParameters.get(PATIENTID_ATTR_NAME );
            if (patientIdStr == null) {
                logger.warning(String.format("Request doesn't contain %s value", PATIENTID_ATTR_NAME ));
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