package mrthinger.shubi.sitedriver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import mrthinger.shubi.Info;
import mrthinger.shubi.SiteKey;
import mrthinger.shubi.gui.WindowMainController;
import mrthinger.shubi.task.Task;
import mrthinger.shubi.type.Account;
import mrthinger.shubi.type.AdidasGuestCheckout;
import mrthinger.shubi.type.SizeList;

public class FootsiteSiteDriver extends SiteDriver{

	//Product Paths
	//This item is available in: 2 hours 18 min 33 sec
	//unix time is also displayed on product page of launch
	//doesnt matter if you start sending cart requests slightly before release
	private final By timerPath = By.id("pdp_timer");
	private final By productPageRequestKeyPath = By.id("requestKey");
	private final By paypalCheckoutButtonPath = By.id("cart_paypal_button");
	private final By varsPath = By.xpath("//script[text()[contains(.,'questKey')]]");
	private By atcButton;
	private final By FL_EB_sizesContainerPath = By.xpath("//*[@id='size_selection_list']/a");
	private final By FA_sizesContainerPath = By.xpath("//*[@id='pdp_sizes']/ul/li/a");
	private final By CMPS_sizesContainerPath = By.xpath("//span[contains(@class,'product_sizes')]/a");

	//Minicart paths

	//take new request key if this is present
	private final By miniCartRequestKeyPath = By.xpath("//script[text()[contains(.,'requestKey')]]");

	//Only comes up on unsuccessful atc
	//GENERATES NEW REQUEST KEY -- valid request key will always come up in ^^ path
	//minicart out of stock
	//The requested item cannot be added to cart because it is out of stock..
	//All shoes in this size are currently in other customers' carts. These shoes may become available again if all transactions are not completed. We suggest you attempt adding your size to your cart again in a few minutes. To learn more, check this out.

	//other errors - DONT GEN REQ KEY
	//You May Be Able To Snag A Pair If You Try Again. Due to high demand, release shoes filter on to the site to avoid any problems.
	//Due to high demand, this product could not be added to cart. Please attempt your request again.
	private final By miniCartErrorPath = By.id("miniAddToCart_error");

	//only comes up on successful atc OR WILL GO DIRECTLY TO CART FOR YOU
	private final By miniCartViewCartPath = By.linkText("View Cart");

	private String sku;
	private boolean setup;

	public FootsiteSiteDriver(DesiredCapabilities dc, Task task) {
		super(dc, task);

		setup = false;
		manage().timeouts().pageLoadTimeout(300, TimeUnit.SECONDS);
		
		//TODO: ADD PAYMENT FUNCTIONALITY FOR FOOTSITES
		paymentButtonFunctionality=false;
		
		//Locate correct ATC button depending on site
		if(task.getSite().equals(SiteKey.FOOTLOCKER) || task.getSite().equals(SiteKey.CHAMPS) || task.getSite().equals(SiteKey.EASTBAY)){
			atcButton = By.name("pdp_addtocart");
		}else if(task.getSite().equals(SiteKey.FOOTACTION)){
			atcButton = By.id("addToCartLink");
		}

	}

	@Override
	public void pay(AdidasGuestCheckout ac){

	}

