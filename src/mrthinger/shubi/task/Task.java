package mrthinger.shubi.task;

import java.util.Map;

import mrthinger.shubi.IntPointer;
import mrthinger.shubi.SiteKey;
import mrthinger.shubi.gui.WindowMainController;
import mrthinger.shubi.sitedriver.SiteDriver;
import mrthinger.shubi.type.Account;
import mrthinger.shubi.type.AccountStore;
import mrthinger.shubi.type.Proxy;
import mrthinger.shubi.type.ShippingAccount;
import mrthinger.shubi.type.SizeList;

public class Task {

	private String shoe;
	private SiteKey site;
	private SizeList sizeList;
	private String size;
	private String url;
	private IntPointer remainingPairs;
	private Account userpass;
	private ShippingAccount shipping;
	private Proxy proxy;
	private SiteDriver driver;
	private String status;
	private AccountStore accountStore;
	private Map<String, String> extraInfo;
	
	private boolean hasCart;
	private boolean needsAtn;
	
	//Dynamic account definition via nextPaypal and nextShipping
	public Task(SiteKey site, String shoe, SizeList sizeList, String url, IntPointer remainingPairs, AccountStore accountStore) {
		this.site = site;
		this.shoe = shoe;
		this.sizeList = sizeList;
		this.size = sizeList.takeSize();
		this.url = url;
		this.remainingPairs = remainingPairs;
		this.accountStore = accountStore;
		this.hasCart = false;
		this.needsAtn = false;
		this.status = "Waiting to be executed...";
	}

	//Static account definition for nikelaunch only
	public Task(SiteKey site, String shoe, String size, String url, IntPointer remainingPairs, Account a, AccountStore accountStore) {
		this.site = site;
		this.shoe = shoe;
		this.size = size;
		this.url = url;
		this.remainingPairs = remainingPairs;
		this.accountStore = accountStore;
		this.userpass = a;
		this.status = "Waiting to be executed...";
		this.hasCart = false;
		this.needsAtn = false;
	}

	public Account nextPaypal(){
		Account pp = accountStore.nextPaypal();
		userpass = pp;
		return pp;
	}
	
	public ShippingAccount nextShipping(){
		ShippingAccount sp = accountStore.nextShipping();
		shipping = sp;
		return sp;
	}
	
	public void setProxy(Proxy proxy){
		this.proxy = proxy;
	}
	
	
	public String getStatus(){
		return status;
	}

	public void setStatus(String status){
		this.status = status;
	}
	
	public void setSize(String size){
		this.size = size;
	}
	
	public boolean hasProxy(){
		return proxy != null;
	}
	
	public int getRemainingPairs() {
		return remainingPairs.getI();
	}
	

	public void setRemainingPairs(int remainingPairs) {
		this.remainingPairs.setI(remainingPairs);
	}

	public int decrementRemainingPairs(){
		return this.remainingPairs.decrementI();
	}
	
	public Map<String, String> getExtraInfo() {
		return extraInfo;
	}

	public void setExtraInfo(Map<String, String> extraInfo) {
		this.extraInfo = extraInfo;
	}

	public String getShoe() {
		return shoe;
	}

	public SiteKey getSite() {
		return site;
	}

	public String getSize() {
		return size;
	}

	public String getUrl() {
		return url;
	}

	public Account getUserpass() {
		return userpass;
	}

	public ShippingAccount getShipping() {
		return shipping;
	}

	public Proxy getProxy() {
		return proxy;
	}

	public SiteDriver getDriver() {
		return driver;
	}

	public void setDriver(SiteDriver driver) {
		this.driver = driver;
	}
	
	public SizeList getSizeList(){
		return sizeList;
	}

	public boolean hasCart() {
		return hasCart;
	}

	public void setHasCart(boolean hasCart) {
		this.hasCart = hasCart;
	}

	public boolean needsAtn() {
		return needsAtn;
	}

	public void setNeedsAtn(boolean needsAtn) {
		this.needsAtn = needsAtn;
	}

	
	
}
