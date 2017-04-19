package mrthinger.shubi.sitedriver;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;

import mrthinger.shubi.Info;
import mrthinger.shubi.SiteKey;
import mrthinger.shubi.captcha.CaptchaBanker;
import mrthinger.shubi.gui.WindowMainController;
import mrthinger.shubi.task.Task;
import mrthinger.shubi.type.Account;
import mrthinger.shubi.type.SizeList;

public class AdidasBackdoorDriver extends SiteDriver {

	private CaptchaBanker cB;
	
	public AdidasBackdoorDriver(DesiredCapabilities dc, Task task) {
		super(dc, task);
		

	}
	
	@Override
	public int checkOut() throws InterruptedException{
		
		long captchaTime = 1484211540000L;
		long releaseTime = 1484211600000L;
		
		//long releaseTime = System.currentTimeMillis() + (60L * 1000L);
		//long captchaTime = releaseTime - (45L * 1000L);
		
		task.setStatus("Waiting for release");
		
		while(true){
			long currentTime = System.currentTimeMillis();
			
			//If its after or exactly 3:59am EST 1/12/17
			if(currentTime >= captchaTime){
				task.setStatus("Starting captcha gen");
				
				//Start captcha gen
				if(task.getSite().equals(SiteKey.ADIDAS_UK_BACKDOOR)){
					cB = new CaptchaBanker(SiteKey.ADIDAS_UK_BACKDOOR, Info.ADIDAS_UK_SITEKEY);
				}else if(task.getSite().equals(SiteKey.ADIDAS_US_BACKDOOR)){
					cB = new CaptchaBanker(SiteKey.ADIDAS_US_BACKDOOR, Info.ADIDAS_US_SITEKEY);
				}else{
					System.out.println("Invalid task passed to AdidasBackdoorDriver");
				}
				
				break;
			}
			
			Thread.sleep(20);
		}
		
		task.setStatus("Waiting for release - captcha gen started");
		
		while(true){
			long currentTime = System.currentTimeMillis();
			
			if(currentTime > releaseTime){
				//If shoe has been released
				task.setStatus("Released attempting to purchase");
				

				String link = getBackdoorUrl().concat(cB.waitForResponse());
				System.out.println(link);
				get(link);
				
				task.setStatus("Tried backdoor, did it work?");
				
				
				Thread.sleep(5000);
				
				By ppCheckoutButtonPath = By.xpath("//*[@id='minicart_overlay']/div[3]/a[3]/button");
				findElement(ppCheckoutButtonPath).click();
				
				task.setStatus("AY LMAO: " + task.getSize());
				task.decrementRemainingPairs();
				
				
				Thread.sleep(2000);
				
				while(true){
					try{
						switchTo().frame("injectedUl");
						break;
					}catch(Exception e){
						Thread.sleep(1000);
					}
				}

				Account pp = task.nextPaypal();
				
				
				waitForElementLoad(By.id("email"));
				Thread.sleep(2000);
				findElement(By.id("email")).clear();
				findElement(By.id("email")).sendKeys(pp.getUser());
				findElement(By.id("password")).clear();
				findElement(By.id("password")).sendKeys(pp.getPass());
				findElement(By.id("btnLogin")).click();
				
				task.setStatus("HELP : FINISH CHECKING OUT");
				
				
				break;
			}
			
			Thread.sleep(20);
			
		}
		
		cB.shutOff();
		return ResponseCode.SUCCESS;
		
	}

	private String getBackdoorUrl(){
		String url = "";
		if(task.getSite().equals(SiteKey.ADIDAS_US_BACKDOOR)){
			//us url
			url = "http://www.adidas.com/on/demandware.store/Sites-adidas-US-Site/en_US/Cart-MiniAddProduct?"
					+ "masterPid=" + task.getShoe()
					+ "&pid=" + task.getShoe() + "_" + SizeList.toAdidasSizeCode(task.getSize())
					+ "&ajax=true"
					+ "&layer=Add+To+Bag+overlay"
					+ "&Quantity=1"
					+ "&sessionSelectedStoreID=null"
					+ "&g-recaptcha-response=";
		}else if(task.getSite().equals(SiteKey.ADIDAS_UK_BACKDOOR)){
			//uk url
			url = "http://www.adidas.co.uk/on/demandware.store/Sites-adidas-GB-Site/en_GB/Cart-MiniAddProduct?"
					+ "masterPid=" + task.getShoe()
					+ "&pid=" + task.getShoe() + "_" + SizeList.toAdidasSizeCode(task.getSize())
					+ "&ajax=true"
					+ "&layer=Add+To+Bag+overlay"
					+ "&Quantity=1"
					+ "&sessionSelectedStoreID=null"
					+ "&g-recaptcha-response=";
		}
		
		return url;
	}
	
	@Override
	public void onClose(){
		if(cB != null){
			cB.shutOff();
		}
	}

	/*
	private boolean isOrderable(){
		//client id check stuff + json parse for orderable -- orderable doesnt work, check for c_previewTo for release time in UTC
		//gen link to product
		String stockLink = "http://production-us-adidasgroup.demandware.net/s/adidas-US/dw/shop/v15_6/products/("
				+ "C77124_640"
				+ ")?"
				+ "client_id=bb1e6193-3c86-481e-a440-0d818af5f3c8&expand=availability";
	}
	*/
}
