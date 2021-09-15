package mockVkm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

import org.json.simple.JSONObject;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class MakeRequest {
	
	private static final String VKM2020_BASE_URL = "http://PTP_LB_URL/viitekehysmuunnin/muunna";
	//private static final String VKM2020_BASE_URL = "https://devapi.testivaylapilvi.fi/viitekehysmuunnin/muunna";
	//private static final String VKM2020_BASE_URL = "https://testijulkinen.vayla.fi/viitekehysmuunnin/muunna";

	private String queryString;
	private String response;
	
	public MakeRequest(JSONObject newParams, LambdaLogger logger, Boolean logger_on) throws IOException {
		setQueryString(newParams, logger);
		setResponse(this.queryString, logger, logger_on);
	}
	
	private void setQueryString(JSONObject newParams, LambdaLogger logger) {
		StringBuilder query = new StringBuilder();
    	Iterator<String> keys = newParams.keySet().iterator();

    	while(keys.hasNext()) {
    	    String key = keys.next();
    	    if (query.toString().contains("?") ) {
    			query.append("&");
    		}
    		else {
    			query.append("?");
    		}
    	    query.append(key);
    		query.append("=");
    		query.append(newParams.get(key).toString());
    	}
    	this.queryString = query.toString();
    	if (newParams.containsKey("json")) {
    		this.queryString = queryString.replaceAll("\\{", "%7B");
    		this.queryString = queryString.replaceAll("\\}", "%7D");
    		this.queryString = queryString.replaceAll(":", "%3A");
    		this.queryString = queryString.replaceAll("\"", "%22");
    		this.queryString = queryString.replaceAll(",", "%2C");
    		this.queryString = queryString.replaceAll("\\[", "%5B");
    		this.queryString = queryString.replaceAll("\\]", "%5D");
    	}
	}
	
	private void setResponse(String query, LambdaLogger logger, Boolean logger_on) throws IOException {
		StringBuilder content;
		String staticContent;
		URL url = new URL(VKM2020_BASE_URL + query);
		if (logger_on) {
			logger.log(url.toString());
		}
	
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		//connection.setRequestProperty("X-API-KEY", "");
		try (BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
			String line;
			content = new StringBuilder();
			while ((line = input.readLine()) != null) {
				content.append(line);
				content.append(System.lineSeparator());
			}
		} finally {
			connection.disconnect();
		}

		staticContent = content.toString();
		if (logger_on) {
			logger.log(staticContent);
		}
		this.response = staticContent;
	}
	
	public String getResponse() {
		return this.response;
	}
}
