package mrthinger.shubi.type;

public class Account {

	private String user;
	private String pass;
	private String type;
	
	public Account(String user, String pass){
		this.user = user;
		this.pass = pass;
	}
	
	public Account(String user, String pass, String type) {
		this(user, pass);
		this.type = type;
	}
	
	public String getType(){
		return type;
	}
	
	public void setType(String type){
		this.type = type;
	}
	
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPass() {
		return pass;
	}
	public void setPass(String pass) {
		this.pass = pass;
	}
	
	@Override
	public String toString(){
		return user;
	}
	
	//If username and type are same its a duplicate account
	public boolean equalsAccount(Account a){
		return a.getUser().equals(user) && a.getType().equals(type);
	}
	
}
