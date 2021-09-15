package mockVkm;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class Virhe {

	String virheteksti;
	
	public Virhe(String teksti, LambdaLogger logger, Boolean logger_on) {
		this.virheteksti = teksti;
	}
	
}
