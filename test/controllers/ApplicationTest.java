package controllers;

import static helpers.TestSetup.*;
import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

import java.util.Map;

import org.junit.Test;

import akka.actor.ActorRef;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.anyString;


import external.services.OAuthService;
import play.libs.F;
import play.libs.F.Tuple;
import play.libs.OAuth.RequestToken;
import play.mvc.Http.Context;
import play.mvc.Http.Flash;
import play.mvc.Http.Status;
import play.mvc.Result;
import play.test.Helpers;

public class ApplicationTest {
	@Test
	public void redirectToOAuthProviderForRegistration(){
		Context.current.set(testHttpContext());
		OAuthService service = mock(OAuthService.class);
		Tuple<String, RequestToken> t = new F.Tuple<String, RequestToken>(
				"twitter.redirect.url", new RequestToken("twitter.token", "twitter.secret"));
		when(service.retrieveRequestToken(anyString())).thenReturn(t);
		
		Application app = new Application(mock(ActorRef.class), service, mock(Map.class));
		
		Result result = app.register();
		
		assertThat(status(result)).isEqualTo(Status.SEE_OTHER);
		assertThat(Helpers.redirectLocation(result)).isEqualTo("twitter.redirect.url");
		
		Flash flash = Context.current().flash();
		
		assertThat(flash.get("request_token")).isEqualTo("twitter.token");
		assertThat(flash.get("request_secret")).isEqualTo("twitter.secret");
	}
}