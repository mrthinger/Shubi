package mrthinger.shubi;

import java.util.concurrent.TimeUnit;

public class Info {

	//DEBUG OPTIONS
	public static final boolean DEBUG = true;
	
	//this causes too much lag when on
	public static final boolean FOOTSITE_VERBOSE = false;
	
	public static final boolean SITEDRIVER_VERBOSE = false;
	public static final boolean CAPTCHASERVICE_VERBOSE = true;
	public static final boolean ADIDAS_VERBOSE = true;
	public static final boolean NIKE_VERBOSE = false;
	public static final boolean WARDEN_VERBOSE = false;
	public static final boolean MONITOR_HARVEST = true;
	
	//Require key?
	public static final boolean LOGIN = true;
	
	//ACTUALLY COMPLETE ORDERS?
	public static final boolean ORDER_SHOES = true;
	
	//VERSION
	public static final int BUILD = 35;
	public static final String VERSION = "1.1.1." + BUILD;
	public static final String TITLE = "MrThinger's Shubi v" + VERSION;
	
	//SERVER
	public static final String SERVER_IP = "45.32.6.227";
	//public static final String SERVER_IP = "127.0.0.1";
	public static final String SERVER_PORT = "8080";
	
	//YeezyConstants
	public static final String YEEZY_US_URL = "http://www.adidas.com/yeezy";
	public static final String YEEZY_NAME = "YZY";

	//---------------------------------------------------------------------------
	//SETTINGS MENU
	
	//Captcha
	public static volatile long CAPTCHA_TIMEOUT = TimeUnit.MINUTES.toMillis(2);
	public static volatile String TWOCAPTCHA_KEY = "";
	
	//Task
	public static volatile String ADIDAS_US_SITEKEY = "6Le4AQgUAAAAAABhHEq7RWQNJwGR_M-6Jni9tgtA";
	public static volatile String ADIDAS_UK_SITEKEY = "6LeOnCkTAAAAAK72JqRneJQ2V7GvQvvgzsVr-6kR";
	//MAX ADD TO CART FAILURES
	public static volatile int MAX_FAILURES = 8;
	//Max Tasks per proxy per site
	public static volatile int MAX_PROCESS = 1;
	
}