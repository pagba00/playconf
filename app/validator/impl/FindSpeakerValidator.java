package validator.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import play.libs.F.Function0;
import play.libs.F.Promise;
import beans.FindSpeaker;
import validator.JsonInputValidator;

public class FindSpeakerValidator implements JsonInputValidator<FindSpeaker> {
	public Promise<Map<String, List<String>>> validate(final FindSpeaker input){
		return Promise.promise(new Function0<Map<String, List<String>>>(){
			public Map<String, List<String>> apply() throws Throwable {
				return validateInput(input);
			}
		});
	}
	
	private Map<String, List<String>> validateInput(FindSpeaker input) {
		Map<String, List<String>> errors = new HashMap<String, List<String>>();
		List<String> nameErrors = new ArrayList<String>();
		List<String> emailErrors = new ArrayList<String>();
		List<String> twitterIdErrors = new ArrayList<String>();
		
		if(StringUtils.isEmpty(input.getName())
				&& StringUtils.isEmpty(input.getEmail())
				&& StringUtils.isEmpty(input.getTwitterId())){
			List<String> errorList = new ArrayList<String>();
			errorList.add("No search criteria provided");
			errors.put("inputJson", errorList);
		}
		
		if(StringUtils.contains(input.getName(), "'")){
			nameErrors.add("Cannot search with illegal character \"'\"");
		}
		
		if(StringUtils.contains(input.getEmail(), "'")){
			emailErrors.add("Cannot search with illegal character \"'\"");
		}
		
		if(StringUtils.contains(input.getTwitterId(), "'")){
			twitterIdErrors.add("Cannot search with illegal character \"'\"");
		}
		
		if(nameErrors.size() > 0){
			errors.put("name", nameErrors);
		}
		
		if(emailErrors.size() > 0){
			errors.put("email", emailErrors);
		}
		
		if(twitterIdErrors.size() > 0){
			errors.put("twitterId", twitterIdErrors);
		}

		return errors;
	}

}
