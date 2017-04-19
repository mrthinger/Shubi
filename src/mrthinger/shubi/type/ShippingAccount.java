package mrthinger.shubi.type;

public class ShippingAccount {

	//Evan:Pierce:23 Pawtucket Dr.:Cherry Hill:New Jersey:08003:8562177544

	
	private String firstName;
	private String lastName;
	private String streetAdr;
	private String city;
	private String state;
	private String zip;
	private String phone;
	
	public ShippingAccount(
			String firstName, String lastName, String streetAdr,
			String city, String state, String zip,
			String phone) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.streetAdr = streetAdr;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.phone = phone;
	}
	
	@Override
	public String toString(){
		String toReturn = "SHIPPING:" + getFirstName() + ":" + getLastName() + ":" + getStreetAdr() + ":" + getCity() + ":" + getState()
		 + ":" + getZip() + ":" + getPhone();
		return toReturn;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getStreetAdr() {
		return streetAdr;
	}

	public String getCity() {
		return city;
	}

	public String getState() {
		return state;
	}

	public String getZip() {
		return zip;
	}

	public String getPhone() {
		return phone;
	}
	
	public boolean equalsShipping(ShippingAccount spA){
		return spA.getStreetAdr().equals(streetAdr);
	}
	
	
}
