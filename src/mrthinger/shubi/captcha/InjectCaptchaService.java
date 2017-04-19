package mrthinger.shubi.captcha;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mrthinger.shubi.Info;
import mrthinger.shubi.type.Backdoor;

public class InjectCaptchaService {

	private volatile PooledCaptchaBank cBank;
	private volatile ObservableList<Backdoor> bdList;
	private volatile boolean running;
	private volatile boolean updating;

	public InjectCaptchaService(PooledCaptchaBank cBank, ObservableList<Backdoor> bdList){
		this.cBank = cBank;
		this.bdList = bdList;
		running = true;
		updating = false;

		Thread t = new Thread(()->{
			while(running){ 

				updateCaptchaBank();

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
			}
		});

		t.setDaemon(true);
		t.start();
	}

	public void updateCaptchaBank() {
		while(updating){
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
		}

		updating = true;
		
		ObservableList<Backdoor> tBdList = FXCollections.observableArrayList(bdList);
		
		Map<String, Integer> siteKeyUsers = new HashMap<>();

		for(Backdoor s : tBdList){
			if(s.needsCaptcha() && s.getUsers() > 0 && s.isCaptchaGenEnabled()){
				//System.out.println(s.getName() + ":" + s.getUsers());
				if(siteKeyUsers.containsKey(s.getSiteKey())){
					int currentUsers = siteKeyUsers.get(s.getSiteKey());
					currentUsers += s.getUsers();
					siteKeyUsers.replace(s.getSiteKey(), currentUsers);
				}else{
					siteKeyUsers.put(s.getSiteKey(), s.getUsers());
				}
			}

		}

		//Update captcha request amount for siteKeys
		for(Entry<String, Integer> siteKeyUser : siteKeyUsers.entrySet()){
			//System.out.println(siteKeyUser.getKey() + ":" + siteKeyUser.getValue());
			Backdoor selectedBd = null;
			for(Backdoor s : tBdList){
				if(s.needsCaptcha()){
					if(s.getSiteKey().equals(siteKeyUser.getKey())){
						selectedBd = s;
						break;
					}

				}
			}

			cBank.setUsers(siteKeyUser.getKey(),
					selectedBd.getSiteKeyUrl(), siteKeyUser.getValue());
		}
		
		//Stop generating captchas for unused siteKeys
		for(String siteKey : cBank.getSiteKeys()){
			//if(Info.MONITOR_HARVEST)System.out.println(siteKey+ " : "+cBank.getNumBankers(siteKey));
			if(!siteKeyUsers.containsKey(siteKey)){
				cBank.removeAllBankers(siteKey);
			}
		}

		//System.out.println(cBank.getNumBankers("6Le4AQgUAAAAAABhHEq7RWQNJwGR_M-6Jni9tgtA"));

		updating = false;
	}

	public void shutOff(){
		running = false;
	}

	public PooledCaptchaBank getBank(){
		return cBank;
	}

}
