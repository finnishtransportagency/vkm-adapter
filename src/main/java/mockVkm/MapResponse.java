package mockVkm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class MapResponse {

	Map[] responseSetArray;
	
	Integer distance = 0;
	
	//Key = oldVkmKey, Value = newVkmkey
	HashMap<String, String> mappings = new HashMap<String, String>();
	
	List<String> stringIntegersWithLeadingZero = new ArrayList<>(Arrays.asList("maakunta", "kuntakoodi", "kuntanro", "urakka_alue"));
	
	HashMap<String, Integer> codeLength = new HashMap<String, Integer>();
	{
		codeLength.put("maakunta", 2);
		codeLength.put("kuntakoodi", 3);
		codeLength.put("kuntanro", 3);
		codeLength.put("urakka_alue", 3);
	}
	
	Boolean hasErrors = false;
	
	Object errorsObject = null;
	
	
	public MapResponse(String[] responseKeys, String response, LambdaLogger logger, Boolean logger_on, Boolean alkupiste) {
		Integer countingTrack = null;
		setMappings(alkupiste);
		List<Map> responseSetStore = new ArrayList<Map>();
    	JSONParser parser = new JSONParser();
    	JSONObject responseObj = new JSONObject();
		try {
			responseObj = (JSONObject) parser.parse(response);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		JSONArray features = (JSONArray) responseObj.get("features");
	for (int i = 0; i < features.size(); i++) {
		Boolean hasErrorsInFeature = false;
		Map responseSet = new LinkedHashMap();
		JSONObject feature = (JSONObject) features.get(i);
		JSONObject properties = (JSONObject) feature.get("properties");
		
		JSONObject firstError = new JSONObject();
		if (properties.containsKey("virheet")) {
			JSONArray virheet = (JSONArray) properties.get("virheet");
			for (Object virhe : virheet) {
				JSONObject virheJSON = (JSONObject) virhe;
				byte virheKoodi = Byte.valueOf(virheJSON.get("virhekoodi").toString());
				if (virheKoodi >= 1 && virheKoodi <= 3) {
					this.hasErrors = true;
					hasErrorsInFeature = true;
					firstError = virheJSON;
				}
			}
		}
		
		if (!alkupiste && properties.containsKey("etaisyys_loppu") && properties.containsKey("etaisyys")) {
			if (countingTrack == null) {
				if (!Integer.valueOf(properties.get("ajorata").toString()).equals(0)) {
					countingTrack = Integer.valueOf(properties.get("ajorata").toString());
				}
			}
			if (Integer.valueOf(properties.get("ajorata").toString()) == 0 || Integer.valueOf(properties.get("ajorata").toString()) == countingTrack) {
				this.distance += Integer.valueOf(properties.get("mitattu_pituus").toString());
			}
		}
		
		for (int k = 0; k < responseKeys.length; k++) {
			String oldVkmKey = responseKeys[k];
			String newVkmKey = mappings.get(oldVkmKey);
			if (!hasErrorsInFeature) {
				if (newVkmKey != null && properties.containsKey(newVkmKey)) {
					if (stringIntegersWithLeadingZero.contains(oldVkmKey)) {
						String intValue = properties.get(newVkmKey).toString();
						for (int l = intValue.length(); l < codeLength.get(oldVkmKey); l++) {
							intValue = "0" + intValue;
						}
						responseSet.put(oldVkmKey, intValue);
					}
					else {
						responseSet.put(oldVkmKey, properties.get(newVkmKey));
					}
				}
				else if (oldVkmKey.equals("osoite")) {
					if (properties.containsKey("katunimi")) {
						responseSet.put("osoite", getOsoiteFromNewVkm(properties));
					}
				}
				else if (oldVkmKey.equals("address")) {
					if (properties.containsKey("katunimi")) {
						responseSet.put("address", getAddressFromNewVkm(properties));
					}
				}
				else if (oldVkmKey.equals("point")) {
					if (properties.containsKey("x") && properties.containsKey("y")) {
						responseSet.put("point", getPointFromNewVkm(properties, alkupiste, logger));
					}
				}
				if (oldVkmKey.equals("palautusarvo") && alkupiste) {
					responseSet.put("palautusarvo", 1);
				}
			}
			else {
				if (oldVkmKey.equals("palautusarvo") && alkupiste) {
					responseSet.put("palautusarvo", 0);
				}
				if (oldVkmKey.equals("virhe") && alkupiste) {
					responseSet.put("virhe", firstError.get("virheviesti"));
				}
				if (oldVkmKey.equals("virheteksti") && alkupiste) {
					responseSet.put("virheteksti", firstError.get("virheviesti"));
				}
			}
		}
		if (this.hasErrors) {
			this.errorsObject = responseSet;
		}
		if (!responseSet.isEmpty()) {
			responseSetStore.add(responseSet);
		}
	}
	this.responseSetArray = new Map[responseSetStore.size()];
	responseSetStore.toArray(responseSetArray);
	}
	
	private String getOsoiteFromNewVkm(JSONObject props) {
		StringBuilder osoite = new StringBuilder(0);
		if (props.containsKey("katunimi")) {
			osoite.append(props.get("katunimi"));
			if (props.containsKey("katunumero")) {
				osoite.append(" " + props.get("katunumero").toString());
			}
		}
		return osoite.toString();
	}
	
	private String getAddressFromNewVkm(JSONObject props) {
		StringBuilder osoite = new StringBuilder(0);
		if (props.containsKey("katunimi")) {
			osoite.append(props.get("katunimi"));
			if (props.containsKey("katunumero")) {
				osoite.append(" " + props.get("katunumero").toString());
				if (props.containsKey("kuntanimi")) {
					osoite.append(", " + props.get("kuntanimi"));
				}
			}
		}
		return osoite.toString();
	}
	
	private Map getPointFromNewVkm(JSONObject props, Boolean alkupiste, LambdaLogger logger) {
		Map point = new LinkedHashMap();
		JSONObject wkid = new JSONObject();
		wkid.put("wkid", 3067);
		point.put("spatialReference", wkid);
		Double y = alkupiste ? Double.valueOf(props.get("y").toString()) : Double.valueOf(props.get("y_loppu").toString());
		Double x = alkupiste ? Double.valueOf(props.get("x").toString()) : Double.valueOf(props.get("x_loppu").toString());
		point.put("y", y);
		point.put("x", x);
		return point;
	}
	
	private void setMappings(Boolean alkupiste) {
		mappings.put("tunniste", "tunniste");
		if (alkupiste) {
			mappings.put("ely", "ely");
			mappings.put("ely_nimi", "elynimi");
			mappings.put("kunta", "kuntanimi");
			mappings.put("kunta_nimi", "kuntanimi");
			mappings.put("maakunta", "maakuntakoodi");
			mappings.put("kuntakoodi", "kuntakoodi");
			mappings.put("kuntanro", "kuntakoodi");
			mappings.put("maakunta_nimi", "maakuntanimi");
			mappings.put("urakka_alue_nimi", "ualuenimi");
			mappings.put("urakka_nimi", "ualuenimi");
			mappings.put("urakka_alue", "ualue");
			mappings.put("x", "x");
			mappings.put("y", "y");
			mappings.put("z", "z");
			mappings.put("tie", "tie");
			mappings.put("ajorata", "ajorata");
			mappings.put("osa", "osa");
			mappings.put("etaisyys", "etaisyys");
			mappings.put("tietyyppi", "tietyyppi");
			mappings.put("valimatka", "valimatka");
		}
		else {
			mappings.put("ely", "ely_loppu");
			mappings.put("ely_nimi", "elynimi_loppu");
			mappings.put("kunta", "kuntanimi_loppu");
			mappings.put("kunta_nimi", "kuntanimi_loppu");
			mappings.put("maakunta", "maakuntakoodi_loppu");
			mappings.put("kuntakoodi", "kuntakoodi_loppu");
			mappings.put("kuntanro", "kuntakoodi_loppu");
			mappings.put("maakunta_nimi", "maakuntanimi_loppu");
			mappings.put("urakka_alue_nimi", "ualuenimi_loppu");
			mappings.put("urakka_nimi", "ualuenimi_loppu");
			mappings.put("urakka_alue", "ualue_loppu");
			mappings.put("x", "x_loppu");
			mappings.put("y", "y_loppu");
			mappings.put("z", "z_loppu");
			mappings.put("tie", "tie_loppu");
			mappings.put("ajorata", "ajorata_loppu");
			mappings.put("osa", "osa_loppu");
			mappings.put("etaisyys", "etaisyys_loppu");
			mappings.put("tietyyppi", "tietyyppi_loppu");
			mappings.put("valimatka", "valimatka_loppu");
		}
	}
	
	public Map[] getResponseSetArray() {
		return this.responseSetArray;
	}
	
	public Integer getDistance() {
		return this.distance;
	}
	
	public Boolean getHasErrors() {
		return this.hasErrors;
	}
	
	public Object getErrorsObject() {
		return this.errorsObject;
	}
}
