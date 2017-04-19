package mrthinger.shubi.sitedriver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import com.google.common.base.Function;

import mrthinger.shubi.Info;
import mrthinger.shubi.KeyGenerator;
import mrthinger.shubi.gui.WindowMainController;
import mrthinger.shubi.task.Task;
import mrthinger.shubi.type.AdidasGuestCheckout;
import mrthinger.shubi.type.Backdoor;
import mrthinger.shubi.type.Proxy;

public class SiteDriver extends ChromeDriver{

	protected JavascriptExecutor jse;
	protected int x;
	protected int y;
	protected Task task;
	protected boolean restartRequest;
	protected boolean toFrontRequest;
	protected boolean closeRequest;
	protected boolean hideRequest;
	protected boolean payRequest;
	protected boolean pauseRequest;
	protected boolean hidden;
	protected volatile boolean running;
	protected volatile boolean paused;

	private volatile boolean pauseManaged;
	//yeezy only request
	protected boolean injectRequest;

	protected Point hiddenPoint;

	protected boolean paymentButtonFunctionality;

	public SiteDriver(DesiredCapabilities dc, Task task){
		super(dc);
		this.task = task;
		this.task.setDriver(this);
		this.jse = (JavascriptExecutor)this;
		this.restartRequest = false;
		this.toFrontRequest = false;
		this.closeRequest = false;
		this.hideRequest = false;
		this.hidden = true;
		this.running = true;
		this.paused = false;
		this.pauseManaged = false;

		this.payRequest=false;
		this.injectRequest=false;

		this.paymentButtonFunctionality = false;

		x = GetScreenWorkingWidth();
		y = GetScreenWorkingHeight();

		hiddenPoint = new Point((4*x/5), 0);

		manage().window().setSize(new Dimension(x,y));
		manage().window().setPosition(hiddenPoint);
	}

	public boolean hasPaymentButtonFunctionality(){
		return paymentButtonFunctionality;
	}

	public int handleRequests(){
		int requestNumber = ResponseCode.REQUEST_NOTHING;
		
		if(injectRequest){
			//inject stuff

			boolean iMadeItPopup = WindowMainController.getInjectPopup().prompt(task);
			String bd = null;
			if(iMadeItPopup){
				while(true){
					bd = WindowMainController.getInjectPopup().getInjectableJS();
					//Wait for bdUrl to be set
					if(bd != null){
						//if no profile was selected itll return with as n
						if(!bd.equals("n")){
							//if a valid url was returned requestNumber will equal requestInject
							requestNumber = ResponseCode.REQUEST_INJECT;
						}
						break;
					}
					
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}


				//If requestnumber was set it means we received a valid bdUrl
				if(requestNumber == ResponseCode.REQUEST_INJECT){
					try{
						jse.executeScript(bd);
					}
					catch(Exception e){
						System.out.println("JS Injection Failed!");
					}
				}
			}
			injectRequest = false;
		}
		
		if(payRequest){
			//pay stuff
			boolean iMadeItPopup = WindowMainController.getPayPopup().prompt();
			AdidasGuestCheckout profile = null;
			if(iMadeItPopup){
				while(true){
					profile = WindowMainController.getPayPopup().getPickedAdidasGuestCheckout();

					//Wait for profile to be set
					if(profile != null){
						//if no profile was selected itll return with email null
						if(profile.getEmail() != null){
							//if a profile was selected requestNumber will equal requestPay
							requestNumber = ResponseCode.REQUEST_PAY;
						}
						break;
					}

					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}


				//If requestnumber was set it means we received a valid adidascheckoutProfile
				if(requestNumber == ResponseCode.REQUEST_PAY){
					//add try catch
					pay(profile);
				}
			}
			payRequest = false;
		}

		if(toFrontRequest){
			if(hidden){

				manage().window().setPosition(new Point(0, 0));

				hidden = false;
			}else{

				manage().window().setPosition(hiddenPoint);

				hidden = true;
			}
			requestNumber = ResponseCode.REQUEST_FRONT;
			toFrontRequest = false;
		}

		if(hideRequest){
			if(!hidden){

				manage().window().setPosition(hiddenPoint);

				hidden = true;
			}
			requestNumber = ResponseCode.REQUEST_HIDE;
			hideRequest = false;
		}

		if(closeRequest){
			requestNumber = ResponseCode.REQUEST_CLOSE;
			closeRequest = false;
			exit();
		}

		if(restartRequest){
			requestNumber = ResponseCode.REQUEST_RESTART;
			if(paused) paused = false;
			restartRequest = false;
		}
		
		if(pauseRequest){
			//toggle pausing
			paused = !paused;
			if(paused){
				task.setStatus("Paused.");
				pauseManaged = false;
			}else{
				task.setStatus("Unpaused.");
			}
			
			pauseRequest = false;
		}
		
		//This is the call thats managing the pause loop so the others dont cause a stack overflow
		boolean manager = false;
		while(paused){
			if(pauseManaged && !manager){
				break;
			}
			pauseManaged = true;
			manager = true;
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
			if(handleRequests() == ResponseCode.REQUEST_RESTART){
				return ResponseCode.RESTART;
			}
		}

		return requestNumber;
	}

