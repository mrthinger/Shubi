package mrthinger.shubi.type;

import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import mrthinger.shubi.BackdoorConstants;
import mrthinger.shubi.KeyGenerator;

@XmlRootElement
@XmlType(propOrder={"name", "boundShoeName", "site", "url", "postParams", "extraInfo", "users", "captchaGenEnabled"})
public class Backdoor {

	private String name;
	private String url;
	private String site;
	private Map<String, String> postParams;
	private Map<String, String> extraInfo;
	
	//set by user
	private String boundShoeName;
	private int users;
	private boolean captchaGenEnabled = false;
	
	public Backdoor(){}
	
	public Backdoor(String site, String name, String url, Map<String, String> postParams, Map<String, String> extraInfo){
		this.site = site;
		this.name = name;
		this.postParams = postParams;
		this.url = url;
		this.extraInfo = extraInfo;
	}
	
	public Map<String, String> getPostParams(){
		return postParams;
	}
	
	public void setPostParams(Map<String, String> postParams){
		this.postParams = postParams;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Map<String, String> getExtraInfo() {
		return extraInfo;
	}

	public void setExtraInfo(Map<String, String> legend) {
		this.extraInfo = legend;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public boolean needsCaptcha(){
		boolean needsCaptcha = false;
		for(Entry<String, String> e : postParams.entrySet()){
			if(e.getValue().equals(BackdoorConstants.CAPTCHA)){
				needsCaptcha = true;
				break;
			}
		}
		return needsCaptcha;
	}
	
	public String getSiteKey(){
		String siteKey = null;
		for(Entry<String, String> e : extraInfo.entrySet()){
			if(e.getKey().equals(BackdoorConstants.SITEKEY)){
				siteKey = e.getValue();
				break;
			}
		}
		return siteKey;
	}
	
	public String getSiteKeyUrl(){
		String siteKeyUrl = null;
		for(Entry<String, String> e : extraInfo.entrySet()){
			if(e.getKey().equals(BackdoorConstants.SITEKEY_URL)){
				siteKeyUrl = e.getValue();
				break;
			}
		}
		return siteKeyUrl;
	}
	
	@Override
	public String toString(){
		String bd = "name`" + name 
				+ "\nurl`" + url;
		for(Entry<String, String> e:extraInfo.entrySet()){
			bd += "\n" + e.getKey() + "`" + e.getValue();
		}
		
		return bd;
	}
	
	public String toJSInject(){
		KeyGenerator keyGen = new KeyGenerator();
		StringBuilder scriptBuilder = new StringBuilder();
		
		//Make form
		scriptBuilder.append("var f = document.createElement('form');"
				+ "f.setAttribute('id','shubiPost');"
				+ "f.setAttribute('method','post');"
				+ "f.setAttribute('action','" + url + "');");
		
		//Add params
		for(Entry<String, String> param : postParams.entrySet()){
			String elementKey = keyGen.genUniqueKey(3);
			scriptBuilder.append(
					"var " + elementKey + " = document.createElement('input');"
					+ elementKey + ".setAttribute('name','" + param.getKey() + "');"
					+ elementKey + ".setAttribute('value','" + param.getValue() + "');"
					+ "f.appendChild("+elementKey+");"
			);
			
			//System.out.println(param.getKey() + "=" + param.getValue());
		}
		
		//Add submit button
		String submitElement = "submit";
		scriptBuilder.append(
				"var " + submitElement + " = document.createElement('input');"
				+ submitElement + ".setAttribute('type','submit');"
				+ submitElement + ".setAttribute('value','Submit');"
				+ "f.appendChild("+submitElement+");"
		);
		
		//Add it to page
		scriptBuilder.append("document.getElementsByTagName('body')[0].appendChild(f);");
		
		//Submit
		scriptBuilder.append("document.getElementById(document.querySelector(\"[id^='shubiPost']\").id).submit();");
		//Make script
		String injectScript = scriptBuilder.toString();

		return injectScript;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getBoundShoeName() {
		return boundShoeName;
	}

	public void setBoundShoeName(String boundShoeName) {
		this.boundShoeName = boundShoeName;
	}

	public int getUsers() {
		return users;
	}

	public void setUsers(int users) {
		this.users = users;
	}

	public boolean isCaptchaGenEnabled() {
		return captchaGenEnabled;
	}

	public void setCaptchaGenEnabled(boolean captchaGenEnabled) {
		this.captchaGenEnabled = captchaGenEnabled;
	}
	
}
