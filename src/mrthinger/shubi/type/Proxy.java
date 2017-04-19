package mrthinger.shubi.type;

import java.util.HashMap;

import mrthinger.shubi.SiteKey;

public class Proxy {

	private String ip;
	private String port;
	private String user;
	private String pass;
	private int tasksUsing;
	
	private HashMap<SiteKey, Boolean> usage;
	private HashMap<SiteKey, Long> responseTimes;
	private boolean tested;
	
	public Proxy(String ip, String port, String user, String pass) {
		this.tasksUsing = 0;
		this.tested = false;
		this.ip = ip;
		this.port = port;
		this.user = user;
		this.pass = pass;
		this.usage = createUsageMap();
		this.responseTimes = createResponseTimeMap();
	}

	public Proxy(Proxy p) {
		this.tasksUsing = 0;
		this.ip = p.getIp();
		this.port = p.getPort();
		this.user = p.getUser();
		this.pass = p.getPass();
		this.usage = p.getUsageMap();
		this.tested = p.isTested();
		this.responseTimes = p.getResponseTimeMap();
	}

	public boolean canConnectTo(SiteKey site) {
		return usage.get(site);
	}
	
	private HashMap<SiteKey, Long> createResponseTimeMap(){
		HashMap<SiteKey, Long> resMap = new HashMap<>();
		for(SiteKey site : SiteKey.values()){
			resMap.put(site, 1L);
		}
		return resMap;
	}
	
	public void updateResponseTimeMap(SiteKey site, long responseTime){
		responseTimes.replace(site, responseTime);
	}
	
	public HashMap<SiteKey, Long> getResponseTimeMap(){
		return responseTimes;
	}
	
	private HashMap<SiteKey, Boolean> createUsageMap(){
		HashMap<SiteKey, Boolean> usageMap = new HashMap<>();
		for(SiteKey site : SiteKey.values()){
			usageMap.put(site, Boolean.TRUE);
		}
		return usageMap;
	}
	
	public void updateUsageMap(SiteKey site, boolean usable){
		usage.replace(site, usable);
	}
	
	public HashMap<SiteKey, Boolean> getUsageMap(){
		return usage;
	}
	
	public boolean isUsableOn(SiteKey site){
		return usage.get(site);
	}
	
	public void addTaskUsing(){
		tasksUsing++;
	}
	
	public void subtractTaskUsing(){
		tasksUsing--;
	}
	
	public int getNumTasksUsing(){
		return tasksUsing;
	}
	
	public String getIp() {
		return ip;
	}

	public String getPort() {
		return port;
	}

	public String getUser() {
		return user;
	}

	public String getPass() {
		return pass;
	}
	
	@Override
	public String toString(){
		return ip + " : " + port + " : " + user + " : " + pass + " : " + tasksUsing;
	}

	public void setUsage(HashMap<SiteKey, Boolean> usage) {
		this.usage = usage;
	}

	public void setResponseTimes(HashMap<SiteKey, Long> responseTimes) {
		this.responseTimes = responseTimes;
	}

	public boolean isTested() {
		return tested;
	}

	public void setTested(boolean tested) {
		this.tested = tested;
	}


}
