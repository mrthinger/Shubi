package mrthinger.shubi.sitedriver;


import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import mrthinger.shubi.Info;
import mrthinger.shubi.USAStateConverter;
import mrthinger.shubi.task.Task;
import mrthinger.shubi.type.Account;
import mrthinger.shubi.type.ShippingAccount;
//Script was rushed, needs to be redone
public class NikeLaunchSiteDriver extends SiteDriver{


	public NikeLaunchSiteDriver(DesiredCapabilities dc, Task task){
		super(dc, task);
		manage().timeouts().pageLoadTimeout(300, TimeUnit.SECONDS);

		manage().window().setSize(new Dimension(x/4,y));

	}

	@Override
	public int checkOut() throws InterruptedException{

		WebDriverWait smallWait = new WebDriverWait(this, 5);
		WebDriverWait mediumWait = new WebDriverWait(this, 15);

		//TODO: ADD WAITS
		if(handleRequests() == ResponseCode.REQUEST_RESTART){
			return ResponseCode.RESTART;
		}

		//go to page
		task.setStatus("Going to product page.");
		get(task.getUrl());

		if(handleRequests() == ResponseCode.REQUEST_RESTART){
			return ResponseCode.RESTART;
		}

		task.setStatus("Waiting to see if popup shows...");
		Thread.sleep(5000);

		if(handleRequests() == ResponseCode.REQUEST_RESTART){
			return ResponseCode.RESTART;
		}

		if(isElementPresent(By.xpath("//button")) && findElement(By.xpath("//button")).isDisplayed()){
			findElement(By.xpath("//button")).click();
		}

		//Select size
		task.setStatus("Selecting size.");
		WebElement dropDownListBox = waitForElementLoad(By.xpath("//select"));

		Select clickThis = new Select(dropDownListBox);

		//print available sizes
		List<WebElement> sizes = clickThis.getOptions();

		if(Info.NIKE_VERBOSE){
			for(WebElement s : sizes){

				System.out.println(s.getText());

			}
		}
		clickThis.selectByVisibleText(task.getSize());

		if(handleRequests() == ResponseCode.REQUEST_RESTART){
			return ResponseCode.RESTART;
		}

		//checkout
		task.setStatus("Checking out.");
		//crashed here on OVO release twice
		mediumWait.until(ExpectedConditions.elementToBeClickable(By.xpath("//aside/div[2]/div/div/a"))).click();

		if(handleRequests() == ResponseCode.REQUEST_RESTART){
			return ResponseCode.RESTART;
		}

		//Need to sign in


		if(handleRequests() == ResponseCode.REQUEST_RESTART){
			return ResponseCode.RESTART;
		}
		Thread.sleep(2000);
		task.setStatus("Signing in.");
		mediumWait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()[contains(.,'Sign')]]"))).click();
		//findElement(By.linkText("Sign In")).click();
		Thread.sleep(2000);

		if(handleRequests() == ResponseCode.REQUEST_RESTART){
			return ResponseCode.RESTART;
		}

		mediumWait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[2]/input")));
		findElement(By.xpath("//div[2]/input")).clear();
		findElement(By.xpath("//div[2]/input")).sendKeys(task.getUserpass().getUser());

		if(handleRequests() == ResponseCode.REQUEST_RESTART){
			return ResponseCode.RESTART;
		}

		findElement(By.xpath("//div[3]/input")).clear();
		findElement(By.xpath("//div[3]/input")).sendKeys(task.getUserpass().getPass());

		if(handleRequests() == ResponseCode.REQUEST_RESTART){
			return ResponseCode.RESTART;
		}

		findElement(By.xpath("//div[6]/input")).click();

		if(handleRequests() == ResponseCode.REQUEST_RESTART){
			return ResponseCode.RESTART;
		}

		Thread.sleep(5000);

		if(handleRequests() == ResponseCode.REQUEST_RESTART){
			return ResponseCode.RESTART;
		}
		
		//Enter shipping
		By fNPath = By.id("first-name-shipping");
		if(isElementPresent(fNPath) && findElement(fNPath).isDisplayed()){
			task.setStatus("Entering in shipping");
			ShippingAccount sa = task.nextShipping();
			findElement(By.id("first-name-shipping")).clear();
			findElement(By.id("first-name-shipping")).sendKeys(sa.getFirstName());
			findElement(By.id("last-name-shipping")).clear();
			findElement(By.id("last-name-shipping")).sendKeys(sa.getLastName());
			findElement(By.id("shipping-address-1")).clear();
			findElement(By.id("shipping-address-1")).sendKeys(sa.getStreetAdr());
			findElement(By.id("city")).clear();
			findElement(By.id("city")).sendKeys(sa.getCity());
			findElement(By.id("state")).clear();
			findElement(By.id("state")).sendKeys(USAStateConverter.convertToStateAbbreviation(sa.getState()));
			findElement(By.id("zipcode")).clear();
			findElement(By.id("zipcode")).sendKeys(sa.getZip());
			findElement(By.id("phone-number")).clear();
			findElement(By.id("phone-number")).sendKeys(sa.getPhone());
			mediumWait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//a[contains(text(),'Save & Continue')])[3]"))).click();
			mediumWait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//a[contains(text(),'Save & Continue')])[2]"))).click();
		}
		
		Thread.sleep(2000);
		
		//Add paypal (this is unreliable)
		By addPPPath = By.id("addPayPal");
		if(isElementPresent(addPPPath) && findElement(addPPPath).isDisplayed()){
			task.setStatus("Adding paypal");
			Thread.sleep(5000);
			mediumWait.until(ExpectedConditions.elementToBeClickable(addPPPath)).click();
			
			Thread.sleep(5000);

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
			
			
			By continuePath = By.xpath("//button[contains(text(),'Continue')]");
			
			while(true){
				try{
					mediumWait.until(ExpectedConditions.elementToBeClickable(continuePath)).click();
					break;
					
				}catch(Exception e){
					Thread.sleep(1000);
				}
			}
			
			
			By agreePath = By.xpath("//*[@id='button']/input");
			
			while(true){
				try{
					mediumWait.until(ExpectedConditions.elementToBeClickable(agreePath)).click();
					break;
					
				}catch(Exception e){
					Thread.sleep(1000);
				}
			}
		
			

			if(ppPoppedUp){
				switchTo().window(ogWindowHandle);
			}
			
			
		}
		
		Thread.sleep(5000);
		
		task.setStatus("Submiting order!");

		if(Info.ORDER_SHOES){
			safeClick(By.linkText("Submit Order"));
			System.out.println("Purchasing...");
		}else{
			System.out.println("Sandbox Nike Order");
		}

		task.decrementRemainingPairs();



		return ResponseCode.SUCCESS;
	}
}
