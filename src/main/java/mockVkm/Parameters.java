package mockVkm;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Parameters {
	
	private Object oldParametersObj;
	private String oldParametersJson;
	private JSONObject oldParameters;
	
	private String newParametersJson;
	private JSONObject newParameters;
	
	public Parameters(Map<String, Object> event, LambdaLogger logger, Boolean logger_on) {
		Gson gson = new Gson();
    	JSONParser parser = new JSONParser();
    	
		this.oldParametersObj = event.get("queryStringParameters");
		this.oldParametersJson = gson.toJson(this.oldParametersObj);
		this.oldParameters = new JSONObject();
		try {
			this.oldParameters = (JSONObject) parser.parse(this.oldParametersJson);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		this.newParameters = new JSONObject();
		this.newParameters = setNewParameters(this.newParameters, this.oldParameters, logger);
	}
	
	private JSONObject setNewParameters(JSONObject newParams, JSONObject oldParams, LambdaLogger logger) {
		Iterator<String> keys = oldParams.keySet().iterator();
    	while(keys.hasNext()) {
    	    String key = keys.next();
    	    newParams.put(key, oldParams.get(key));
    	}
    	if (newParams.containsKey("json")) {
    		JSONParser parser = new JSONParser();
        	JSONObject store = new JSONObject();
    		try {
    			store = (JSONObject) parser.parse(newParams.toString());
    		} catch (ParseException e) {
    			e.printStackTrace();
    		}
    		newParams.clear();
    		newParams = setNewJson(store, logger);
    	}
    	else {
	    	newParams = setCommonParameters(newParams, oldParams, logger);
    	}
    	newParams.put("metadata", "true");
    	return newParams;
	}
	
	private JSONObject setCommonParameters(JSONObject newParams, JSONObject oldParams, LambdaLogger logger) {
		if (newParams.containsKey("sade") && Integer.valueOf(newParams.get("sade").toString()) > 1000) {
    		newParams.put("sade", 1000);
    	}
		if (newParams.containsKey("alueetpois")) {
    		newParams.remove("alueetpois");
    		newParams.put("palautusarvot", "1,2,3,5,6");
    	}
    	else {
    		newParams.put("palautusarvot", "1,2,3,4,5,6");
    	}
    	if (newParams.containsKey("callback")) {
    		newParams.remove("callback");
    	}
    	if (newParams.containsKey("in")) {
    		newParams.remove("in");
    	}
    	if (newParams.containsKey("out")) {
    		newParams.remove("out");
    	}
    	if (newParams.containsKey("et_erotin")) {
    		newParams.remove("et_erotin");
    	}
    	if (newParams.containsKey("maxoffset")) {
    		newParams.remove("maxoffset");
    	}
    	if (newParams.containsKey("raide_text")) {
    		newParams.remove("raide_text");
    	}
    	if (newParams.containsKey("kieli")) {
    		newParams.remove("kieli");
    	}
    	if (newParams.containsKey("losa")) {
    		newParams.put("osa_loppu", oldParams.get("losa"));
    		newParams.remove("losa");
    	}
    	if (newParams.containsKey("let")) {
    		newParams.put("etaisyys_loppu", oldParams.get("let"));
    		newParams.remove("let");
    	}
    	if ( (newParams.containsKey("tie")) ) {
    		Boolean validRoad = false;
    		Boolean additionalRoad = false;
    		try {
    			Integer tieValue = Integer.valueOf(newParams.get("tie").toString());
    			validRoad = true;
    		}
			catch(Exception e) {				
			}
    		if (newParams.containsKey("x")) {
    			try {
        			Double xValue = Double.valueOf(newParams.get("x").toString());
        			additionalRoad = true;
        		}
    			catch(Exception e) {				
    			}
    		}
    		if (validRoad && !additionalRoad) {
	    		Boolean validOsa = false;
	    		Boolean validEtaisyys = false;
	    		Boolean validLosa = false;
	    		Boolean valihaku = true;
    			if (newParams.containsKey("osa")) {
    				try {
            			Integer osaValue = Integer.valueOf(newParams.get("osa").toString());
            			validOsa = true;
            		}
    				catch(Exception e) {				
        			}
    			}
    			if (newParams.containsKey("etaisyys")) {
    				try {
            			Integer etaisyysValue = Integer.valueOf(newParams.get("etaisyys").toString());
            			if (validOsa)
            				validEtaisyys = true;
            		}
    				catch(Exception e) {				
        			}
    			}
    			if (newParams.containsKey("osa_loppu")) {
    				try {
            			Integer losaValue = Integer.valueOf(newParams.get("osa_loppu").toString());
            			validLosa = true;
            		}
    				catch(Exception e) {				
        			}
    			}
    			if (validEtaisyys)
    				valihaku = false;
    			if (validLosa)
    				valihaku = true;
    			if ( valihaku ) {
    				newParams.put("valihaku", "true");
    			}
    		}
    	}
    	if (newParams.containsKey("address")) {
    		newParams = getKatuOsoiteFromAddress(newParams, logger);
    		newParams.remove("address");
    	}
    	if (newParams.containsKey("tievali")) {
    		newParams = setVaylanLuonne(newParams, 0); //change!!
    	}
    	return newParams;
	}
	
	private JSONObject getKatuOsoiteFromAddress(JSONObject newParams, LambdaLogger logger) {
		String osoite = newParams.get("address").toString().trim();
		osoite = osoite.replaceAll("%2520", " ");
		osoite = osoite.replaceAll("%20", " ");
		osoite = osoite.replaceAll("%252C", ",");
		osoite = osoite.replaceAll("%2C", ",");
		osoite = osoite.replaceAll(",", " ");
		osoite = osoite.replaceAll("\\s+", " ");
		String[] katuosoite = osoite.split(" ");
		if (katuosoite.length > 0) {
			newParams.put("katunimi", katuosoite[0]);
			if (katuosoite.length > 1) {
				try {
					newParams.put("katunumero", Integer.parseInt(katuosoite[1]));
					if (katuosoite.length > 2) {
						newParams.put("kuntanimi", katuosoite[2]);
					}
				}
				catch (Exception e) {
					newParams.put("kuntanimi", katuosoite[1]);
				}
			}
		}
		return newParams;
	}
	
	private JSONObject setNewJson(JSONObject newParams, LambdaLogger logger) {
		List<JSONObject> jsonMuunnoksetStore = new ArrayList<JSONObject>();
		JSONParser parser = new JSONParser();
    	JSONObject oldParamsJsonParam = new JSONObject();
		String decodedJson = String.valueOf(newParams.get("json"));
		decodedJson = decodedJson.replaceAll("%7B", "\\{");
		decodedJson = decodedJson.replaceAll("%7D", "\\}");
		decodedJson = decodedJson.replaceAll("%3A", ":");
		decodedJson = decodedJson.replaceAll("%22", "\"");
		decodedJson = decodedJson.replaceAll("%2C", ",");
		decodedJson = decodedJson.replaceAll("%5B", "\\[");
		decodedJson = decodedJson.replaceAll("%5D", "\\]");
		try {
		   oldParamsJsonParam = (JSONObject) parser.parse(decodedJson);
		} catch (ParseException e) {
		   e.printStackTrace();
		}
		
		JSONArray ParamObjects = new JSONArray();
		if (oldParamsJsonParam.containsKey("tieosoitteet")) {
			ParamObjects = (JSONArray) oldParamsJsonParam.get("tieosoitteet");
		}
		else if (oldParamsJsonParam.containsKey("koordinaatit")) {
			ParamObjects = (JSONArray) oldParamsJsonParam.get("koordinaatit");
		}
		else if (oldParamsJsonParam.containsKey("rataosoitteet")) {
			ParamObjects = (JSONArray) oldParamsJsonParam.get("rataosoitteet");
		}
		
		JSONObject obj = new JSONObject();
		JSONObject objNew = new JSONObject();
		for (int i = 0; i < ParamObjects.size(); i++ ) {
			try {
				obj = (JSONObject) parser.parse(String.valueOf(ParamObjects.get(i)));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			objNew = obj;
			obj = setCommonParameters(objNew, obj, logger);
			if (this.oldParameters.containsKey("tilannepvm")) {
				objNew.put("tilannepvm", this.oldParameters.get("tilannepvm"));
			}
			if (this.oldParameters.containsKey("kohdepvm")) {
				objNew.put("kohdepvm", this.oldParameters.get("kohdepvm"));
			}
			jsonMuunnoksetStore.add(objNew);
		}
		JSONObject[] jsonMuunnoksetTemp = new JSONObject[jsonMuunnoksetStore.size()];
		jsonMuunnoksetStore.toArray(jsonMuunnoksetTemp);
		JSONArray jsonMuunnokset = new JSONArray();
		for (int j = 0; j < jsonMuunnoksetTemp.length; j++) {
			jsonMuunnokset.add(jsonMuunnoksetTemp[j]);
		}
		JSONObject json= new JSONObject();
		json.put("json", jsonMuunnokset);
		return json;		
	}
	
	public JSONObject setVaylanLuonne(JSONObject newParams, Integer toAdd) {
		// Väylän luonteen lisäys tievälin mukaan
		newParams.put("vaylan_luonne", toAdd);
		return newParams;
	}
	
	public JSONObject setTietyyppi(JSONObject newParams, Integer toAdd) {
		// Tietyypin lisääminen
		newParams.put("tietyyppi", toAdd);
		return newParams;
	}

	public Object getOldParametersObj() {
		return oldParametersObj;
	}

	public String getOldParametersJson() {
		return oldParametersJson;
	}

	public JSONObject getOldParameters() {
		return oldParameters;
	}

	public String getNewParametersJson() {
		return newParametersJson;
	}

	public JSONObject getNewParameters() {
		return newParameters;
	}
	
}
