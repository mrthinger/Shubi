package mrthinger.shubi.captcha;

import java.io.IOException;

import mrthinger.shubi.Info;

public class TwoCaptchaService {
	
	/**
	 * This class is used to establish a connection to 2captcha.com 
	 * and receive the token for solving google recaptcha v2
	 * 
	 * @author Chillivanilli
	 * @version 1.0
	 * 
	 * If you have a custom software requests, please contact me 
	 * via forum: http://thebot.net/members/chillivanilli.174861/
	 * via eMail: chillivanilli@chillibots.com
	 * via skype: ktlotzek
	 */


	/**
	 * The google site key from the page you want to solve the recaptcha at
	 */
	private String googleKey;


	/**
	 * The URL where the recaptcha is placed.
	 * For example: https://www.google.com/recaptcha/api2/demo
	 */
	private String pageUrl;

	/**
	 * The proxy ip if you want a worker to solve the recaptcha through your proxy
	 */
	private String proxyIp;

	/**
	 * The proxy port
	 */
	private String proxyPort;

	/**
	 * Your proxy username, if your proxy uses user authentication
	 */
	private String proxyUser;

	/**
	 * Your proxy password, if your proxy uses user authentication
	 */
	private String proxyPw;

	/**
	 * Your proxy type, for example ProxyType.HTTP
	 */
	private ProxyType proxyType;

	/**
	 * The HttpWrapper which the requests are made with
	 */
	private HttpWrapper hw;


	/**
	 * Constructor if you don't use any proxy
	 * @param apiKey
	 * @param googleKey
	 * @param pageUrl
	 */
	public TwoCaptchaService(String googleKey, String pageUrl) {
		this.googleKey = googleKey;
		this.pageUrl = pageUrl;
		hw = new HttpWrapper();
	}

	/**
	 * Constructor if you are using a proxy without user authentication
	 * @param apiKey
	 * @param googleKey
	 * @param pageUrl
	 * @param proxyIp
	 * @param proxyPw
	 * @param proxyType
	 */
	public TwoCaptchaService(String googleKey, String pageUrl, String proxyIp, String proxyPort, ProxyType proxyType) {
		this(googleKey, pageUrl);
		this.proxyIp = proxyIp;
		this.proxyPort = proxyPort;
		this.proxyType = proxyType;
	}

	/**
	 * Constructor if you are using a proxy with user authentication
	 * @param apiKey
	 * @param googleKey
	 * @param pageUrl
	 * @param proxyIp
	 * @param proxyPort
	 * @param proxyUser
	 * @param proxyPw
	 * @param proxyType
	 */
	public TwoCaptchaService(String googleKey, String pageUrl, String proxyIp, String proxyPort,
			String proxyUser, String proxyPw, ProxyType proxyType) {
		this(googleKey,pageUrl);
		this.proxyIp = proxyIp;
		this.proxyPort = proxyPort;
		this.proxyUser = proxyUser;
		this.proxyPw = proxyPw;
		this.proxyType = proxyType;
	}

