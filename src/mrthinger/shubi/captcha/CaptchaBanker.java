package mrthinger.shubi.captcha;

import java.io.IOException;

import mrthinger.shubi.Info;
import mrthinger.shubi.SiteKey;

public class CaptchaBanker {

	//will request a new captcha when buffer amount
	//of milliseconds are away from current captcha expiring
	public static final long GENBUFFER = 30000;
	
	private volatile String responseCode;
	private volatile long expireTime;
	
	private volatile String captchaKey;
	private volatile String site;

	private volatile boolean running;
	
	private TwoCaptchaService cap;

	public CaptchaBanker(String site, String captchaKey){
		this.site = site;
		this.captchaKey = captchaKey;
		responseCode = null;
		running = true;
		
		cap = new TwoCaptchaService(captchaKey, site);

		Thread gen = new Thread(() -> {

			while(running){

				long timedelta = Math.abs(expireTime - System.currentTimeMillis());
				
				if(responseCode == null || timedelta < GENBUFFER){
					generateResponse();
				}
				
				try {
					Thread.sleep(50);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		});
		
		gen.setDaemon(true);
		gen.start();
	}
	
	public CaptchaBanker(SiteKey adidasUsBackdoor, String adidasUsSitekey) {
		this(adidasUsBackdoor.getHomeUrl(), adidasUsSitekey);
	}

	private void generateResponse(){

		String capkey = cap.solveCaptcha();
		
		expireTime = System.currentTimeMillis() + 120000;
		responseCode = capkey;
		//System.out.println(responseCode);
	}

	/**
	 * 
	 * @return waits for response to be set then returns it
	 */
	public String waitForResponse(){
		while(true){
			String response = takeResponse();
			if(response != null){
				return response;
			}else{
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
	}
	
	/**
	 * 
	 * @return response if response code is set, otherwise null
	 */
	public String takeResponse(){
		checkExpire();
		String res = responseCode;
		responseCode = null;
		return res;
	}
	
	/**
	 * 
	 * @return if this object contains a valid captcha response
	 */
	public boolean hasResponse(){
		checkExpire();
		return responseCode != null;
	}
	
	
	/**
	 * Debug method that shouldnt be used to take captcha
	 */
	public String viewResponse(){
		checkExpire();
		return responseCode;
	}

	private void checkExpire() {
		if(System.currentTimeMillis() >= expireTime){
			responseCode = null;
		}
	}
	
	public void shutOff(){
		running = false;
	}
	
	public String getCaptchaKey(){
		return captchaKey;
	}

}
