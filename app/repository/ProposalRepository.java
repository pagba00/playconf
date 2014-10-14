package repository;

import common.DBExecutionContext;

import play.Logger;
import play.db.ebean.Model.Finder;
import play.libs.F.Function;
import play.libs.F.Function0;
import play.libs.F.Promise;
import models.Proposal;
import models.SessionType;
import models.Speaker;

public class ProposalRepository {
	private static Finder<Long, Proposal> find = new Finder<Long, Proposal>(Long.class, Proposal.class);
	
	private static Proposal placeholder(){
		Proposal proposal = new Proposal();
		proposal.keywords = "";
		proposal.proposal = "";
		Speaker speaker = new Speaker();
		speaker.name = "";
		speaker.pictureUrl = "";
		speaker.twitterId = "";
		proposal.speaker = speaker;
		proposal.title = "Coming soon...";
		proposal.type = SessionType.Keynote;		
		return proposal;
	}
	
	public static Promise<Proposal> findKeynote() {
		return Promise.promise(new Function0<Proposal>(){
			public Proposal apply() throws Throwable {
				return find.where().eq("type", SessionType.Keynote).findUnique();
			}
		}, DBExecutionContext.getCtx()).recover(new Function<Throwable, Proposal>(){
					public Proposal apply(Throwable t) throws Throwable {
						Logger.error("unable to fetch keynotes from database", t);
						return placeholder();
					}
				}, DBExecutionContext.getCtx());
	}
	
	public static Promise<Void> save(final Proposal p){
		return Promise.promise(new Function0<Void>(){
			public Void apply() throws Throwable {
				p.save();
				return null;
			}
		}, DBExecutionContext.getCtx());
	}

	public static Promise<Proposal> selectRandomTalk() {
		return Promise.promise(new Function0<Proposal>(){
			public Proposal apply() throws Throwable {
				Long randomId = (long) (1 + Math.random() * (5 - 1));
				return find.byId(randomId);
			}
		}, DBExecutionContext.getCtx());
	}
}
