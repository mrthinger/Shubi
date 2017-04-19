package mrthinger.shubi.task;

import java.util.ArrayList;
import java.util.Map;

import mrthinger.shubi.IntPointer;
import mrthinger.shubi.SiteKey;
import mrthinger.shubi.type.AccountStore;
import mrthinger.shubi.type.Proxy;
import mrthinger.shubi.type.ProxyStore;
import mrthinger.shubi.type.SizeList;

public class TaskBank {

	protected String shoe;
	protected volatile IntPointer remainingPairs;

	protected SizeList sizeList;

	protected SiteKey site;
	protected String url;
	
	protected AccountStore accountStore;
	
	protected Map<String, String> extraInfo;

	//proxy
	protected ProxyStore proxyStore;
	
	protected int numTasks;
	
	protected volatile ArrayList<Task> tasks;

	protected boolean useTestedProxies;
	protected boolean useUntestedProxies;
	
	//protected TaskBank constuctor for nikelaunch
	protected TaskBank(String shoe, SiteKey site, SizeList sizeList,
			IntPointer remainingPairs, String url, ProxyStore proxyStore, String numTasks,
			AccountStore nikeAccounts, boolean useTestedProxies, boolean useUntestedProxies){
		this.shoe = shoe;
		this.site = site;
		this.sizeList = sizeList;
		this.remainingPairs = remainingPairs;
		this.url = url;
		this.proxyStore = proxyStore;
		this.numTasks = Integer.parseInt(numTasks);
		this.tasks = new ArrayList<>();
		this.accountStore = nikeAccounts;
		this.useTestedProxies = useTestedProxies;
		this.useUntestedProxies = useUntestedProxies;
	}
	
	public TaskBank(String shoe, SiteKey site, SizeList sizeList,
			IntPointer remainingPairs, String url, AccountStore accountStore,
			ProxyStore proxyStore, String numTasks, boolean useTestedProxies, boolean useUntestedProxies){
		this.shoe = shoe;
		this.site = site;
		this.sizeList = sizeList;
		this.remainingPairs = remainingPairs;
		this.url = url;
		this.accountStore = accountStore;
		this.proxyStore = proxyStore;
		this.numTasks = Integer.parseInt(numTasks);
		this.tasks = new ArrayList<>();
		this.useTestedProxies = useTestedProxies;
		this.useUntestedProxies = useUntestedProxies;
	}
	
	public ArrayList<Task> generateTasks(){
		for(int i = 0; i < this.numTasks; i++){
			tasks.add(genTask());
		}
		return tasks;
	}
	
	public Task genTask(){
		Task task = new Task(site, shoe, sizeList, url, remainingPairs, accountStore);
		try {
			Proxy proxy = proxyStore.takeProxy(site, useTestedProxies, useUntestedProxies);
			task.setProxy(proxy);
		} 
		//An exception here means that no viable proxy was found for this task
		catch (Exception e) {}
		
		if(extraInfo != null){
			task.setExtraInfo(extraInfo);
		}
		return task;
	}
	
	public void executeTasks(){
		for(Task t : tasks){
			TaskExecuter.execute(t);
		}
	}

	public ArrayList<Task> getTasks(){
		return tasks;
	}
	
	public AccountStore getAccountStore(){
		return accountStore;
	}
	
	public IntPointer getRemainingPairsPointer(){
		return remainingPairs;
	}
	
	public int getRemainingPairs() {
		return remainingPairs.getI();
	}
	
	public void setRemainingPairs(int remainingPairs) {
		this.remainingPairs.setI(remainingPairs);
	}

	public int decrementRemainingPairs(){
		return this.remainingPairs.decrementI();
	}

	public String getShoe(){
		return shoe;
	}

	public SiteKey getSite() {
		return site;
	}

	public ProxyStore getProxyStore() {
		return proxyStore;
	}

	public Map<String, String> getExtraInfo() {
		return extraInfo;
	}

	public void setExtraInfo(Map<String, String> extraInfo) {
		this.extraInfo = extraInfo;
	}
}
