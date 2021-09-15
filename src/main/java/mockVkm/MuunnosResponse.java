package mockVkm;

import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class MuunnosResponse {
	
	Object responseObject = new Object();
	JSONObject tieosoitteet = new JSONObject();
	JSONObject koordinaatit = new JSONObject();
	
	String[] keysTieosoitteet = {"ajorata", "palautusarvo", "virheteksti", "tietyyppi", "valimatka", "osa", "etaisyys", "tie", "tunniste"};
	String[] keysKoordinaatit = {"palautusarvo", "virheteksti", "z", "y", "x"};
	
	public MuunnosResponse(Parameters params, LambdaLogger logger, Boolean logger_on) throws IOException {
		JSONObject newParams = new JSONObject();
		newParams = params.getNewParameters();
		if (params.getOldParameters().get("out").toString().equals("tieosoite")) {
			JSONArray muunnokset = (JSONArray) newParams.get("json");
			for (int i = 0; i < muunnokset.size(); i++) {
				((JSONObject) muunnokset.get(i)).put("tietyyppi", "1,2,3,4,5,6,7,8,9");
			}
			newParams.put("json", muunnokset);
		}
		MakeRequest request = new MakeRequest(newParams, logger, logger_on); 
		String vkm2020_response = request.getResponse();
		MapResponse response;
		if (params.getOldParameters().get("out").toString().equals("tieosoite")) {
			response = new MapResponse(keysTieosoitteet, vkm2020_response, logger, logger_on, true);
			this.tieosoitteet.put("tieosoitteet", response.getResponseSetArray());
			this.responseObject = this.tieosoitteet;
		}
		else if (params.getOldParameters().get("out").toString().equals("koordinaatti")) {
			response = new MapResponse(keysKoordinaatit, vkm2020_response, logger, logger_on, true);
			this.koordinaatit.put("koordinaatit", response.getResponseSetArray());
			this.responseObject = this.koordinaatit;
		}
	}
	
	public Object getResponseObject() {
		return responseObject;
	}
	
}