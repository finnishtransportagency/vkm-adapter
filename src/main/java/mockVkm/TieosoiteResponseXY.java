package mockVkm;

import java.io.IOException;

import org.json.simple.JSONObject;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class TieosoiteResponseXY {

	Object responseObject = new Object();
	
	String[] keys = {"virhe", "ely_nimi", "tie", "kuntakoodi", "maakunta", "urakka_nimi", "urakka_alue", "kunta_nimi", "ajorata", "ely", "osa", "etaisyys", "maakunta_nimi", "y", "x"};
	
	public TieosoiteResponseXY(Parameters params, LambdaLogger logger, Boolean logger_on) throws IOException {
		JSONObject newParams = params.getNewParameters();
		newParams.put("tietyyppi", "1,2,3,4,5,6,7,8,9");
		MakeRequest request = new MakeRequest(newParams, logger, logger_on);
		String vkm2020_response = request.getResponse();
		MapResponse response = new MapResponse(keys, vkm2020_response, logger, logger_on, true);
		this.responseObject =  response.getResponseSetArray()[0];
	}
	
	public Object getResponseObject() {
		return responseObject;
	}
	
}
