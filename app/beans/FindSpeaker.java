package beans;

import java.io.Serializable;

public class FindSpeaker implements Serializable{

	private static final long serialVersionUID = -3820047860488070435L;
	private String name;
	private String email;
	private String twitterId;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getTwitterId() {
		return twitterId;
	}
	public void setTwitterId(String twitterId) {
		this.twitterId = twitterId;
	}
}
