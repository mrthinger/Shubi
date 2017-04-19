package mrthinger.shubi.type;

public class AdidasGuestCheckout {

	private ShippingAccount shippingInfo;
	private CreditCard paymentInfo;
	private String email;

	public AdidasGuestCheckout(CreditCard paymentInfo, ShippingAccount shippingInfo, String email) {
		this.shippingInfo = shippingInfo;
		this.paymentInfo = paymentInfo;
		this.email = email;
	}

	public ShippingAccount getShippingInfo() {
		return shippingInfo;
	}

	public void setShippingInfo(ShippingAccount shippingInfo) {
		this.shippingInfo = shippingInfo;
	}

	public CreditCard getPaymentInfo() {
		return paymentInfo;
	}

	public void setPaymentInfo(CreditCard paymentInfo) {
		this.paymentInfo = paymentInfo;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		//get last 4 digits of CC# if more than 4 digits exist
		String last4 = "";
		if(getPaymentInfo().getCardNum().length() > 4){
			last4 = getPaymentInfo().getCardNum().substring(getPaymentInfo().getCardNum().length()-4);
		}else{
			last4 = getPaymentInfo().getCardNum();
		}
		
		sb.append("Bill: " + getPaymentInfo().getFirstName() + " " + getPaymentInfo().getLastName() + " -" + last4);
		sb.append(" Ship: " + getShippingInfo().getFirstName() + " " + getShippingInfo().getLastName());
		sb.append(" Email: " + getEmail());

		return sb.toString();
	}

}
