package mockVkm;

import java.io.IOException;

import org.json.simple.JSONObject;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class GeocodeResponse {
	
	Object responseObject = new Object();
	JSONObject results = new JSONObject();
	
	String[] keys = {"ely", "ely_nimi", "address", "maakunta", "kuntakoodi", "maakunta_nimi", "urakka_alue_nimi", "urakka_alue", "kunta_nimi", "y", "x"};
	
	public GeocodeResponse(Parameters params, LambdaLogger logger, Boolean logger_on) throws IOException {
		MakeRequest request = new MakeRequest(params.getNewParameters(), logger, logger_on); 
		String vkm2020_response = request.getResponse();
		MapResponse response = new MapResponse(keys, vkm2020_response, logger, logger_on, true);
		this.results.put("results", response.getResponseSetArray());
		this.responseObject = this.results;
	}
	
	public Object getResponseObject() {
		return responseObject;
	}
	
}