	@Override
	public int checkOut() throws InterruptedException{

		if(handleRequests() == ResponseCode.REQUEST_RESTART){
			return ResponseCode.RESTART;
		}

		//request key unique to browser session cookie
		//next request key is generated in miniadd to cart head(at least on successful add)
		//not sure if its important to get a unique request key for every add request or if a new one will
		//even generate on fail to add to cart (check for one if not use the same?)
		//browser session cookie value must stay constant inorder to use the request key


		task.setStatus("Going to product page.");

		try{
			get(task.getUrl());
		}catch(TimeoutException e){
			task.setStatus("Failed to connect to website! Proxy Error?");
			return ResponseCode.WAIT_FOR_USER;
		}
		
		//Check to see if we have a cart
		if(manage().getCookieNamed("CARTSKUS") == null){
			task.setHasCart(false);
		}

		//Set popup cookies
		if(!setup){
			manage().addCookie(new Cookie("disableAutoShowEmailOverlay0", "1"));
			manage().addCookie(new Cookie("cnx_sa", "1"));
			manage().addCookie(new Cookie("emailSignupSeen", "1"));
			setup = true;
		}

		if(handleRequests() == ResponseCode.REQUEST_RESTART){
			return ResponseCode.RESTART;
		}

		WebDriverWait smallWait = new WebDriverWait(this, 5);
		WebDriverWait mediumWait = new WebDriverWait(this, 15);

		long launchTime = 0;
		String requestKey = "";


		//new line regex [\\r\\n]+
		//var requestKey = 'D4A4C6FD2923b986';
		//var launchTime_150073 = 1485183600000;

		WebElement varsScript = null;
		task.setStatus("Waiting for page elements to load...");

		try{
			mediumWait.until(ExpectedConditions.presenceOfElementLocated(timerPath));
		}catch(TimeoutException e){
			task.setStatus("Page load imcomplete. Restarting!");
			return ResponseCode.RESTART;
		}

		try{
			varsScript = mediumWait.until(ExpectedConditions.presenceOfElementLocated(varsPath));
		}catch(TimeoutException e){
			task.setStatus("On website. Not product page.. Restarting!");
			return ResponseCode.RESTART;
		}

		boolean foundLaunchTime = false;
		String[] varsParts = varsScript.getAttribute("innerHTML").split("[\\r\\n]+");
		for(String var : varsParts){
			if(var.contains("questKey")){
				String[] parts = var.split("'");
				requestKey = parts[1];
			}else if(var.contains("launchTime")){
				String[] parts = var.split("=");
				String lTime = parts[1].trim();
				//TODO:FIX THIS
				lTime = lTime.substring(0, lTime.length()-1);
				//launchTime = Long.parseLong(lTime);
				launchTime = 0;
				foundLaunchTime = true;
			}
		}

		if(Info.FOOTSITE_VERBOSE)System.out.println("Request key: " + requestKey);

		//See if its up
		WebElement timer;
		try{
			timer = mediumWait.until(ExpectedConditions.presenceOfElementLocated(timerPath));
		}catch(TimeoutException e){
			task.setStatus("Couldnt find timer!");
			return ResponseCode.WAIT_FOR_USER;
		}

		//if product isnt up for purchase
		String timerStyle = timer.getAttribute("style");
		if((timerStyle != null && !timerStyle.contains("display: none")) && !isPresentAndDisplayed(atcButton)){
			if(Info.DEBUG)System.out.println("Launch time: " + launchTime);
			long deltaTime = 0;
			do{
				if(foundLaunchTime){
					long currentTime = System.currentTimeMillis();
					deltaTime = launchTime - currentTime;

					long hours = TimeUnit.MILLISECONDS.toHours(deltaTime);
					long mins = TimeUnit.MILLISECONDS.toMinutes(deltaTime) - TimeUnit.HOURS.toMinutes(hours);
					long secs = TimeUnit.MILLISECONDS.toSeconds(deltaTime) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(mins);
					task.setStatus("Waiting " + hours + ":" + mins + ":" + secs);
				}else{
					deltaTime = 1;
					task.setStatus("Waiting...");
				}

				Thread.sleep(50);
				if(handleRequests() == ResponseCode.REQUEST_RESTART){
					return ResponseCode.RESTART;
				}

				timerStyle = findElement(timerPath).getAttribute("style");
			}while(deltaTime > 0 || (timerStyle != null && !timerStyle.contains("display: none")) || isPresentAndDisplayed(atcButton));
		}

		//product is up for purchase
		else{
			task.setStatus("Product is up for purchase!");
		}

		//size settings
		String ogPrefSize = task.getSize();
		ArrayList<String> pickedSizes = new ArrayList<>();
		List<String> possibleSizes = getInStockSizes();

		boolean onCartPage = false;

		//Send atc requests
		int i = 1;
		while(true){

			//reload page + possible sizes every 100th
			if(i % 100 == 0){
				try{
					task.setStatus("Reloading sizes...");
					get(task.getUrl());

					try{
						mediumWait.until(ExpectedConditions.presenceOfElementLocated(timerPath));
					}catch(TimeoutException e){
						task.setStatus("Page load imcomplete. Restarting!");
						continue;
					}

				}catch(TimeoutException e){
					task.setStatus("Failed to connect to website! We already connected once.");
					return ResponseCode.WAIT_FOR_USER;
				}

				possibleSizes = getInStockSizes();
				if(possibleSizes.size() == 0){
					task.setStatus("No sizes in stock.");
					return ResponseCode.WAIT_FOR_USER;
				}
			}

			if(handleRequests() == ResponseCode.REQUEST_RESTART){
				return ResponseCode.RESTART;
			}
			//TODO: if no sizes were found
			String size = pickSize(ogPrefSize, possibleSizes, pickedSizes);
			String footSiteSize = SizeList.toFootsiteSize(size);
			task.setSize(size);
			task.setStatus("Sending atc request #" + i);

			By sizePath = null;
			if(task.getSite().equals(SiteKey.FOOTLOCKER) || task.getSite().equals(SiteKey.EASTBAY)){
				sizePath = By.xpath("//*[@id='size_selection_list']/a[text()[contains(.,'"+ footSiteSize+"')]]");
			}else if(task.getSite().equals(SiteKey.FOOTACTION)){
				sizePath = By.xpath("//*[@id='pdp_sizes']/ul/li/a[text()[contains(.,'"+ footSiteSize+"')]]");
			}else if(task.getSite().equals(SiteKey.CHAMPS)){
				sizePath = By.xpath("//span[contains(@class,'product_sizes')]/a[text()[contains(.,'"+ footSiteSize+"')]]");
			}
			
			jsClick(findElement(sizePath));

			//TODO: MAKE SURE POPUPCOMES OUT BEFORE CLICKING ATC AGAIN
			jsClick(findElement(atcButton));

			i++;

			if(handleRequests() == ResponseCode.REQUEST_RESTART){
				return ResponseCode.RESTART;
			}

			Thread.sleep(1000);

			if(manage().getCookieNamed("CARTSKUS") != null){
				if(Info.FOOTSITE_VERBOSE) System.out.println("Found cart cookie");
				task.setStatus("IN CART : GETTING TO SHOPPING CART");
				task.setHasCart(true);
				onCartPage = false;
				break;
			}

			if(handleRequests() == ResponseCode.REQUEST_RESTART){
				return ResponseCode.RESTART;
			}

		}

		//Get to cart
		if(!onCartPage){
			if(handleRequests() == ResponseCode.REQUEST_RESTART){
				return ResponseCode.RESTART;
			}
			get(task.getSite().getHomeUrl() + "shoppingcart/default.cfm");
			Thread.sleep(1000);
			if(handleRequests() == ResponseCode.REQUEST_RESTART){
				return ResponseCode.RESTART;
			}
			if(isPresentAndDisplayed(paypalCheckoutButtonPath)){
				task.setStatus("IN CART : ON SHOPPING CART");
				onCartPage = true;
			}
		}
		if(handleRequests() == ResponseCode.REQUEST_RESTART){
			return ResponseCode.RESTART;
		}

		
		boolean autoPPCheckout = false;
		//TODO: PAYMENTS
		if(autoPPCheckout){
			if(paypalCheckout() == ResponseCode.REQUEST_RESTART){
				return ResponseCode.RESTART;
			}	
		}
		

		task.setStatus("ATN! CONFIRM ORDER");

		if(task.getRemainingPairs() > 0){

			if(Info.ORDER_SHOES){
				System.out.println("Footsite ordering not yet implemented...");
			}else{
				System.out.println("Sandbox footsite Order");
			}

			task.decrementRemainingPairs();

		}else{
			if(Info.DEBUG) System.out.println("no more remaining pairs");
		}

		return ResponseCode.SUCCESS;
	}

