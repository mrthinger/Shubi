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
import mrthinger.shubi.USAStateConverter;
import mrthinger.shubi.captcha.CaptchaBanker;
import mrthinger.shubi.gui.WindowMainController;
import mrthinger.shubi.task.Task;
import mrthinger.shubi.type.Account;
import mrthinger.shubi.type.AdidasGuestCheckout;

public class AdidasSiteDriver extends SiteDriver{

	private CaptchaBanker cB;
	private boolean setupSite;

	private final By addCartPath = By.name("add-to-cart-button");
	private final By sizesContainerPath = By.xpath("//div[@class='ffSelectMenuMid' and .//ul[.//li[.//span[text()[contains(.,'size')]]]]]/ul/li");
	private final By sizesShieldPath = By.xpath("//*[@class='ffSelectButton' and (.//span[text()[contains(.,'Size')]] or .//span[text()[contains(.,'size')]])]");

	private final By sizeSelectOptionsPath = By.xpath("//select[@name='pid']/option");

	private final By checkoutButtonPath = By.xpath("//div[@class='minicart_summery']/*[@title='Checkout']");

	private final By captchaPath = By.xpath("//iframe[contains(@src,'recaptcha')]");

	private final By reviewButtonPath = By.id("goToReviews");
	private final By paypalButtonPath = By.xpath("//a/button");


	//Setup waits
	private WebDriverWait smallWait;
	private WebDriverWait mediumWait;

	public AdidasSiteDriver(DesiredCapabilities dc, Task task){
		super(dc, task);
		setupSite = false;

		smallWait = new WebDriverWait(this, 5);
		mediumWait = new WebDriverWait(this, 10);

		manage().timeouts().pageLoadTimeout(300, TimeUnit.SECONDS);

		if(task.getSite().equals(SiteKey.ADIDAS_US)){
		//	cB = new CaptchaBanker(task.getSite().getHomeUrl(), Info.ADIDAS_US_SITEKEY);
		}else{
		//	cB = new CaptchaBanker(task.getSite().getHomeUrl(), Info.ADIDAS_UK_SITEKEY);
		}

		paymentButtonFunctionality = true;

	}

