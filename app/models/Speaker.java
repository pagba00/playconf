package models;

import javax.persistence.*;

import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class Speaker extends Model {
	
	private static final long serialVersionUID = 1L;

	@Id
	public Long id;
	
	@Required
	public String name;
	
	@Required
	@Email
	public String email;
	
	@Required
	@MinLength(value = 10)
	@MaxLength(value = 1000)
	@Column(length = 1000)
	public String bio;
	
	@Required
	public String twitterId;
	
	@Required
	public String pictureUrl;
}