	private int paypalCheckout() throws InterruptedException {

			//Click pp checkout
			task.setStatus("IN CART : Clicking paypal.");
			findElement(paypalCheckoutButtonPath).click();
			if(handleRequests() == ResponseCode.REQUEST_RESTART){
				return ResponseCode.RESTART;
			}

			Thread.sleep(2000);


			String ogWindowHandle = getWindowHandle();


			boolean ppPoppedUp = false;

			if(getWindowHandles().size() > 1){
				task.setStatus("CHECKING OUT : ON PP POPUP");
				ppPoppedUp = true;
				for(String winHandle : getWindowHandles()){
					switchTo().window(winHandle);
				}
			}else{
				task.setStatus("CHECKING OUT : ON Paypal");
			}

			if(handleRequests() == ResponseCode.REQUEST_RESTART){
				return ResponseCode.RESTART;
			}

			while(true){
				try{
					switchTo().frame("injectedUl");
					break;
				}catch(Exception e){
					if(handleRequests() == ResponseCode.REQUEST_RESTART){
						return ResponseCode.RESTART;
					}
					Thread.sleep(100);
				}
			}

			Account pp = task.nextPaypal();


			if(handleRequests() == ResponseCode.REQUEST_RESTART){
				return ResponseCode.RESTART;
			}
			waitForElementLoad(By.id("email"));
			Thread.sleep(1000);
			if(handleRequests() == ResponseCode.REQUEST_RESTART){
				return ResponseCode.RESTART;
			}
			findElement(By.id("email")).clear();
			findElement(By.id("email")).sendKeys(pp.getUser());
			if(handleRequests() == ResponseCode.REQUEST_RESTART){
				return ResponseCode.RESTART;
			}
			findElement(By.id("password")).clear();
			findElement(By.id("password")).sendKeys(pp.getPass());
			if(handleRequests() == ResponseCode.REQUEST_RESTART){
				return ResponseCode.RESTART;
			}
			findElement(By.id("btnLogin")).click();
			if(handleRequests() == ResponseCode.REQUEST_RESTART){
				return ResponseCode.RESTART;
			}

			if(ppPoppedUp){
				switchTo().window(ogWindowHandle);
			}
		
		return ResponseCode.SUCCESS;
	}

