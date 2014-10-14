package models;

import javax.persistence.*;
import javax.validation.Valid;

import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class Proposal extends Model {
	
	private static final long serialVersionUID = 2L;

	@Id
	public Long id;
	
	@Required
	public String title;
	
	@Required
	@MinLength(value = 10)
	@MaxLength(value = 1000)
	@Column(length = 1000)
	public String proposal;
	
	@Required
	public SessionType type = SessionType.OneHourTalk;
	
	@Required
	public Boolean isApproved = false;
	
	public String keywords;
	
	@Valid
	@OneToOne(cascade = CascadeType.ALL)
	public Speaker speaker;
}
