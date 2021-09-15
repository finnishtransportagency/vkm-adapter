package mockVkm;

import java.io.IOException;

import org.json.simple.JSONObject;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class TieosoiteResponse {

	Object responseObject = new Object();
	JSONObject tieosoitteet = new JSONObject();
	JSONObject alkupiste = new JSONObject();
	
	String[] keys = {"virhe", "tietyyppi", "ely_nimi", "tie", "kuntakoodi", "maakunta", "urakka_nimi", "urakka_alue", "kunta_nimi", "ajorata", "point", "ely", "osa", "etaisyys", "maakunta_nimi"};
		
	public TieosoiteResponse(Parameters params, LambdaLogger logger, Boolean logger_on) throws IOException {
		MakeRequest request = new MakeRequest(params.getNewParameters(), logger, logger_on); 
		String vkm2020_response = request.getResponse();
		MapResponse response = new MapResponse(keys, vkm2020_response, logger, logger_on, true);
		if (!response.getHasErrors()) {
			this.tieosoitteet.put("tieosoitteet", response.getResponseSetArray());
			this.alkupiste.put("alkupiste", this.tieosoitteet);
			this.responseObject =  this.alkupiste;
		}
		else {
			this.responseObject =  response.getErrorsObject();
		}
	}
	
	public Object getResponseObject() {
		return responseObject;
	}
	
}