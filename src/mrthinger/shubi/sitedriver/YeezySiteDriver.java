package mrthinger.shubi.sitedriver;

import java.util.concurrent.TimeUnit;

import javax.naming.directory.SearchControls;

import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import mrthinger.shubi.ShubiMain;
import mrthinger.shubi.USAStateConverter;
import mrthinger.shubi.gui.WindowMainController;
import mrthinger.shubi.task.Task;
import mrthinger.shubi.type.AdidasGuestCheckout;
import mrthinger.shubi.type.Backdoor;
import net.sourceforge.htmlunit.corejs.javascript.PolicySecurityController;

public class YeezySiteDriver extends SiteDriver{

	//what text is being displayed on the page
	private final By bodyPath = By.xpath("/html/body");

	//Setup waits
	private WebDriverWait smallWait;
	private WebDriverWait mediumWait;

	public YeezySiteDriver(DesiredCapabilities dc, Task task) {
		super(dc, task);

		paymentButtonFunctionality = true;
		
		manage().timeouts().pageLoadTimeout(300, TimeUnit.SECONDS);

		smallWait = new WebDriverWait(this, 5);
		mediumWait = new WebDriverWait(this, 10);
	}

	@Override
	public int checkOut() throws InterruptedException{

		if(handleRequests() == ResponseCode.REQUEST_RESTART){
			return ResponseCode.RESTART;
		}
		
		task.setNeedsAtn(false);

		//go to yeezy page ay lmao
		task.setStatus("Going to Yeezy page.");

		try{
			get(task.getUrl());
		}catch(TimeoutException e){
			task.setStatus("Failed to connect to website! Proxy Error?");
			return ResponseCode.WAIT_FOR_USER;
		}

		//Whats there? If coming soon or if waiting.
		int secsNotOnProductPage = 0;
		while(true){

			if(handleRequests() == ResponseCode.REQUEST_RESTART){
				return ResponseCode.RESTART;
			}

			WebElement body = null;
			String pageText = "";
			try{
				body = mediumWait.until(ExpectedConditions.visibilityOfElementLocated(bodyPath));
				
			}catch(TimeoutException e){
				continue;
			}
			
			try{
				pageText = body.getText().toLowerCase();
			}catch(Exception e){
				
			}

			
			//TODO: MAKE ACCOUNT FOR PROXY ERRORS
			//on coming soon
			if(pageText.contains("coming soon")){
				task.setStatus("On coming soon page. " + secsNotOnProductPage);
				
				//reload the page every 30 seconds
				if(secsNotOnProductPage > 2 && secsNotOnProductPage % 30 == 0){
					task.setStatus("Refreshing Yeezy page.");

					try{
						get(task.getUrl());
					}catch(TimeoutException e){
						task.setStatus("Failed to connect to website! Proxy Error?");
						return ResponseCode.WAIT_FOR_USER;
					}
				}
			}

			//on splash
			//you are waiting || please do not refresh this page
			else if((pageText.contains("you") && pageText.contains("are") && pageText.contains("waiting"))
					|| (pageText.contains("please") && pageText.contains("refresh") && pageText.contains("page"))){
				task.setStatus("On splash. " + secsNotOnProductPage);
			}
			
			//on coming soon
			else if(pageText.contains("sold out")){
				task.setStatus("Sold out LMAO " + secsNotOnProductPage);
				
				//reload the page every 2 mins
				if(secsNotOnProductPage > 2 && secsNotOnProductPage % 120 == 0){
					task.setStatus("Refreshing Yeezy page.");

					try{
						get(task.getUrl());
					}catch(TimeoutException e){
						task.setStatus("Failed to connect to website! Proxy Error?");
						return ResponseCode.WAIT_FOR_USER;
					}
				}
			}

			//otherwise were probably on the product page :)
			else{
				break;
			}

			Thread.sleep(500);

			if(handleRequests() == ResponseCode.REQUEST_RESTART){
				return ResponseCode.RESTART;
			}

			Thread.sleep(500);

			secsNotOnProductPage++;

		}

		//On product Page
		
		//Product page loop
		int secsOnProductPage = 0;
		while(true){
			
			if(handleRequests() == ResponseCode.REQUEST_RESTART){
				return ResponseCode.RESTART;
			}
			
			long mins = TimeUnit.SECONDS.toMinutes(secsOnProductPage);
			long secs = TimeUnit.SECONDS.toSeconds(secsOnProductPage) - TimeUnit.MINUTES.toSeconds(mins);
			
			task.setNeedsAtn(true);
			task.setStatus("PRODUCT PAGE " + mins + ":" + secs);
			
			Thread.sleep(500);

			if(handleRequests() == ResponseCode.REQUEST_RESTART){
				return ResponseCode.RESTART;
			}

			Thread.sleep(500);
			
			secsOnProductPage++;
			
			//wait x(1 hour in this case) mins on product page before restarting
			//Current theory is 10-15 mins on product page max and 10 mins to check out 
			//(should be a max of 20-25 mins but since testing Ill go an hour just in case)
			if(secsOnProductPage >= (60*60)){
				return ResponseCode.RESTART;
			}
			
		}

		//return ResponseCode.SUCCESS;

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
}
