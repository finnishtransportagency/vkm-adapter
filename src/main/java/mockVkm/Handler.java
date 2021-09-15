package mockVkm;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

import org.json.simple.JSONObject;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Map;

public class Handler implements RequestHandler<Map<String, Object>, Object> {
    //@Override
    public Object handleRequest(Map<String, Object> event, Context context) {
    	
    	final Boolean LOGGER_ON = true;
    	
    	LambdaLogger logger = context.getLogger();
    	
    	if (LOGGER_ON) {
    		logger.log("ENVIRONMENT VARIABLES: " + System.getenv());
    		logger.log("CONTEXT: " + context);
    		logger.log("EVENT: " + event);
    		logger.log("EVENT TYPE: " + event.getClass().toString());
    	}
    	
    	Gson gson = new Gson();
    	Parameters params = new Parameters(event, logger, LOGGER_ON);
    	String path = event.get("path").toString();
        
        JSONObject response = new JSONObject();
        JSONObject headers = new JSONObject();
        Object body_obj = new Object();
    	
    	if (path.toUpperCase().contains("MUUNNOS")) {
    		if (LOGGER_ON) {
    			logger.log("Muunnos");
    		}
    		try {
				body_obj = new MuunnosResponse(params, logger, LOGGER_ON).getResponseObject();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	else if (path.toUpperCase().contains("TIEOSOITE")) {
    		if (LOGGER_ON) {
    			logger.log("Tieosoite");
    		}
    		if (params.getNewParameters().containsKey("x")) {
    			if (LOGGER_ON) {
					logger.log("Tieosoite: XY-haku");
				}
    			try {
    				body_obj = new TieosoiteResponseXY(params, logger, LOGGER_ON).getResponseObject();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
    		else if (params.getNewParameters().containsKey("valihaku")) {
    			try {
        			String valihakuValue = params.getNewParameters().get("valihaku").toString();
        			if (valihakuValue.toUpperCase().equals("TRUE")) {
        				if (LOGGER_ON) {
        					logger.log("Tieosoite: Valihaku");
        				}
        				try {
            				body_obj = new TieosoitevaliResponse(params, logger, LOGGER_ON).getResponseObject();
            			} catch (IOException e) {
            				e.printStackTrace();
            			}
        			}
        		}
    			catch(Exception e) {				
    			}
    		}
    		else if (params.getNewParameters().containsKey("tie")) {
    			if (LOGGER_ON) {
					logger.log("Tieosoite: haku pistemäisellä tieosoitteella");
				}
    			try {
    				body_obj = new TieosoiteResponse(params, logger, LOGGER_ON).getResponseObject();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
    		
    	}
    	else if (path.toUpperCase().contains("REVERSEGEOCODE")) {
    		if (LOGGER_ON) {
    			logger.log("Reversegeocode");
    		}
    		try {
				body_obj = new ReversegeocodeResponse(params, logger, LOGGER_ON).getResponseObject();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	else if (path.toUpperCase().contains("GEOCODE")) {
    		if (LOGGER_ON) {
    			logger.log("Geocode");
    		}
    		try {
				body_obj = new GeocodeResponse(params, logger, LOGGER_ON).getResponseObject();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	else {
    		if (LOGGER_ON) {
    			logger.log("Virhe");
    		}
    		body_obj = new Virhe("Virheellinen polku", logger, LOGGER_ON);
    	}
    
        headers.put("content-type", "application/json; charset=UTF-8");
    	//headers.put("content-type", "text/html; charset=UTF-8");
        String body = gson.toJson(body_obj);
    
        response.put("isBase64Encoded", false);
        response.put("statusCode", 200);
        response.put("headers", headers);
        response.put("body",body);

        
        return response;
    }
    
}