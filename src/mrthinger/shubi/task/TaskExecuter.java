package mrthinger.shubi.task;

import org.openqa.selenium.remote.DesiredCapabilities;

import mrthinger.shubi.SiteKey;
import mrthinger.shubi.gui.WindowMainController;
import mrthinger.shubi.sitedriver.AdidasBackdoorDriver;
import mrthinger.shubi.sitedriver.AdidasSiteDriver;
import mrthinger.shubi.sitedriver.FootsiteSiteDriver;
import mrthinger.shubi.sitedriver.NikeLaunchSiteDriver;
import mrthinger.shubi.sitedriver.ResponseCode;
import mrthinger.shubi.sitedriver.SiteDriver;
import mrthinger.shubi.sitedriver.YeezySiteDriver;

public class TaskExecuter {

	public static void execute(Task task){

		SiteKey site = task.getSite();

		Thread t = new Thread(new Runnable() {

			@Override
			public void run(){

				DesiredCapabilities dc = SiteDriver.genCapabilities(task.getProxy());

				SiteDriver driver = null;

				//Nike
				if(site.equals(SiteKey.NIKELAUNCH)){
					driver = new NikeLaunchSiteDriver(dc, task);
				}

				//Adidas
				else if(site.equals(SiteKey.ADIDAS_US) || site.equals(SiteKey.ADIDAS_UK)){
					driver = new AdidasSiteDriver(dc, task);
				}

				//Adidas backdoor sites
				else if(site.equals(SiteKey.ADIDAS_US_BACKDOOR) || site.equals(SiteKey.ADIDAS_UK_BACKDOOR)){
					driver = new AdidasBackdoorDriver(dc, task);
				}
				
				//Footsites
				else if(site.equals(SiteKey.FOOTLOCKER) || site.equals(SiteKey.CHAMPS)
						|| site.equals(SiteKey.FOOTACTION) || site.equals(SiteKey.EASTBAY)){
					driver = new FootsiteSiteDriver(dc, task);
				}
				
				//YEEZY!
				else if(site.equals(SiteKey.YEEZY_US)){
					driver = new YeezySiteDriver(dc, task);
				}
				
				//Something wrong happens
				else{
					driver = new SiteDriver(dc, task);
				}


				while(task.getDriver().isRunning()){
					try {

						int response = driver.checkOut();

						if(response == ResponseCode.RESTART){
							continue;
						}
						
						else if(response == ResponseCode.WAIT_FOR_USER || response == ResponseCode.SUCCESS){
							//handle requests, restart if requested
							boolean restart = false;
							while(task.getDriver().isRunning()){
								int request = task.getDriver().handleRequests();
								if(request == ResponseCode.REQUEST_RESTART){
									restart = true;
									break;
								}
								Thread.sleep(50);
							}
							if(restart){
								continue;
							}
						}


					} catch (Exception e) {
						e.printStackTrace();
						task.setStatus("CRASHED LOL");
						
						//handle requests, restart if requested
						boolean restart = false;
						while(task.getDriver().isRunning()){
							int request = task.getDriver().handleRequests();
							if(request == ResponseCode.REQUEST_RESTART){
								restart = true;
								break;
							}
							try {
								Thread.sleep(50);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
						}
						if(restart){
							continue;
						}
					}
				}

			}
		});

		t.setDaemon(true);
		t.start();

	}
}
