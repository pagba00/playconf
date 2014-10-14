package global;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;

import external.services.OAuthService;
import external.services.impl.TwitterOAuthService;
import actors.EventPublisher;
import actors.messages.RandomlySelectedTalkEvent;
import akka.actor.ActorRef;
import models.Proposal;
import play.Application;
import play.GlobalSettings;
import play.Play;
import play.libs.Akka;
import play.libs.F;
import play.libs.F.Callback;
import play.libs.F.Promise;
import play.mvc.Http.RequestHeader;
import play.mvc.Results;
import play.mvc.SimpleResult;
import repository.ProposalRepository;
import scala.concurrent.duration.Duration;
import validator.JsonInputValidator;
import validator.impl.FindSpeakerValidator;

public class PlayConfGlobal extends GlobalSettings {
	Injector injector = Guice.createInjector(new AbstractModule(){
		@SuppressWarnings("rawtypes")
		@Override
		protected void configure() {
			bind(ActorRef.class).toProvider(new Provider<ActorRef>(){
				public ActorRef get() {
					return EventPublisher.ref;
				}
			});
			bind(OAuthService.class).toProvider(new Provider<OAuthService>(){
				public OAuthService get() {
					return new TwitterOAuthService(Play.application().configuration().getString("twitter.consumer.key"), 
													Play.application().configuration().getString("twitter.consumer.secret"));
				}
			});
			bind(Map.class).toProvider(new Provider<Map<String, JsonInputValidator>>(){
				public Map get() {
					Map<String, JsonInputValidator> validators = new HashMap<String, JsonInputValidator>();
					validators.put("FindSpeakerValidator", new FindSpeakerValidator());
					return validators;
				}
			});
		}
	});
	
	
	@Override
	public <A> A getControllerInstance(Class<A> clazz) throws Exception{
		return injector.getInstance(clazz);
	}
	
	@Override
	public void onStart(Application arg0){
		super.onStart(arg0);
		Akka.system().scheduler().schedule(Duration.create(1, TimeUnit.SECONDS),
				Duration.create(10, TimeUnit.SECONDS),
				selectRandomTalks(),
				Akka.system().dispatcher());
	}
	
	private Runnable selectRandomTalks() {
		return new Runnable(){
			public void run() {
				Promise<Proposal> proposal = ProposalRepository.selectRandomTalk();
				proposal.onRedeem(new Callback<Proposal>(){
					public void invoke(Proposal p) throws Throwable {
						EventPublisher.ref.tell(new RandomlySelectedTalkEvent(p), ActorRef.noSender());
					}
				});
			}
		};
	}

	@Override
	public Promise<SimpleResult> onHandlerNotFound(RequestHeader arg0) {
		return F.Promise.<SimpleResult>pure(Results.notFound(views.html.error.render()));
	}
	@Override
	public Promise<SimpleResult> onError(RequestHeader arg0, Throwable arg1) {
		return F.Promise.<SimpleResult>pure(Results.internalServerError(views.html.error.render()));
	}
}
