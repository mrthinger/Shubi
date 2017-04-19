package mrthinger.shubi;

import java.util.ArrayList;
import java.util.List;

public enum SiteKey {
	NIKELAUNCH("NIKE LAUNCH", "https://www.nike.com/launch/"),
	YEEZY_US("YEEZY US", "http://www.adidas.com/us/"),
	ADIDAS_US("ADIDAS US", "http://www.adidas.com/us/"),
	ADIDAS_UK("ADIDAS UK", "http://www.adidas.co.uk/"),
	ADIDAS_US_BACKDOOR("ADIDAS US BACKDOOR", "http://www.adidas.com/us/"),
	ADIDAS_UK_BACKDOOR("ADIDAS UK BACKDOOR", "http://www.adidas.co.uk/"),
	FOOTLOCKER("FOOTLOCKER", "http://www.footlocker.com/"),
	EASTBAY("EASTBAY", "http://www.eastbay.com/"),
	CHAMPS("CHAMPS", "http://www.champssports.com/"),
	FOOTACTION("FOOTACTION", "http://www.footaction.com/")
	//UBIQ("UBIQ", "ubiqsite..."),
	//SNEAKERSNSTUFF("SNEAKERSNSTUFF"),
	//FOOTACTION("FOOTACTION"),
	//FINISHLINE("FINISHLINE")
	;
	private String name;
	private String homeUrl;

	SiteKey(String name, String homeUrl){
		this.name = name;
		this.homeUrl = homeUrl;
	}

	@Override
	public String toString(){
		return name;
	}

	public String getName(){
		return name;
	}

	public String getHomeUrl(){
		return homeUrl;
	}

	public static SiteKey getByName(String name){
		for(SiteKey site : SiteKey.values()){
			if(site.getName().equals(name)){
				return site;
			}
		}
		return null;
	}

	public static List<SiteKey> getByUrl(String url){
		List<SiteKey> sites = new ArrayList<>();

		for(SiteKey site : SiteKey.values()){
			if(site.getHomeUrl().equals(url)){
				sites.add(site);
			}
		}
		return sites;
	}
}
