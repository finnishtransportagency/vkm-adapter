package mockVkm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

//DouglasPeucker adapted from Justin Wetherell (https://github.com/phishman3579/java-algorithms-implementation.git)


public class TieosoitevaliResponse {

	Object responseObject = new Object();
	Map valiResponse = new LinkedHashMap();
	JSONObject tieosoitteetAlku = new JSONObject();
	JSONObject tieosoitteetLoppu = new JSONObject();
	JSONObject linesContent = new JSONObject();
	JSONObject alkupiste = new JSONObject();
	JSONObject loppupiste = new JSONObject();
	JSONObject lines = new JSONObject();
	Integer distance = null;
	
	String[] keys = {"virhe", "ajorata", "point", "tietyyppi", "osa", "etaisyys", "tie"};
	
	public TieosoitevaliResponse(Parameters params, LambdaLogger logger, Boolean logger_on) throws IOException {
		MakeRequest request = new MakeRequest(params.getNewParameters(), logger, logger_on); 
		String vkm2020_response = request.getResponse();
		
		MapResponse responseAlku = new MapResponse(keys, vkm2020_response, logger, logger_on, true);
		if (!responseAlku.getHasErrors()) {
			this.tieosoitteetAlku.put("tieosoitteet", streamPisteet(responseAlku.getResponseSetArray(), true, logger));
					
			MapResponse responseLoppu = new MapResponse(keys, vkm2020_response, logger, logger_on, false);
			this.distance = responseLoppu.getDistance();
			this.tieosoitteetLoppu.put("pituus", this.distance);
			this.tieosoitteetLoppu.put("tieosoitteet", streamPisteet(responseLoppu.getResponseSetArray(), false, logger));
			
			this.lines.put("lines", getLines(vkm2020_response, logger));
			
			this.valiResponse.put("loppupiste", this.tieosoitteetLoppu);
			this.valiResponse.put("lines", this.lines);
			this.valiResponse.put("alkupiste", this.tieosoitteetAlku);
			this.responseObject =  this.valiResponse;
		}
		else {
			this.responseObject = responseAlku.getErrorsObject();
		}
	}
	
	public Object getResponseObject() {
		return responseObject;
	}
	
