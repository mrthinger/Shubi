package mrthinger.shubi;

import java.util.logging.Level;

import org.apache.commons.logging.LogFactory;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.stage.Stage;
import mrthinger.shubi.captcha.PooledCaptchaBank;
import mrthinger.shubi.gui.BackdoorController;
import mrthinger.shubi.gui.LoginController;
import mrthinger.shubi.gui.ProxyController;
import mrthinger.shubi.gui.SettingsController;
import mrthinger.shubi.gui.WindowMainController;
import mrthinger.shubi.warden.ShubiAuthWrapper;
import mrthinger.shubi.warden.WardenService;


public class ShubiMain extends Application {

	public static volatile ShubiAuthWrapper shubiAuthService;
	public static volatile WardenService ws;
	public static volatile PooledCaptchaBank injectCaptchaPool;
	public static volatile PooledCaptchaBank manualCaptchaPool;
	public static volatile InjectGenerator iGen;
	

	public static void main(String[] args) {	

		//Get chromedriver
		if(System.getProperty("os.name").contains("Mac")){
			System.setProperty("webdriver.chrome.driver", "/Users/MrThinger/Desktop/chromedrivermac");
		}else{
			System.setProperty("webdriver.chrome.driver", System.getProperty("user.home") + "/Desktop/chromedriver.exe");
		}

		Application.launch();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		//Shut off htmlunit logger
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
		java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
		java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);

		//initiate server session
		shubiAuthService = new ShubiAuthWrapper();

		//Start services
		injectCaptchaPool = new PooledCaptchaBank();
		iGen = new InjectGenerator(injectCaptchaPool);
		manualCaptchaPool = new PooledCaptchaBank();
		
		//Main window
		FXMLLoader loader = new FXMLLoader();
		loader.setBuilderFactory(new JavaFXBuilderFactory());
		loader.setLocation(ClassLoader.getSystemClassLoader().getResource("mrthinger/shubi/gui/ShubiMainWindow.fxml"));
		loader.load();

		WindowMainController winMain = (WindowMainController) loader.getController();

		//Load proxy config window
		FXMLLoader loader3 = new FXMLLoader();
		loader3.setBuilderFactory(new JavaFXBuilderFactory());
		loader3.setLocation(ClassLoader.getSystemClassLoader().getResource("mrthinger/shubi/gui/ProxyConfigWindow.fxml"));
		loader3.load();

		ProxyController proxyController = (ProxyController) loader3.getController();
		winMain.setProxyController(proxyController);

		//Load settings window
		FXMLLoader loader4 = new FXMLLoader();
		loader4.setBuilderFactory(new JavaFXBuilderFactory());
		loader4.setLocation(ClassLoader.getSystemClassLoader().getResource("mrthinger/shubi/gui/SettingsWindow.fxml"));
		loader4.load();

		SettingsController settingsController = (SettingsController) loader4.getController();
		winMain.setSettingsController(settingsController);

		//Load backdoor window
		FXMLLoader loader5 = new FXMLLoader();
		loader5.setBuilderFactory(new JavaFXBuilderFactory());
		loader5.setLocation(ClassLoader.getSystemClassLoader().getResource("mrthinger/shubi/gui/BackdoorWindow.fxml"));
		loader5.load();
		
		BackdoorController bdController = (BackdoorController) loader5.getController();
		winMain.setBackdoorController(bdController);
		bdController.setiGen(iGen);
		bdController.setTaskMaster(winMain.getTaskMaster());

		//Auth
		if(Info.LOGIN){
			FXMLLoader loader2 = new FXMLLoader();
			loader2.setBuilderFactory(new JavaFXBuilderFactory());
			loader2.setLocation(ClassLoader.getSystemClassLoader().getResource("mrthinger/shubi/gui/LoginWindow.fxml"));
			loader2.load();

			LoginController loginControl = (LoginController) loader2.getController();

			loginControl.setShubiController(winMain);
			loginControl.setPrimaryStage(primaryStage);

		}else{
			winMain.setPrimaryStage(primaryStage);
		}

		primaryStage.show();
	}
}
