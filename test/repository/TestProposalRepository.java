package repository;

import static helpers.TestSetup.*;
import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;
import models.Proposal;
import models.Speaker;

import org.junit.Test;

import com.avaje.ebean.Ebean;

public class TestProposalRepository {
	@Test
	public void testSaveNewProposal(){
		running(fakeApplication(testGlobalSettings()), new Runnable(){
			public void run() {
				Proposal s = sampleProposal();
				s.save();
				assertThat(rowCount()).isEqualTo(1);
			}
		});
	}
	
	@Test
	public void testSaveNewProposalWithSpeaker(){
		running(fakeApplication(testGlobalSettings()), new Runnable(){
			public void run() {
				Proposal s = sampleProposal();
				s.speaker = sampleSpeaker();
				s.save();
				assertThat(rowCount()).isEqualTo(1);
				assertThat(Ebean.find(Speaker.class).findUnique().name).isEqualTo("Nilanjan");
			}
		});
	}
	
	private int rowCount() {
		return Ebean.find(Proposal.class).findRowCount();
	}
}