	private Map[] getLines(String response, LambdaLogger logger) {
		List<Map> linesStore = new ArrayList<Map>();
		JSONParser parser = new JSONParser();
    	JSONObject responseObj = new JSONObject();
		try {
			responseObj = (JSONObject) parser.parse(response);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		JSONArray features = (JSONArray) responseObj.get("features");
		Double epsilon = getEpsilon(features, logger);
		logger.log("Epsilon: " + epsilon.toString());
		for (int i = 0; i < features.size(); i++) {
			JSONObject feature = (JSONObject) features.get(i);
			JSONObject geometry = (JSONObject) feature.get("geometry");
			String gType = String.valueOf(geometry.get("type"));
			JSONArray coordinates = (JSONArray) geometry.get("coordinates");
			if (gType.equals("MultiLineString")) {
				for (int j = 0; j < coordinates.size(); j++) {
					JSONArray lineNew = (JSONArray) coordinates.get(j);
					if (this.distance != null && this.distance > 100000) { //Simplify geometry
						Double[][] lineNewData = new Double[lineNew.size()][3];
						for (int k = 0; k < lineNew.size(); k++) {
							JSONArray coord = new JSONArray();
							coord = (JSONArray) lineNew.get(k);
							for (int l = 0; l < coord.size(); l++) {
								lineNewData[k][l] = Double.valueOf((coord.get(l)).toString());
							}
						}
						Double[][] lineNewHolder = douglasPeucker(lineNewData, epsilon, logger);
						int length = lineNewHolder.length;
						JSONArray lineNewSimplified = new JSONArray();
						for (int m = 0; m < lineNewHolder.length; m++) {
							JSONArray position = new JSONArray();
							for (int n = 0; n < (lineNewHolder[m]).length; n++) {
								position.add(lineNewHolder[m][n]);
							}
							lineNewSimplified.add(position);
						}
						linesStore = addLine(linesStore, lineNewSimplified, logger);
					}
					else {
						linesStore = addLine(linesStore, lineNew, logger);
					}
				}
			}
			else if (gType.equals("LineString")) {
				JSONArray lineNew = (JSONArray) coordinates;
				linesStore = addLine(linesStore, lineNew, logger);
			}
		}
		Map[] lines = new Map[linesStore.size()];
		linesStore.toArray(lines);
		
		return lines;	
	}
	
	private List<Map> addLine(List<Map> linesStore, JSONArray lineNew, LambdaLogger logger) {
		List<Integer[]> lineOldStore = new ArrayList<Integer[]>();
		for (int k = 0; k < lineNew.size(); k++) {
			JSONArray pointNewRaw = (JSONArray) lineNew.get(k);
			Double[] pointNew = new Double[pointNewRaw.size()];
			for (int l = 0; l < pointNewRaw.size(); l++) {
	            pointNew[l] = Double.valueOf(String.valueOf(pointNewRaw.get(l)));
	        }
			Integer[] pointOld = { (int) Math.round(pointNew[0]), (int) Math.round(pointNew[1]) };
			lineOldStore.add(pointOld);
		}
		Integer[][] lineOld = new Integer[lineOldStore.size()][2];
		lineOldStore.toArray(lineOld);
		Integer[][][] topLine = { lineOld };
		Map line = new LinkedHashMap();
		line.put("paths", topLine);
		JSONObject wkid = new JSONObject();
		wkid.put("wkid", 3067);
		line.put("spatialReference", wkid);
		linesStore.add(line);
		return linesStore;
	}
	
	private Map[] streamPisteet(Map[] pisteet, Boolean min, LambdaLogger logger) {
		List<Map> pisteetStore = new ArrayList<Map>();
		Integer minMaxOsa = null;
		Integer minMaxEtaisyys = null;
		for (Map piste: pisteet) {
			Boolean hitOsa = min ? (minMaxOsa == null || Integer.valueOf(piste.get("osa").toString()) <= minMaxOsa) : (minMaxOsa == null || Integer.valueOf(piste.get("osa").toString()) >= minMaxOsa);
			if (hitOsa) {
				minMaxOsa = Integer.valueOf(piste.get("osa").toString());
			}
		}
		for (Map piste: pisteet) {
			if ((Integer.valueOf(piste.get("osa").toString())).equals(minMaxOsa)) {
				Boolean hitEtaisyys = min ? (minMaxEtaisyys == null || Integer.valueOf(piste.get("etaisyys").toString()) <= minMaxEtaisyys) : (minMaxEtaisyys == null || Integer.valueOf(piste.get("etaisyys").toString()) >= minMaxEtaisyys);
				if (hitEtaisyys) {
					minMaxEtaisyys = Integer.valueOf(piste.get("etaisyys").toString());
				}
			}
		}
		for (Map piste: pisteet) {
			Boolean hit = (Integer.valueOf(piste.get("osa").toString()).equals(minMaxOsa) && Integer.valueOf(piste.get("etaisyys").toString()).equals(minMaxEtaisyys));
			if (hit) {
				pisteetStore.add(piste);
			}
		}
		Map[] pisteetStreamed = new Map[pisteetStore.size()];
		pisteetStore.toArray(pisteetStreamed);
		return pisteetStreamed;
	}
	
	private Double getEpsilon(JSONArray features, LambdaLogger logger) {
//		Integer numPositions = 0;
//		for (int i = 0; i < features.size(); i++) {
//			JSONObject feature = (JSONObject) features.get(i);
//			JSONObject geometry = (JSONObject) feature.get("geometry");
//			String gType = String.valueOf(geometry.get("type"));
//			JSONArray coordinates = (JSONArray) geometry.get("coordinates");
//			if (gType.equals("MultiLineString")) {
//				for (int j = 0; j < coordinates.size(); j++) {
//					JSONArray line = (JSONArray) coordinates.get(j);
//					numPositions = numPositions + line.size();
//				}
//			}
//		}
//		logger.log("NumPositions: " + numPositions.toString());
//		if (numPositions < 1000)
//			return 0.001;
//		else
//			return (double) (numPositions / 2000);
		return (double) (this.distance / 100000);
	}
	
	public Double[][] douglasPeucker(Double[][] arrayList, double epsilon, LambdaLogger logger) {
		List<Double[]> list = new ArrayList<Double[]>();
		Integer length = arrayList.length;
		for (int i = 0; i < length; i++) {
			list.add(arrayList[i]);
		}
        final List<Double[]> resultList = new ArrayList<Double[]>();
        douglasPeucker(list, 0, list.size(), epsilon, resultList);
        Double[][] simplifiedArrayList = new Double[(resultList.size())][3];
        resultList.toArray(simplifiedArrayList);
        return simplifiedArrayList;
    }
	
	private void douglasPeucker(List<Double[]> list, int s, int e, double epsilon, List<Double[]> resultList) {
        double dmax = 0;
        int index = 0;

        final int start = s;
        final int end = e-1;
        for (int i=start+1; i<end; i++) {
            final double px = list.get(i)[0];
            final double py = list.get(i)[1];
            final double vx = list.get(start)[0];
            final double vy = list.get(start)[1];
            final double wx = list.get(end)[0];
            final double wy = list.get(end)[1];
            final double d = perpendicularDistance(px, py, vx, vy, wx, wy); 
            if (d > dmax) {
                index = i;
                dmax = d;
            }
        }
        if (dmax > epsilon) {
            douglasPeucker(list, s, index, epsilon, resultList);
            douglasPeucker(list, index, e, epsilon, resultList);
        } else {
            if ((end-start)>0) {
                resultList.add(list.get(start));
                resultList.add(list.get(end));   
            } else {
                resultList.add(list.get(start));
            }
        }
    }
	
	private double perpendicularDistance(double px, double py, double vx, double vy, double wx, double wy) {
        return Math.sqrt(distanceToSegmentSquared(px, py, vx, vy, wx, wy));
    }
	
	private double distanceToSegmentSquared(double px, double py, double vx, double vy, double wx, double wy) {
        final double l2 = distanceBetweenPoints(vx, vy, wx, wy);
        if (l2 == 0) 
            return distanceBetweenPoints(px, py, vx, vy);
        final double t = ((px - vx) * (wx - vx) + (py - vy) * (wy - vy)) / l2;
        if (t < 0) 
            return distanceBetweenPoints(px, py, vx, vy);
        if (t > 1) 
            return distanceBetweenPoints(px, py, wx, wy);
        return distanceBetweenPoints(px, py, (vx + t * (wx - vx)), (vy + t * (wy - vy)));
    }
	
	private double distanceBetweenPoints(double vx, double vy, double wx, double wy) {
        return sqr(vx - wx) + sqr(vy - wy);
    }
	
	private double sqr(double x) { 
        return Math.pow(x, 2);
    }
}