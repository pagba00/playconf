package controllers;

import actors.messages.CloseConnectionEvent;
import actors.messages.NewConnectionEvent;
import actors.messages.NewProposalEvent;
import actors.messages.UserRegistrationEvent;
import akka.actor.ActorRef;
import beans.FindSpeaker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import external.services.OAuthService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import play.libs.F.Callback;
import play.libs.F.Callback0;
import play.libs.F.Function;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.libs.F.Tuple;
import play.libs.Json;
import play.libs.OAuth.RequestToken;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import play.data.Form;
import models.*;
import repository.ProposalRepository;
import repository.SpeakerRepository;
import validator.JsonInputValidator;

@Singleton
public class Application extends Controller {
    private static final Form<Proposal> proposalForm = Form.form(Proposal.class);
	
	private final ActorRef ref;
	private final OAuthService oauthService;
	@SuppressWarnings("rawtypes")
	private final Map<String, JsonInputValidator> validators;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Inject
	public Application(ActorRef ref, OAuthService oauthService, Map validators){
    	this.ref = ref;
    	this.oauthService = oauthService;
    	this.validators = validators;
    }
	
	public Promise<Result> index() {
        Promise<Proposal> keynote = ProposalRepository.findKeynote();
    	Promise<Result> result = keynote.map(new Function<Proposal, Result>(){
    		public Result apply(Proposal keynote) throws Throwable {
    			return ok(views.html.index.render(keynote));
			}
    	});
        return result;
    }
    
    public Result newProposal() {
        return ok(views.html.newProposal.render(proposalForm));
    }
    
    public Promise<Result> submitProposal() {
        Form<Proposal> submittedForm = proposalForm.bindFromRequest();
        if(submittedForm.hasErrors()){
        	return Promise.<Result>pure(ok(views.html.newProposal.render(submittedForm)));
        } else{
        	final Proposal proposal = submittedForm.get();
        	Promise<Result> res = ProposalRepository.save(proposal).map(new Function<Void, Result>(){
        		public Result apply(Void arg0) throws Throwable {
					flash("message", "Thanks for submitting new proposal");
					ref.tell(new NewProposalEvent(proposal), ActorRef.noSender());
		        	return redirect(routes.Application.index());
				}
        		
        	});
        	return res;
        }
    }
    
    public WebSocket<JsonNode> buzz(){
    	return new WebSocket<JsonNode>(){
    		public void onReady(play.mvc.WebSocket.In<JsonNode> in,
					play.mvc.WebSocket.Out<JsonNode> out) {
				final String uuid = UUID.randomUUID().toString();
				ref.tell(new NewConnectionEvent(uuid, out), ActorRef.noSender());
				in.onClose(new Callback0(){
					public void invoke() throws Throwable {
						ref.tell(new CloseConnectionEvent(uuid), ActorRef.noSender());
					}
				});
			}
    	};
    }
    
    public Result register() {
    	String callbackUrl = routes.Application.registerCallback().absoluteURL(request());
        Tuple<String, RequestToken> t = oauthService.retrieveRequestToken(callbackUrl);
        flash("request_token", t._2.token);
        flash("request_secret", t._2.secret);
    	return redirect(t._1);
    }
    
    public Result registerCallback() {
        RequestToken token = new RequestToken(flash("request_token"), flash("request_secret"));
        
        String authVerifier = request().getQueryString("oauth_verifier");
        
        Promise<JsonNode> userProfile = oauthService.registeredUserProfile(token, authVerifier);
        userProfile.onRedeem(new Callback<JsonNode>(){

            public void invoke(JsonNode twitterJson) throws Throwable {
                RegisteredUser user = RegisteredUser.fromJson(twitterJson);
                user.save();
                ref.tell(new UserRegistrationEvent(user), ActorRef.noSender());
            }
            
        });
        return redirect(routes.Application.index());
    }
    
    public Promise<Result> getAllSpeakers(){
    	final Promise<List<Speaker>> speakers = SpeakerRepository.findAll();
    	Promise<Result> result = speakers.map(new Function<List<Speaker>, Result>(){
    		public Result apply(List<Speaker> spk) throws Throwable {
    			return ok(Json.toJson(spk));
			}
    	});
    	return result;
    }
    
    @BodyParser.Of(BodyParser.Json.class)
    public Promise<Result> findSpeakers(){
    	JsonNode json;
    	FindSpeaker find = new FindSpeaker();
    	Promise<Map<String, List<String>>> errorsPromise;
    	Map<String, List<String>> errors;
    	try {
    		json = request().body().asJson();
    		find = Json.fromJson(json, FindSpeaker.class);
    		errorsPromise = validate(find, "FindSpeakerValidator");
    		errors = errorsPromise.get(2500);
		} catch (final Exception e) {
			return errorToJson(e);
		}
    	if(errors.size() > 0){
    		return validationErrorToJson(errors);
    	}
    	final Promise<List<Speaker>> speakers = SpeakerRepository.findSpeaker(find);
    	Promise<Result> result = speakers.map(new Function<List<Speaker>, Result>(){
    		public Result apply(List<Speaker> spk) throws Throwable {
    			if(spk.size()==0){
    				ObjectNode result = Json.newObject();
    				List<String> errors = new ArrayList<String>();
    				errors.add("No speaker found with given criteria");
    				result.put("errors", Json.toJson(errors));
    				return ok(result);
    			}
    			return ok(Json.toJson(spk));
			}
    	});
    	return result;
    }

    private Promise<Result> errorToJson(final Exception e){
		final Promise<List<String>> emptySpk = emptyStringList();
		Promise<Result> jsonErr = emptySpk.map(new Function<List<String>, Result>(){
			public Result apply(List<String> spk) throws Throwable {
				ObjectNode result = Json.newObject();
				List<String> errors = new ArrayList<String>();
				Map<String, List<String>> map = new HashMap<String, List<String>>();
				map.put("inputJson", errors);
				errors.add(e.getMessage());
				result.put("errors", Json.toJson(map));
				return ok(result);
			}
		});
		return jsonErr;
	}
    
    private Promise<Result> validationErrorToJson(final Map<String, List<String>> e){
		final Promise<List<String>> emptySpk = emptyStringList();
		Promise<Result> jsonErr = emptySpk.map(new Function<List<String>, Result>(){
			public Result apply(List<String> spk) throws Throwable {
				ObjectNode result = Json.newObject();
				result.put("errors", Json.toJson(e));
				return ok(result);
			}
		});
		return jsonErr;
	}
	
	public static Promise<List<String>> emptyStringList(){
		return Promise.promise(new Function0<List<String>>(){
			public List<String> apply() throws Throwable {
				List<String> empty = new ArrayList<String>();
				return empty;
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	private Promise<Map<String, List<String>>> validate(Object input, String operation){
		return validators.get(operation).validate(input);
	}
}