	@Override
	public int checkOut() throws InterruptedException{

		if(handleRequests() == ResponseCode.REQUEST_RESTART){
			return ResponseCode.RESTART;
		}

		if(!setupSite){
			//Set up cookies so we dont get annoying pop ups
			task.setStatus("Setting up site.");

			try{

				get(task.getSite().getHomeUrl());
		
				if(handleRequests() == ResponseCode.REQUEST_RESTART){
					return ResponseCode.RESTART;
				}
				manage().addCookie(new Cookie("UserSignUpAndSave", "4"));
				manage().addCookie(new Cookie("geoRedirectionAlreadySuggested", "true"));
				if(Info.ADIDAS_VERBOSE)System.out.println("set cookies");
				setupSite=true;
			}catch(TimeoutException e){
				task.setStatus("Failed to reach website homepage. Proxy issue?");
				return ResponseCode.WAIT_FOR_USER;
			}
		}

		//go to page
		task.setStatus("Going to product page.");
		try{
			get(task.getUrl());
		}catch(TimeoutException e){
			task.setStatus("Failed to reach product page. Trying again.");
			return ResponseCode.RESTART;
		}

		if(handleRequests() == ResponseCode.REQUEST_RESTART){
			return ResponseCode.RESTART;
		}
		Thread.sleep(500);
		if(handleRequests() == ResponseCode.REQUEST_RESTART){
			return ResponseCode.RESTART;
		}

		boolean onSplash = false;

		//Check to see if product is up for purchase
		while(true){
			if(isElementPresent(addCartPath) && findElement(addCartPath).isDisplayed()){
				task.setStatus("Product up for purchase!");
				if(Info.ADIDAS_VERBOSE)System.out.println("Product is up for purchase!");
				break;
			}else{
				//if were on coming soon page
				if(isElementPresent(reviewButtonPath) && findElement(reviewButtonPath).isDisplayed()){
					task.setStatus("On coming soon - Waiting for release.");
					if(Info.ADIDAS_VERBOSE)System.out.println("On coming soon");
					if(handleRequests() == ResponseCode.REQUEST_RESTART){
						return ResponseCode.RESTART;
					}
					Thread.sleep(5000);
					return ResponseCode.RESTART;
				}
				//if were on splash
				else{
					task.setStatus("On splash page.");
					if(Info.ADIDAS_VERBOSE)System.out.println("On splash page.");
					onSplash = true;
					break;
				}	
			}
		}

		if(handleRequests() == ResponseCode.REQUEST_RESTART){
			return ResponseCode.RESTART;
		}

		//handle splash page
		while(onSplash){
			if(isElementPresent(addCartPath) && findElement(addCartPath).isDisplayed()){
				task.setStatus("Product up for purchase!");
				if(Info.ADIDAS_VERBOSE)System.out.println("Product is up for purchase!");
				break;
			}
			//if were still on splash
			else{
				if(handleRequests() == ResponseCode.REQUEST_RESTART){
					return ResponseCode.RESTART;
				}
				Thread.sleep(100);
			}
		}

		if(handleRequests() == ResponseCode.REQUEST_RESTART){
			return ResponseCode.RESTART;
		}
		Thread.sleep(1000);

		///ON PAGE NOW
		int failures = 0;
		ArrayList<String> pickedSizes = new ArrayList<>();
		String ogPrefSize = task.getSize();

		//Retry adding to cart
		while(true){

			if(failures == Info.MAX_FAILURES){
				if(Info.ADIDAS_VERBOSE)System.out.println("Add to bag button is probably broken, waiting for user...");
				task.setStatus("Stuck on product page");
				task.setSize(ogPrefSize);

				return ResponseCode.WAIT_FOR_USER;
			}

			if(handleRequests() == ResponseCode.REQUEST_RESTART){
				return ResponseCode.RESTART;
			}

			//Chose size
			task.setStatus("Choosing size.");


			WebElement sizeShieldButton;
			try{
				sizeShieldButton = smallWait.until(ExpectedConditions.elementToBeClickable(sizesShieldPath));
			}catch(TimeoutException e){
				failures++;
				continue;
			}

			Thread.sleep(300);

			List<WebElement> sizesContainer = findElements(sizesContainerPath);

			ArrayList<String> possibleSizes = new ArrayList<>();

			//try to find sizes (assumes select shield is already open)
			//if not click the size shield and try again
			for(int j = 0; j < 2; j++){
				for(WebElement e : sizesContainer){
					String size = e.getText().trim();
					try{
						Float.parseFloat(size);
						possibleSizes.add(size);
					}catch(Exception notsize){
						//wasnt a size
					}
				}
				
				if(possibleSizes.size() == 0){
					sizeShieldButton.click();
					continue;
				}else{
					break;
				}
			}
			
			

			String sizeToPick = pickSize(ogPrefSize, possibleSizes, pickedSizes);

			if(sizeToPick == null || sizeToPick.equals("")){
				task.setStatus("ON PAGE : Couldn't find any requested sizes");
				return ResponseCode.WAIT_FOR_USER;
			}

			//Pick the size
			pickedSizes.add(sizeToPick);
			task.setSize(sizeToPick);

			for(WebElement e : sizesContainer){
				String size = e.getText().trim();
				if(size != null && size.equals(sizeToPick)){
					e.click();
					break;
				}
			}

			//make sure size shield closed

			if(handleRequests() == ResponseCode.REQUEST_RESTART){
				return ResponseCode.RESTART;
			}

			//do captcha if it appears			
			boolean isCaptchaThere = isElementPresent(captchaPath);
			if(isCaptchaThere){
				task.setStatus("Solving captcha");

				WebElement captchaIFrame = findElement(captchaPath);
				String srcCaptcha = captchaIFrame.getAttribute("src");
				String captchaKey = findSiteKey(srcCaptcha);

				//If sitekey was switched
				if(!captchaKey.equals(cB.getCaptchaKey())){
					cB.shutOff();
					System.out.println("changed sitekey to: " + captchaKey);
					cB = new CaptchaBanker(getCurrentUrl(), captchaKey);
				}

				if(handleRequests() == ResponseCode.REQUEST_RESTART){
					return ResponseCode.RESTART;
				}
				String reskey = cB.waitForResponse();
				if(handleRequests() == ResponseCode.REQUEST_RESTART){
					return ResponseCode.RESTART;
				}
				setValue(findElement(By.id("g-recaptcha-response")), reskey);
			}else{
				if(Info.ADIDAS_VERBOSE)System.out.println("no captcha to solve");
			}

			if(handleRequests() == ResponseCode.REQUEST_RESTART){
				return ResponseCode.RESTART;
			}

			//click atc
			WebElement addToBagButton;
			try{
				addToBagButton = smallWait.until(ExpectedConditions.elementToBeClickable(addCartPath));
			}catch(TimeoutException e){
				failures++;
				continue;
			}
			//Click add to bag
			task.setStatus("Clicking add to bag.");
			jsClick(addToBagButton);

			if(handleRequests() == ResponseCode.REQUEST_RESTART){
				return ResponseCode.RESTART;
			}

			//check for successful cart addition
			task.setStatus("Waiting to see if copped...");
			WebElement checkoutButton;

			try{
				checkoutButton = mediumWait.until(ExpectedConditions.elementToBeClickable(checkoutButtonPath));

			} 
			//If pressing add to bag didnt bring up checkout button
			catch(TimeoutException e){
				if(Info.ADIDAS_VERBOSE)System.out.println("Failed to add to cart");
				failures++;
				continue;
			}

			if(handleRequests() == ResponseCode.REQUEST_RESTART){
				return ResponseCode.RESTART;
			}
			
			task.setStatus("IN CART : Checking out");
			task.setHasCart(true);
			checkoutButton.click();
			break;

		}

		//timeout exception would happen around here if clicking checkout lead to a null page

		if(handleRequests() == ResponseCode.REQUEST_RESTART){
			return ResponseCode.RESTART;
		}

		WebElement paypalButton;

		try{
			task.setStatus("IN CART : Trying to checkout with paypal");

			paypalButton = mediumWait.until(ExpectedConditions.elementToBeClickable(paypalButtonPath));
			paypalButton.click();

		}catch(TimeoutException e){
			task.setStatus("IN CART : Couldn't find PayPal button");
			return ResponseCode.WAIT_FOR_USER;
		}

		Thread.sleep(2000);
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
				Thread.sleep(1000);
			}
		}

		//TODO: IF no accounts were entered
		Account pp = task.nextPaypal();


		if(handleRequests() == ResponseCode.REQUEST_RESTART){
			return ResponseCode.RESTART;
		}
		waitForElementLoad(By.id("email"));
		Thread.sleep(1000);
		findElement(By.id("email")).clear();
		findElement(By.id("email")).sendKeys(pp.getUser());
		findElement(By.id("password")).clear();
		findElement(By.id("password")).sendKeys(pp.getPass());
		findElement(By.id("btnLogin")).click();
		if(handleRequests() == ResponseCode.REQUEST_RESTART){
			return ResponseCode.RESTART;
		}

		task.setStatus("ATN! CONFIRM ORDER");

		if(task.getRemainingPairs() > 0){

			if(Info.ORDER_SHOES){
				System.out.println("Adidas ordering not yet implemented...");
			}else{
				System.out.println("Sandbox Adidas Order");
			}

			task.decrementRemainingPairs();

		}else{
			if(Info.ADIDAS_VERBOSE) System.out.println("no more remaining pairs");
		}

		
		cB.shutOff();


		return ResponseCode.SUCCESS;
	}


	//----------
	//ADRESSPAGE
	//----------
	//Shipping paths
	private final By shipFnPath = By.id("dwfrm_delivery_singleshipping_shippingAddress_addressFields_firstName");
	private final By shipLnPath = By.id("dwfrm_delivery_singleshipping_shippingAddress_addressFields_lastName");
	private final By shipStrAdrPath = By.id("dwfrm_delivery_singleshipping_shippingAddress_addressFields_address1");
	private final By shipCityPath = By.id("dwfrm_delivery_singleshipping_shippingAddress_addressFields_city");
	private final By shipStateSelectPath = By.id("dwfrm_delivery_singleshipping_shippingAddress_addressFields_countyProvince");
	private final By shipZipPath = By.id("dwfrm_delivery_singleshipping_shippingAddress_addressFields_zip");
	private final By shipPhonePath = By.id("dwfrm_delivery_singleshipping_shippingAddress_addressFields_phone");

	//Billing checkbox
	private final By sameBillingAsShipCheckboxPath = By.id("dwfrm_delivery_singleshipping_shippingAddress_useAsBillingAddress");

	//Billing paths
	private final By billFnPath = By.id("dwfrm_delivery_billing_billingAddress_addressFields_firstName");
	private final By billLnPath = By.id("dwfrm_delivery_billing_billingAddress_addressFields_lastName");
	private final By billStrAdrPath = By.id("dwfrm_delivery_billing_billingAddress_addressFields_address1");
	private final By billCityPath = By.id("dwfrm_delivery_billing_billingAddress_addressFields_city");
	private final By billStateSelectPath = By.id("dwfrm_delivery_billing_billingAddress_addressFields_countyProvince");
	private final By billZipPath = By.id("dwfrm_delivery_billing_billingAddress_addressFields_zip");
	private final By billPhonePath = By.id("dwfrm_delivery_billing_billingAddress_addressFields_phone");

	//Email path
	private final By emailPath = By.id("dwfrm_delivery_singleshipping_shippingAddress_email_emailAddress");

	//Submit path
	private final By reviewAndPayPath = By.id("dwfrm_delivery_savedelivery");

	//----------------
	//CREDIT CARD PAGE
	//----------------
	private final By nameOnCardPath = By.id("dwfrm_payment_creditCard_owner");
	private final By ccNumberPath = By.id("dwfrm_payment_creditCard_number");
	private final By ccExpMonthSelectPath = By.id("dwfrm_payment_creditCard_month");
	private final By ccExpYearSelectPath = By.id("dwfrm_payment_creditCard_year");
	private final By securityCodePath = By.id("dwfrm_payment_creditCard_cvn");

	//Shipping URL
	private final String addressUrlKeyWord = "delivery-start";
	private final String creditcardUrlKeyWord = "COSummary";

	@Override
	public void pay(AdidasGuestCheckout profile) {
		System.out.println(profile.toString());

		String url = getCurrentUrl();
		boolean forceCreditCardFillin = false;


		//On shipping page
		if(url.contains(addressUrlKeyWord)){

			task.setStatus("Attempting to fill in shipping and billing addresses and email");

			//wait for first form element to show up, after, the rest should be loaded also
			WebElement shipFn = null;
			try{
				shipFn = mediumWait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(shipFnPath)).get(0);
			}catch(TimeoutException e){
				task.setStatus("Couldn't locate elements, try reloading the page then trying again.");
				return;
			}

			//Try opening separate billing address values (shouldnt matter because values are being set via JS)
			try{
				makeVisible(findElement(sameBillingAsShipCheckboxPath));
				smallWait.until(ExpectedConditions.elementToBeClickable(sameBillingAsShipCheckboxPath)).click();
			}catch(TimeoutException e){}

			//Shipping first name
			setValue(shipFn, profile.getShippingInfo().getFirstName());

			//Shipping last name
			WebElement shipLn = findElement(shipLnPath);
			setValue(shipLn, profile.getShippingInfo().getLastName());

			//Shipping str adr
			WebElement shipStrAdr = findElement(shipStrAdrPath);
			setValue(shipStrAdr, profile.getShippingInfo().getStreetAdr());

			//shipping city
			WebElement shipCity = findElement(shipCityPath);
			setValue(shipCity, profile.getShippingInfo().getCity());

			//shipping state
			WebElement shipState = findElement(shipStateSelectPath);
			String stateAbr = USAStateConverter.convertToStateAbbreviation(profile.getShippingInfo().getState());
			setValue(shipState, stateAbr);

			//shipping Zip
			WebElement shipZip = findElement(shipZipPath);
			setValue(shipZip, profile.getShippingInfo().getZip());

			//shipping phone number
			WebElement shipPhone = findElement(shipPhonePath);
			setValue(shipPhone, profile.getShippingInfo().getPhone());

			//Billing first name
			WebElement billFn = findElement(billFnPath);
			setValue(billFn, profile.getPaymentInfo().getFirstName());

			//Billing last name
			WebElement billLn = findElement(billLnPath);
			setValue(billLn, profile.getPaymentInfo().getLastName());

			//Billing str adr
			WebElement billStrAdr = findElement(billStrAdrPath);
			setValue(billStrAdr, profile.getPaymentInfo().getStreetAdr());

			//Billing city
			WebElement billCity = findElement(billCityPath);
			setValue(billCity, profile.getPaymentInfo().getCity());

			//Billing state
			WebElement billState = findElement(billStateSelectPath);
			String billStateAbr = USAStateConverter.convertToStateAbbreviation(profile.getPaymentInfo().getState());
			setValue(billState, billStateAbr);

			//Billing Zip
			WebElement billZip = findElement(billZipPath);
			setValue(billZip, profile.getPaymentInfo().getZip());

			//Billing phone number
			WebElement billPhone = findElement(billPhonePath);
			setValue(billPhone, profile.getPaymentInfo().getPhone());

			//EMAIL
			WebElement email = findElement(emailPath);
			setValue(email, profile.getEmail());

			//Submit info
			findElement(reviewAndPayPath).click();

			forceCreditCardFillin = true;


		}

		//wait for page to finish loading then wait for first form element to show up on the new page
		//Or were already on that new page when pay was called (two birds with one if statement)

		//On shipping page
		if(url.contains(creditcardUrlKeyWord) || forceCreditCardFillin){

			task.setStatus("Attempting to fill in credit card info.");

			//TODO: SELECT CREDITCARD RADIOBUTTON IF ITS NOT ALREADY SELECTED
			
			//wait for first form element to show up, after, the rest should be loaded also

			//Name on Card
			WebElement nameOnCard = null;
			try{
				nameOnCard = mediumWait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(nameOnCardPath)).get(0);
			}catch(TimeoutException e){
				task.setStatus("Couldn't locate elements, try reloading the page then trying again.");
				return;
			}
			setValue(nameOnCard, profile.getPaymentInfo().getNameOnCard());

			//Card number
			WebElement cardNum = findElement(ccNumberPath);
			setValue(cardNum, profile.getPaymentInfo().getCardNum());

			//Card expiration month
			WebElement cardExpMonth = findElement(ccExpMonthSelectPath);
			setValue(cardExpMonth, profile.getPaymentInfo().getExpMonth());

			//Card expiration year
			WebElement cardExpYear = findElement(ccExpYearSelectPath);
			setValue(cardExpYear, profile.getPaymentInfo().getExpYear());

			//Security code
			WebElement secCode = findElement(securityCodePath);
			setValue(secCode, profile.getPaymentInfo().getSecurityCode());

		}

		task.setStatus("Done filling.");

	}


	private String findSiteKey(String src){
		String siteKey = "";

		int i = 0;
		int startIndex = 0;
		int endIndex = 0;
		while(true){
			if(String.valueOf(src.charAt(i)).equals("=")){
				startIndex = i+1;
			}else if(String.valueOf(src.charAt(i)).equals("&")){
				endIndex = i;
				break;
			}
			i++;

		}
		siteKey = src.substring(startIndex, endIndex);
		return siteKey;

	}

	@Override
	public void onClose(){
		if(cB != null){
			cB.shutOff();
			if(Info.ADIDAS_VERBOSE)System.out.println("shutting off captcha banker");
		}
	}
}
