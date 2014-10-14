package repository.common;

public enum SQLEnum {
	LIKE(" LIKE "),
	WHERE(" WHERE "),
	EQ(" = "),
	OR(" OR "),
	SPACE(" "),
	TO_LOWER(" LOWER(%s) "),
	WC_PERCENT("%");
	
	String value;
	SQLEnum(String value){
		this.value = value;
	}
	
	public String value(){
		return value;
	}
}
