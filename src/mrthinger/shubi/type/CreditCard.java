package mrthinger.shubi.type;

public class CreditCard extends ShippingAccount{
	//creditcard - billing add ^^ + cc
	//email
	//cc Name on Card-card#-mm/yyyy exp-securitycode
	
	private String nameOnCard;
	private String cardNum;
	private String expMonth;
	private String expYear;
	private String securityCode;
	
	public CreditCard(String firstName, String lastName, String streetAdr, String city, String state, String zip,
			String phone, String nameOnCard, String cardNum, String expMonth, String expYear, String securityCode) {
		super(firstName, lastName, streetAdr, city, state, zip, phone);
		this.nameOnCard = nameOnCard;
		this.cardNum = cardNum;
		this.expMonth = expMonth;
		this.expYear = expYear;
		this.securityCode = securityCode;
	}

	@Override
	public String toString(){
		String toReturn = "CC:" + getFirstName() + ":" + getLastName() + ":" + getStreetAdr() + ":" + getCity() + ":" + getState()
		 + ":" + getZip() + ":" + getPhone() + ":" + getNameOnCard() + ":" + getCardNum() + ":" + getExpMonth() + ":" + getExpYear()
		 + ":" + getSecurityCode();
		return toReturn;
	}
	
	public String getNameOnCard() {
		return nameOnCard;
	}

	public void setNameOnCard(String nameOnCard) {
		this.nameOnCard = nameOnCard;
	}

	public String getCardNum() {
		return cardNum;
	}

	public void setCardNum(String cardNum) {
		this.cardNum = cardNum;
	}

	public String getExpMonth() {
		return expMonth;
	}

	public void setExpMonth(String expMonth) {
		this.expMonth = expMonth;
	}

	public String getExpYear() {
		return expYear;
	}

	public void setExpYear(String expYear) {
		this.expYear = expYear;
	}

	public String getSecurityCode() {
		return securityCode;
	}

	public void setSecurityCode(String securityCode) {
		this.securityCode = securityCode;
	}
	
	public boolean equalsCardNumber(CreditCard cc){
		return cc.getCardNum().equals(this.cardNum);
	}

}
