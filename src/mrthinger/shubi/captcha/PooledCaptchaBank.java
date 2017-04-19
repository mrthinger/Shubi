package mrthinger.shubi.captcha;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PooledCaptchaBank {
	//TODO: make this thread safe, changing the size of arrays in different threads is probably not a good idea. 
	//Maybe somekind of variable to track if a request is already being made

	private volatile List<CaptchaBanker> bankers;
	private volatile Map<String, Integer> users;

	public PooledCaptchaBank(){
		bankers = new ArrayList<>();
		users = new HashMap<>();
	}

	public List<String> getSiteKeys(){
		Map<String, Integer> usersCopy = new HashMap<>(users);
		List<String> siteKeys = new ArrayList<>();
		
		for(Entry<String, Integer> e : usersCopy.entrySet()){
			siteKeys.add(e.getKey());
		}
		
		return siteKeys;
	}
	
	/**
	 * @param siteKey of requested captcha response 
	 * @return waits for response to be set then returns it
	 */
	public String waitForResponse(String siteKey){
		while(true){
			String response = takeResponse(siteKey);
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
	 * @param siteKey of requested captcha response 
	 * @return response if response code is set, otherwise null
	 */
	public String takeResponse(String siteKey){
		List<CaptchaBanker> bankersCopy = new ArrayList<>(bankers);
		
		for(CaptchaBanker cB : bankersCopy){
			if(cB.getCaptchaKey().equals(siteKey) && cB.hasResponse()){
				return cB.takeResponse();
			}
		}

		return null;
	}


	public void setUsers(String siteKey, String url, int numUsers) {
		int currentUsers = 0;

		if(users.containsKey(siteKey)){
			currentUsers = users.get(siteKey);
			
			if(numUsers > currentUsers){
				addUsers(siteKey, url, numUsers - currentUsers);
			}else if(numUsers < currentUsers){
				removeUsers(siteKey, currentUsers - numUsers);
			}
			
			
			
		}else{
			addUsers(siteKey, url, numUsers);
		}

	}

	public void addUsers(String siteKey, String url, int numUsers){
		if(!users.containsKey(siteKey)){
			users.put(siteKey, numUsers);
		}else{
			users.replace(siteKey, users.get(siteKey).intValue() + numUsers);
		}

		int usersForKey = users.get(siteKey);
		int bankersForKey = getNumBankers(siteKey);

		//TODO: make this equation set in the constructor
		int wantedBankers = (int)(2 * Math.sqrt((double)usersForKey));

		if(wantedBankers > bankersForKey){
			int bankersToAdd = wantedBankers - bankersForKey;
			addBankers(siteKey, url, bankersToAdd);
		}
	}

	public void removeUsers(String siteKey, int numUsers){
		if(users.containsKey(siteKey)){
			users.replace(siteKey, users.get(siteKey).intValue() - numUsers);

			int usersForKey = users.get(siteKey);
			int bankersForKey = getNumBankers(siteKey);

			int wantedBankers = (int)(2 * Math.sqrt((double)usersForKey));

			if(bankersForKey > wantedBankers){
				int bankersToRemove = bankersForKey - wantedBankers;
				removeBankers(siteKey, bankersToRemove);
			}
		}	
	}

	public void addBankers(String siteKey, String url, int numberOfBankers){
		for(int i = 0; i < numberOfBankers; i++){
			addBanker(new CaptchaBanker(url, siteKey));
		}
	}

	public void addBanker(String siteKey, String url){
		addBanker(new CaptchaBanker(url, siteKey));
	}

	public void addBanker(CaptchaBanker banker){
		bankers.add(banker);
	}

	public int getNumBankers(String siteKey){
		List<CaptchaBanker> bankersCopy = new ArrayList<>(bankers);
		int n = 0;
		for(CaptchaBanker cB : bankersCopy){
			if(cB.getCaptchaKey().equals(siteKey)){
				n++;
			}
		}
		return n;
	}
	
	public int getNumBanked(String siteKey){
		List<CaptchaBanker> bankersCopy = new ArrayList<>(bankers);
		int n = 0;
		for(CaptchaBanker cB : bankersCopy){
			if(cB.getCaptchaKey().equals(siteKey) && cB.hasResponse()){
				n ++;
			}
		}
		return n;
	}

	public void removeBanker(String siteKey){
		for(int i = bankers.size() - 1; i >= 0; i--){
			if(bankers.get(i).getCaptchaKey().equals(siteKey)){
				bankers.get(i).shutOff();
				bankers.remove(i);
				break;
			}
		}
	}

	public void removeBankers(String siteKey, int numToRemove){
		for(int i = bankers.size() - 1; i >= 0; i--){
			if(numToRemove > 0){
				if(bankers.get(i).getCaptchaKey().equals(siteKey)){
					bankers.get(i).shutOff();
					bankers.remove(i);
					numToRemove--;
				}
			}else{
				break;
			}
		}
	}

	public void removeAllBankers(String siteKey){
		users.replace(siteKey, 0);
		for(int i = bankers.size() - 1; i >= 0; i--){
			if(bankers.get(i).getCaptchaKey().equals(siteKey)){
				bankers.get(i).shutOff();
				bankers.remove(i);
			}
		}
	}
	
	public void removeAllBankers(){
		users = new HashMap<>();
		for(int i = bankers.size() - 1; i >= 0; i--){
				bankers.get(i).shutOff();
				bankers.remove(i);
			
		}
	}

}
