package mrthinger.shubi.proxyload;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import mrthinger.shubi.type.Proxy;
import mrthinger.shubi.type.ProxyStore;

public class ProxyTestDriver implements Runnable{

	private volatile ProxyStore pList;
	private List<String> urlsToTest;
	private volatile ProxyStore testedStore;
	
	private List<UrlTest> tests;
	
	private volatile boolean running;
	private volatile double decimalComplete;
	
	private ProgressBar progressBar;
	private Label statusLabel;

	//tested P is output for tested proxies
	public ProxyTestDriver(ProxyStore testedP, ProgressBar progressBar, Label statusLabel) {
		this.tests = new ArrayList<>();
		this.running = false;
		this.testedStore = testedP;
		this.decimalComplete = 0;
		this.progressBar = progressBar;
		this.statusLabel = statusLabel;
	}

	@Override
	public void run() {
		this.tests = new ArrayList<>();
		
		running = true;
		
		for(Proxy p : pList.getProxies()){
			tests.add(new UrlTest(p, urlsToTest));
		}
		
		for(UrlTest test : tests){
			Thread t = new Thread(test);
			t.setDaemon(true);
			t.start();
		}
		
		while(running){
			//track total status
			int totalNums = 0;
			int totalDenoms = 0;
			boolean allDone = true;
			for(UrlTest test : tests){
				totalNums += test.getComplete();
				totalDenoms += test.getTotal();
				if(test.isRunning()) allDone = false;
			}
			
			decimalComplete = (double)totalNums / (double)totalDenoms;
			Platform.runLater(() -> progressBar.setProgress(decimalComplete)); 
			
			if(allDone){
				Platform.runLater(() -> {
					progressBar.setProgress(0);
					statusLabel.setText("Status: Test Complete!");
				}); 
				testedStore.addAndUpdateProxies(getProxyTestingObsList());
				running = false;
				break;
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
		}
		
	}
	
	public ObservableList<Proxy> getProxyTestingObsList(){
		return pList.getProxies();
	}

	public ProxyStore getTestedProxyStore(){
		return pList;
	}
	
	public boolean isRunning() {
		return running;
	}

	public double getDecimalComplete() {
		return decimalComplete;
	}

	public void setpList(ProxyStore pList) {
		this.pList = pList;
	}

	public void setUrlsToTest(List<String> urlsToTest) {
		this.urlsToTest = urlsToTest;
	}

}