	protected void pay(AdidasGuestCheckout profile) {
		System.out.println("No auto-payment code has been written for this site.");

	}

	public boolean isRunning(){
		return running;
	}

	public void indefinitelyHandleRequests(){
		while(true){
			handleRequests();
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void requestPause(){
		pauseRequest = true;
	}
	public void requestPay(){
		payRequest = true;
	}
	public void requestInject(){
		injectRequest = true;
	}
	public void requestRestart(){
		restartRequest = true;
	}

	public void requestHide(){
		hideRequest = true;
	}

	public void requestToFront(){
		toFrontRequest = true;
	}

	public void requestClose(){
		closeRequest = true;
	}

	protected void onClose(){
		
	}
	
	protected void exit(){
		onClose();
		if(task.hasProxy()){
			task.getProxy().subtractTaskUsing();
		}
		paused = false;
		running = false;
		quit();
	}

	protected WebElement fluentWaitForElement(final By locator, long timeout, long checkEvery) throws TimeoutException {
		Wait<WebDriver> wait = new FluentWait<WebDriver>(this)
				.withTimeout(timeout, TimeUnit.SECONDS)
				.pollingEvery(checkEvery, TimeUnit.SECONDS)
				.ignoring(NoSuchElementException.class);

		WebElement e = wait.until(new Function<WebDriver, WebElement>() {
			public WebElement apply(WebDriver driver) {
				return driver.findElement(locator);
			}
		});

		return e;
	};

	/**
	 * 
	 * @param proxyInfo
	 * @return DesiredCapabilities with proxy info attached
	 */
	public static DesiredCapabilities genCapabilities(Proxy proxyInfo){

		DesiredCapabilities cap = new DesiredCapabilities();

		Map<String, Object> preferences = new HashMap<String, Object>();
		ChromeOptions options = new ChromeOptions();

		preferences.put("enable_do_not_track", true);
		options.setExperimentalOption("prefs", preferences);

		cap.setCapability(ChromeOptions.CAPABILITY, options);

		if(proxyInfo != null){
			org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();
			String ipPort = proxyInfo.getIp()+":"+proxyInfo.getPort();
			proxy.setHttpProxy(ipPort)
			.setFtpProxy(ipPort)
			.setSslProxy(ipPort);
			cap.setCapability(CapabilityType.PROXY, proxy);
			//TODO: PROXY ADDON
		}
		return cap;
	}

	/**
	 * 
	 * @param e element to make visible
	 */
	public void makeVisible(WebElement e){
		jse.executeScript("arguments[0].setAttribute('style', '')", e);
	}

	public void setValue(WebElement e, String value){
		//document.getElementById('dwfrm_delivery_singleshipping_shippingAddress_addressFields_countyProvince').value='NJ'
		jse.executeScript("arguments[0].value = '" + value + "'", e);
	}

	public void jsClick(WebElement e){
		jse.executeScript("arguments[0].click();", e);
	}
	
	
	
	/**
	 * submits backdoor as a post request
	 * @param bd Backdoor to be tried
	 */
	public void postBackdoor(Backdoor bd){
		jse.executeScript(bd.toJSInject());
	}

	/**
	 * 
	 * @return screen width
	 */
	public static int GetScreenWorkingWidth() {
		return java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width;
	}

	/**
	 * 
	 * @return screen height
	 */
	public static int GetScreenWorkingHeight() {
		return java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height;
	}

	/**
	 * Scrolls element into view
	 * 
	 * @param e element to scroll to view
	 */
	protected void scrollToView(WebElement e){
		jse.executeScript("arguments[0].scrollIntoView()", e);
	}


	/**
	 * @return page's javascript loading state
	 */
	protected String getLoadState(){
		return (String) jse.executeScript("return document.readyState");
	}


	/**
	 * Waits indefinitely for element to be loaded on page then returns it
	 * 
	 * @param by path to element
	 * @return loaded WebElement
	 * @throws InterruptedException
	 */
	protected WebElement waitForElementLoad(By by) throws InterruptedException{
		WebElement element = null;
		boolean elementLoaded = false;
		while(!elementLoaded){
			try{	
				element = findElement(by);
				elementLoaded = true;
			} catch (Exception e){
				elementLoaded = false;
			}

			Thread.sleep(50);

		}

		return element;
	}

	protected boolean isPresentAndDisplayed(By by){
		return isElementPresent(by) && findElement(by).isDisplayed();
	}

	/**
	 * 
	 * @param by path to element
	 * @return if element loaded on page
	 */
	protected boolean isElementPresent(By by){
		try{	
			findElement(by);
			return true;
		} catch (Exception e){
			return false;
		}

	}


	/**
	 * Waits for element to load then clicks it
	 * 
	 * @param by element to be clicked
	 * @throws InterruptedException
	 */
	protected void safeClick(By by) throws InterruptedException{

		boolean pageLoaded = false;
		while(!pageLoaded){
			try{	
				findElement(by).click();
				pageLoaded = true;
			} catch (NoSuchElementException e) {
				pageLoaded = false;
			}catch(ElementNotVisibleException v){
				pageLoaded = false;
			}

			Thread.sleep(50);

		}
	}

	public int checkOut() throws InterruptedException{
		System.out.println("Default checkout task was called. If you're seeing this the dev screwed up.");
		return ResponseCode.WAIT_FOR_USER;
	};

	protected String pickSize(String ogPrefSize, List<String> possibleSizes,
			List<String> pickedSizes) {

		//Convert all sizes to same format
		//TODO: FILTER NON-SIZES
		ogPrefSize = String.valueOf(Float.parseFloat(ogPrefSize));
		for(String s : possibleSizes){
			if(s != null && !s.isEmpty()){
				s = String.valueOf(Float.parseFloat(s));
			}
		}
		for(String s : pickedSizes){
			if(s != null && !s.isEmpty()){
				s = String.valueOf(Float.parseFloat(s));
			}
		}

		//If size doenst matter so much to the user that they set a range of sizes
		String sizeToPick = "";

		if(task.getSizeList().hasRange()){

			boolean preferredSizeInStock = false;
			boolean preferredSizePicked = false;

			for(String size : possibleSizes){
				if(size != null && size.equals(ogPrefSize)){
					preferredSizeInStock = true;
					for(String pickedSize : pickedSizes){
						if(pickedSize.equals(ogPrefSize)){
							preferredSizePicked = true;
						}
					}
				}
			}

			//preferred size hasnt been chosen yet and is in stock
			if(preferredSizeInStock && !preferredSizePicked){
				sizeToPick = ogPrefSize;
			}

			//find closest size
			else{
				//gen hashmap of size and distance from pref size
				HashMap<String, Float> sizeDiff = new HashMap<>();
				for(String size : possibleSizes){
					//if size isnt empty and is in range and is not the pref size
					if(size != null && !size.equals("") 
							&& Float.valueOf(size) >= Float.valueOf(task.getSizeList().getMin())
							&& Float.valueOf(size) <= Float.valueOf(task.getSizeList().getMax())
							&& !size.equals(ogPrefSize)){
						Float diff = Math.abs(Float.valueOf(size) - Float.valueOf(ogPrefSize));
						sizeDiff.put(size, diff);
					}
				}

				//remove picked sizes
				List<Entry<String, Float>> toRemoveSizes = new ArrayList<>();

				for(String pickedSize : pickedSizes){
					for(Entry<String, Float> e : sizeDiff.entrySet()){
						if(e.getKey().equals(pickedSize)){
							toRemoveSizes.add(e);
						}
					}
				}

				sizeDiff.entrySet().removeAll(toRemoveSizes);

				//sort hash by distance from preferred size
				Comparator<Entry<String, Float>> valueComparator = new Comparator<Entry<String,Float>>() {

					@Override
					public int compare(Entry<String, Float> e1, Entry<String, Float> e2) {
						Float v1 = e1.getValue();
						Float v2 = e2.getValue();
						return v1.compareTo(v2);
					}
				};

				List<Entry<String, Float>> sizeDiffEntries = new ArrayList<Entry<String, Float>>(sizeDiff.entrySet());
				Collections.sort(sizeDiffEntries, valueComparator);

				if(Info.SITEDRIVER_VERBOSE){
					for(Entry<String, Float> e : sizeDiffEntries){
						System.out.println(e.getKey() + " + " + e.getValue());
					}
				}
				sizeToPick = sizeDiffEntries.get(0).getKey().toString();
			}



		}

		//If size needs to be specific
		else{
			boolean foundSize = false;

			//try to find initial size
			for(String size : possibleSizes){
				if(size != null && size.equals(ogPrefSize)){
					//click it if it hasnt been clicked already
					boolean alreadyPicked = false;
					for(String pickedSize : pickedSizes){
						if(pickedSize.equals(ogPrefSize)){
							alreadyPicked = true;
						}
					}
					if(!alreadyPicked){
						foundSize = true;
						sizeToPick = size;
					}
					break;
				}
			}

			//try to find the other sizes
			if(!foundSize){

				//reset picked sizes so we can still continue until max failure count
				if(pickedSizes.size() == task.getSizeList().getSizeList().size()){
					pickedSizes = new ArrayList<>();
				}

				ArrayList<String> remainingPicks = new ArrayList<>(task.getSizeList().getSizeList());
				remainingPicks.remove(ogPrefSize);

				for(String wantedSize : remainingPicks){
					if(!foundSize){
						for(String size : possibleSizes){
							if(size != null && size.equals(wantedSize)){
								//click it if it hasnt been clicked already
								boolean alreadyPicked = false;
								for(String pickedSize : pickedSizes){
									if(pickedSize.equals(wantedSize)){
										alreadyPicked = true;
									}
								}
								if(!alreadyPicked){
									foundSize = true;
									sizeToPick = size;
								}
								break;

							}
						}
					}
				}
			}

			//None of the sizes were found
			if(!foundSize){
				//method will return empty string "" and be handled by Site specific Driver
			}

		}

		return sizeToPick;
	}





}