	private String genFootlockerAtcUrl(String requestKey, String footSiteSize) {
		//http://www.footlocker.com/catalog/miniAddToCart.cfm?secure=0&inlineAddToCart=1&qty=1&requestKey=54c231939D9CE1E5&size=10.0&sku=14571011
		String atcUrl = task.getSite().getHomeUrl()
				+ "catalog/miniAddToCart.cfm?secure=0&inlineAddToCart=1&qty=1"
				+ "&requestKey="
				+ requestKey
				+ "&size="
				+ footSiteSize
				+ "&sku="
				+ sku;
		return atcUrl;
	}

	private List<String> getInStockSizes(){

		List<String> possibleSizes = new ArrayList<>();

		if(task.getSite().equals(SiteKey.FOOTLOCKER) || task.getSite().equals(SiteKey.EASTBAY)){
			List<WebElement> possibleSizesElements = findElements(FL_EB_sizesContainerPath);
			for(WebElement e : possibleSizesElements){
				if(e.getAttribute("class").contains("in-stock")){
					possibleSizes.add(e.getAttribute("innerHTML").trim());
				}
			}
		}else if(task.getSite().equals(SiteKey.FOOTACTION)){
			List<WebElement> possibleSizesElements = findElements(FA_sizesContainerPath);
			for(WebElement e : possibleSizesElements){
				if(e.getAttribute("class").contains("available")){
					possibleSizes.add(e.getAttribute("innerHTML").trim());
				}
			}
		}else if(task.getSite().equals(SiteKey.CHAMPS)){
			List<WebElement> possibleSizesElements = findElements(CMPS_sizesContainerPath);
			for(WebElement e : possibleSizesElements){
				if(!e.getAttribute("class").contains("disabled")){
					possibleSizes.add(e.getAttribute("innerHTML").trim());
				}
			}
		}
		return possibleSizes;
	}

}
