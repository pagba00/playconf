package repository;

import java.util.ArrayList;
import java.util.List;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;

import beans.FindSpeaker;
import common.DBExecutionContext;
import play.Logger;
import play.db.ebean.Model.Finder;
import play.libs.F.Function;
import play.libs.F.Function0;
import play.libs.F.Promise;
import repository.common.SQLEnum;
import models.Speaker;

public class SpeakerRepository {
	private static Finder<Long, Speaker> find = new Finder<Long, Speaker>(Long.class, Speaker.class);
	
	private static List<Speaker> emptySpeakerList(){
		List<Speaker> list = new ArrayList<Speaker>();
		return list;
	}
	
	public static Promise<List<Speaker>> findAll(){
		return Promise.promise(new Function0<List<Speaker>>(){
			public List<Speaker> apply() throws Throwable {
				return find.all();
			}
		}, DBExecutionContext.getCtx()).recover(new Function<Throwable, List<Speaker>>(){
			public List<Speaker> apply(Throwable t) throws Throwable {
				Logger.error("unable to fetch speakers from database", t);
				return emptySpeakerList();
			}
		}, DBExecutionContext.getCtx());
	}
	
	public static Promise<List<Speaker>> findSpeaker(final FindSpeaker speaker){
		return Promise.promise(new Function0<List<Speaker>>(){
			public List<Speaker> apply() throws Throwable {
				Query<Speaker> query = Ebean.createQuery(Speaker.class);
				StringBuffer queryBuilder = new StringBuffer();
				queryBuilder.append(String.format(SQLEnum.TO_LOWER.value(), "name")).append(SQLEnum.LIKE.value()).append(":name")
							.append(SQLEnum.OR.value())
							.append(String.format(SQLEnum.TO_LOWER.value(), "email")).append(SQLEnum.LIKE.value()).append(":email")
							.append(SQLEnum.OR.value())
							.append(String.format(SQLEnum.TO_LOWER.value(), "twitterId")).append(SQLEnum.LIKE.value()).append(":twitterId");
				query.where(queryBuilder.toString())
					.setParameter("name", String.join(SQLEnum.WC_PERCENT.value(), speaker.getName(), SQLEnum.WC_PERCENT.value()))
					.setParameter("email", String.join(SQLEnum.WC_PERCENT.value(), speaker.getEmail(), SQLEnum.WC_PERCENT.value()))
					.setParameter("twitterId", String.join(SQLEnum.WC_PERCENT.value(), speaker.getTwitterId(), SQLEnum.WC_PERCENT.value()));
				List<Speaker> res = query.findList();
				Logger.info(query.getGeneratedSql());
				return res;
			}
		}, DBExecutionContext.getCtx()).recover(new Function<Throwable, List<Speaker>>(){
			public List<Speaker> apply(Throwable t) throws Throwable {
				Logger.error("unable to fetch speakers from database", t);
				return emptySpeakerList();
			}
		}, DBExecutionContext.getCtx());
	}
}
