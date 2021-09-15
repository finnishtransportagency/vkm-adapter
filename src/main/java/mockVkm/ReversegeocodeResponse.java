package mockVkm;

import java.io.IOException;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class ReversegeocodeResponse {
	
	Object responseObject = new Object();
	
	String[] keys = {"virheteksti", "ely", "ely_nimi", "kunta", "maakunta", "kuntakoodi", "osoite", "maakunta_nimi", "urakka_alue_nimi", "urakka_alue", "y", "x"};
	
	public ReversegeocodeResponse(Parameters params, LambdaLogger logger, Boolean logger_on) throws IOException {
		MakeRequest request = new MakeRequest(params.getNewParameters(), logger, logger_on); 
		String vkm2020_response = request.getResponse();
		MapResponse response = new MapResponse(keys, vkm2020_response, logger, logger_on, true);
		this.responseObject =  response.getResponseSetArray()[0];
	}
	
	public Object getResponseObject() {
		return responseObject;
	}

}