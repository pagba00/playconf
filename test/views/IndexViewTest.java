package views;

import static helpers.TestSetup.*;
import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;
import models.Proposal;
import models.Speaker;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import play.api.templates.Html;
import play.mvc.Http.Context;

public class IndexViewTest {
	@Test
	public void indexViewShouldRenderKeynoteInformation(){
		running(fakeApplication(testGlobalSettings()), new Runnable(){
			public void run() {
				Context.current.set(testHttpContext());
				Proposal s = sampleProposal();
				Speaker speaker = sampleSpeaker();
				s.speaker = speaker;
				Html html = views.html.index.render(s);
				Document doc = Jsoup.parse(contentAsString(html));
				assertThat(doc.select("#title").text()).isEqualTo("Keynote - " + s.title);
				assertThat(doc.select("#speakerName").text()).isEqualTo(speaker.name);
			}
		});
	}
}
