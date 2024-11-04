package telran.pulse.monitoring;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;

public class App {
	 public void handleRequest(final DynamodbEvent event, final Context context) {
		System.out.println("Records: " + event.getRecords().size());
	        event.getRecords().forEach( r -> {
	        	var map = r.getDynamodb().getNewImage();
	        	System.out.printf("Patient ID = %s, timestamp = %s, pulse = %s%n",
	        			map.get("patientId").getN(), map.get("timestamp").getN(), map.get("value").getN());
	        });
	    }

}