	/**
	 * Sends the recaptcha challenge to 2captcha.com and 
	 * checks every second if a worker has solved it
	 * 
	 * @return The response-token which is needed to solve and submit the recaptcha
	 * @throws Exception 
	 * @throws InterruptedException, when thread.sleep is interrupted
	 * @throws IOException, when there is any server issue and the request cannot be completed
	 */
	public String solveCaptcha() {
		if(Info.MONITOR_HARVEST)System.out.println("Sending recaptcha challenge to 2captcha.com");
		String gRecaptchaResponse = "";

		while(true){
			String parameters = "key=" + Info.TWOCAPTCHA_KEY
					+ "&method=userrecaptcha"
					+ "&googlekey=" + googleKey
					+ "&pageurl=" + pageUrl;

			if (proxyIp != null) {
				if (proxyUser != null) {
					parameters += "&proxy=" 
							+ proxyUser + ":" + proxyPw 
							+ "@"
							+ proxyIp + ":" + proxyPort;
				} else {
					parameters += "&proxy=" 
							+ proxyIp + ":" + proxyPort;
				}

				parameters += "&proxytype=" + proxyType;
			}
			
			try{
				hw.get("http://2captcha.com/in.php?" + parameters);
			}catch(IllegalStateException e){
				continue;
			}
		//	if(Info.MONITOR_HARVEST)System.out.println(hw.getHtml());
			String captchaId = hw.getHtml().replaceAll("\\D", "");
			long timeSpentWaiting = 0;

			boolean startOver = false;
			boolean unsolvable = false;

			while(true){
				try{
					
					double startTime = System.currentTimeMillis();
					
					hw.get("http://2captcha.com/res.php?key=" + Info.TWOCAPTCHA_KEY 
							+ "&action=get"
							+ "&id=" + captchaId);

					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {}
					
					double endTime = System.currentTimeMillis();

					timeSpentWaiting += endTime - startTime;
					if(Info.MONITOR_HARVEST)System.out.println(hw.getHtml().trim());
					if(timeSpentWaiting >= Info.CAPTCHA_TIMEOUT){
						startOver = true;
						break;
					}
					
					if(hw.getHtml().contains("OK")){
						break;
					}
					
					if(hw.getHtml().contains("ERROR_CAPTCHA_UNSOLVABLE")){
						unsolvable = true;
						break;
					}
					
				}catch(IllegalStateException e){
					continue;
				}
			}
			
			if(unsolvable){
				System.out.println(googleKey + " is UNSOLVABLE. Stop captcha generation for this key!");
				return "UNSOLVABLE";
			}
			
			if(startOver) continue;

			gRecaptchaResponse = hw.getHtml().replaceAll("OK\\|", "").replaceAll("\\n", "");
			if(Info.MONITOR_HARVEST)System.out.println(gRecaptchaResponse);

			break;

		}

		return gRecaptchaResponse;
	}

	/**
	 * 
	 * @return The google site key
	 */
	public String getGoogleKey() {
		return googleKey;
	}

	/**
	 * Sets the google site key
	 * @param googleKey
	 */
	public void setGoogleKey(String googleKey) {
		this.googleKey = googleKey;
	}

	/**
	 *
	 * @return The page url
	 */
	public String getPageUrl() {
		return pageUrl;
	}

	/**
	 * Sets the page url
	 * @param pageUrl
	 */
	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}

	/**
	 *
	 * @return The proxy ip
	 */
	public String getProxyIp() {
		return proxyIp;
	}

	/**
	 * Sets the proxy ip
	 * @param proxyIp
	 */
	public void setProxyIp(String proxyIp) {
		this.proxyIp = proxyIp;
	}

	/**
	 * 
	 * @return The proxy port
	 */
	public String getProxyPort() {
		return proxyPort;
	}

	/**
	 * Sets the proxy port
	 * @param proxyPort
	 */
	public void setProxyPort(String proxyPort) {
		this.proxyPort = proxyPort;
	}

	/**
	 * 
	 * @return The proxy authentication user
	 */
	public String getProxyUser() {
		return proxyUser;
	}

	/**
	 * Sets the proxy authentication user
	 * @param proxyUser
	 */
	public void setProxyUser(String proxyUser) {
		this.proxyUser = proxyUser;
	}

	/**
	 * 
	 * @return The proxy authentication password
	 */
	public String getProxyPw() {
		return proxyPw;
	}

	/**
	 * Sets the proxy authentication password
	 * @param proxyPw
	 */
	public void setProxyPw(String proxyPw) {
		this.proxyPw = proxyPw;
	}	

	/**
	 * 
	 * @return The proxy type
	 */
	public ProxyType getProxyType() {
		return proxyType;
	}

	/**
	 * Sets the proxy type
	 * @param proxyType
	 */
	public void setProxyType(ProxyType proxyType) {
		this.proxyType = proxyType;
	}
}