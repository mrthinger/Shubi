package mrthinger.shubi.type;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class AccountStore {

	private ArrayList<Account> nikeAccounts;
	private ArrayList<Account> ppAccounts;
	//paypal iterator
	private int ppI;
	private ArrayList<ShippingAccount> shippingAccounts;
	//shipping iterator
	private int spI;
	private ArrayList<CreditCard> creditCards;
	//cc iterator
	private int ccI;
	
	private ArrayList<AdidasGuestCheckout> adidasGuestCheckouts;

	public AccountStore(AccountStore as){
		this();
		nikeAccounts.addAll(as.getNikeAccounts());
		ppAccounts.addAll(as.getPaypalAccounts());
		shippingAccounts.addAll(as.getShippingAccounts());
		creditCards.addAll(as.getCreditCards());
		adidasGuestCheckouts.addAll(as.getAdidasGuestCheckouts());
	}

	public AccountStore() {
		nikeAccounts = new ArrayList<>();
		ppAccounts = new ArrayList<>();
		shippingAccounts = new ArrayList<>();
		ppI = 0;
		spI = 0;
		
		creditCards = new ArrayList<>();
		ccI = 0;
		
		adidasGuestCheckouts = new ArrayList<>();
	}

	public void addAccounts(File file) throws FileNotFoundException{
		Scanner s = new Scanner(file);

		while(s.hasNextLine()){
			String line = s.nextLine();
			addAccount(line);			

		}
		s.close();
	}

	public void addAccount(String account) {
		StringTokenizer ln = new StringTokenizer(account, ":");

		String keyword = ln.nextToken().toUpperCase();
		boolean add = true;

		//Nikelaunch Account
		if(keyword.equals("NIKE")){
			Account a = new Account(ln.nextToken(), ln.nextToken(), keyword); 
			for(Account nA : nikeAccounts){
				if(a.equalsAccount(nA)){
					add = false;
					break;
				}
			}
			if(add) {
				nikeAccounts.add(a);
			}
		}
		//PaypalAccount
		else if(keyword.equals("PAYPAL")){
			Account pp = new Account(ln.nextToken(), ln.nextToken(), keyword);
			for(Account ppA : ppAccounts){
				if(pp.equalsAccount(ppA)){
					add = false;
					break;
				}
			}
			if(add) {
				ppAccounts.add(pp);
			}
		}
		
		//ShippingAccount
		else if(keyword.equals("SHIPPING")){
			ShippingAccount sppANew = new ShippingAccount(ln.nextToken(), ln.nextToken(), ln.nextToken(),
					ln.nextToken(), ln.nextToken(), ln.nextToken(), ln.nextToken());
			for(ShippingAccount sppAOld : shippingAccounts){
				if(sppANew.equalsShipping(sppAOld)){
					add = false;
					break;
				}
			}
			if(add) {
				shippingAccounts.add(sppANew);
			}
		}
		
		//CreditCard
		else if(keyword.equals("CC")){
			CreditCard ccNew = new CreditCard(ln.nextToken(), ln.nextToken(), ln.nextToken(),
					ln.nextToken(), ln.nextToken(), ln.nextToken(), ln.nextToken(), ln.nextToken(), ln.nextToken(),
					ln.nextToken(), ln.nextToken(), ln.nextToken());
			for(CreditCard ccOld : creditCards){
				if(ccNew.equalsCardNumber(ccOld)){
					add = false;
					break;
				}
			}
			if(add) {
				creditCards.add(ccNew);
			}
		}
		
		//Adidas Guest Checkout
		else if(keyword.equals("AGC")){
			CreditCard ccNew = new CreditCard(ln.nextToken(), ln.nextToken(), ln.nextToken(),
					ln.nextToken(), ln.nextToken(), ln.nextToken(), ln.nextToken(), ln.nextToken(), ln.nextToken(),
					ln.nextToken(), ln.nextToken(), ln.nextToken());
			ShippingAccount saNew = new ShippingAccount(ln.nextToken(), ln.nextToken(), ln.nextToken(), ln.nextToken(),
					ln.nextToken(), ln.nextToken(), ln.nextToken());
			String email = ln.nextToken();
			
			AdidasGuestCheckout agc = new AdidasGuestCheckout(ccNew, saNew, email);
			
			for(AdidasGuestCheckout currentAgc : adidasGuestCheckouts){
				//TODO: MAKE PROPER EQUALS CHECKING
				if(currentAgc.equals(agc)){
					add = false;
				}
			}
			
			addAccount(ccNew.toString());
			addAccount(saNew.toString());
			
			if(add){
				adidasGuestCheckouts.add(agc);
			}

			
		}
	}

	public Account nextPaypal(){
		if (ppI >= ppAccounts.size()){
			ppI = 0;
		}
		Account pp = ppAccounts.get(ppI);
		ppI++;
		return pp;
	}

	public ShippingAccount nextShipping(){
		if (spI >= shippingAccounts.size()){
			spI = 0;
		}
		ShippingAccount sp = shippingAccounts.get(spI);
		spI++;
		return sp;
	}
	
	public CreditCard nextCreditCard(){
		if (ccI >= creditCards.size()){
			ccI = 0;
		}
		CreditCard cc = creditCards.get(spI);
		ccI++;
		return cc;
	}
	
	public ArrayList<Account> getAllAccounts(){
		ArrayList<Account> allAccounts = new ArrayList<>();
		if(nikeAccounts != null){
			allAccounts.addAll(nikeAccounts);
		}
		if(ppAccounts != null){
			allAccounts.addAll(ppAccounts);
		}
		return allAccounts;
	}

	public boolean hasShippingAccount(){
		return shippingAccounts != null;
	}

	public ArrayList<Account> getNikeAccounts(){
		return nikeAccounts;
	}

	public boolean isNikeAccountAvailable(){
		return nikeAccounts.size() > 0;
	}

	public ArrayList<Account> getPaypalAccounts(){
		return ppAccounts;
	}

	public ArrayList<ShippingAccount> getShippingAccounts(){
		return shippingAccounts;
	}

	public Account takeOutNikeAccount(){
		Account a = nikeAccounts.get(0);
		nikeAccounts.remove(a);
		return a;
	}

	public void returnNikeAccount(Account a){
		nikeAccounts.add(a);
	}

	public ArrayList<CreditCard> getCreditCards() {
		return creditCards;
	}

	public ArrayList<AdidasGuestCheckout> getAdidasGuestCheckouts() {
		return adidasGuestCheckouts;
	}


}
