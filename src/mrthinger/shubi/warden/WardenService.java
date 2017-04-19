package mrthinger.shubi.warden;

import java.util.List;

import javax.ws.rs.ProcessingException;

import javafx.stage.Stage;
import mrthinger.shubi.Info;
import mrthinger.shubi.ShubiMain;
import mrthinger.shubi.type.Backdoor;

public class WardenService {

	private volatile String sessionKey;
	private volatile String loginKey;

	private Stage stage;

	private volatile boolean running;


	public WardenService(String loginKey, String sessionKey, Stage stage){
		this.loginKey = loginKey;
		this.sessionKey = sessionKey;
		this.stage = stage;
		running = true;

		Thread t = new Thread(() -> {
			while(running){
				try{
					String checkIn = ShubiMain.shubiAuthService.checkIn(this.sessionKey);
					if(checkIn.contains("VALID:")){
						this.sessionKey = checkIn.split(":")[1];
						if(Info.WARDEN_VERBOSE)System.out.println("Valid session! " + this.sessionKey);
					}else{
						//TODO: retry login 5 times or something before punishing user
						if(Info.WARDEN_VERBOSE)System.out.println("Invalid session!!!");
					}
				}catch(ProcessingException e){
					System.out.println("Web API down!");
				}
				try {
					Thread.sleep(10000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		t.setDaemon(true);
		t.start();
	}


	public void logout(){
		try{
			boolean successfulLogout = ShubiMain.shubiAuthService.logout(sessionKey);
			if(successfulLogout){
				if(Info.WARDEN_VERBOSE)System.out.println("Successfully logged out!");
			}else{
				if(Info.WARDEN_VERBOSE)System.out.println("Something fishy about logout");
			}
			running = false;
		}catch(ProcessingException e){
			System.out.println("Web API down!");
		}
	}

	public List<Backdoor> getBackdoors() throws ProcessingException{
		return ShubiMain.shubiAuthService.getBackdoors(sessionKey);
	}





}
