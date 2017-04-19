package mrthinger.shubi.task;

import java.util.ArrayList;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mrthinger.shubi.IntPointer;
import mrthinger.shubi.SiteKey;
import mrthinger.shubi.type.AccountStore;
import mrthinger.shubi.type.ProxyStore;
import mrthinger.shubi.type.SizeList;

public class TaskMaster {

	private ArrayList<TaskBank> taskBanks;
	private ObservableList<Task> tasks;
	private boolean creatingTasks;
	private ObservableList<Task> nonAtnTasks;
	private ObservableList<Task> atnTasks;

	public TaskMaster() {
		taskBanks = new ArrayList<>();
		tasks = FXCollections.observableArrayList();
		nonAtnTasks = FXCollections.observableArrayList();
		atnTasks = FXCollections.observableArrayList();
		creatingTasks = false;
	}

	public void addTasks(AccountStore accountStore, ProxyStore proxyStore, String shoe, SiteKey site,
			SizeList sizeList, String link, String maxPairs, String numTasks, Map<String, String> extraInfo,
			boolean useTestedProxies, boolean useUntestedProxies) {
		creatingTasks = true;
		//make new sizelist, its not important to sync
		SizeList sizeListCopy = new SizeList(sizeList);

		//AccountStore-site sync
		AccountStore accountStoreCopy = null;
		boolean copyAccountStore = true;
		
		for(TaskBank tb : taskBanks){
			if(tb.getSite().equals(site) && !tb.getAccountStore().equals(new AccountStore())){
				accountStoreCopy = tb.getAccountStore();
				copyAccountStore = false;
				break;
			}
		}
		
		if(copyAccountStore){
			if(accountStore != null){
				accountStoreCopy = new AccountStore(accountStore);
			}else{
				accountStore = new AccountStore();
			}
		}
		
		//ProxyStore-site sync
		ProxyStore proxyStoreCopy = null;
		boolean copyProxyList = true;

		for(TaskBank tb : taskBanks){
			if (tb.getSite().equals(site) && tb.getProxyStore().getNumProxies() > 0){
				if(proxyStore != null){
					tb.getProxyStore().addAndUpdateProxies(proxyStore.deepCopyProxyList());
				}
				
				proxyStoreCopy = tb.getProxyStore();
				
				copyProxyList = false;
				break;
			}
		}

		if(copyProxyList){
			if(proxyStore != null){
				proxyStoreCopy = new ProxyStore(proxyStore.deepCopyProxyList());
			}else{
				proxyStoreCopy = new ProxyStore();
			}
		}

		//Shoe-maxPair sync
		IntPointer remainingPairs = null;
		boolean makeNewRPPointer = true;

		for(TaskBank tb : taskBanks){
			if (tb.getShoe().equals(shoe)){
				remainingPairs = tb.getRemainingPairsPointer();
				makeNewRPPointer = false;
				break;
			}
		}
		if(makeNewRPPointer){
			remainingPairs = new IntPointer(Integer.parseInt(maxPairs));
		}


		TaskBank newTaskBank;
		if(site.equals(SiteKey.NIKELAUNCH)){
			newTaskBank = new NikeTaskBank(shoe, site, sizeListCopy, remainingPairs, 
					link, proxyStoreCopy, numTasks, accountStore, useTestedProxies, useUntestedProxies);
		}else{
			newTaskBank = new TaskBank(shoe, site, sizeListCopy, remainingPairs, link,
					accountStore, proxyStoreCopy, numTasks, useTestedProxies, useUntestedProxies);
			if(extraInfo != null){
				newTaskBank.setExtraInfo(extraInfo);
			}
		}

		taskBanks.add(newTaskBank);

		tasks.addAll(newTaskBank.generateTasks());
		newTaskBank.executeTasks();
		
		creatingTasks=false;

	}

	public ObservableList<Task> getTasks(){
		return tasks;
	}
	
	public int getNumTasksWithShoe(String name){
		int n = 0;
		ObservableList<Task> tempTasks = FXCollections.observableArrayList(tasks);
		for(Task t : tempTasks){
			if(t.getShoe().equals(name)){
				n++;
			}
		}
		return n;
	}
	
	public int getNumTasksWithShoeAndSite(String name, SiteKey site){
		int n = 0;
		ObservableList<Task> tempTasks = FXCollections.observableArrayList(tasks);
		for(Task t : tempTasks){
			if(t.getShoe().equals(name) && t.getSite().equals(site)){
				n++;
			}
		}
		return n;
	}
	
	public void updateAtnLists(){
		ObservableList<Task> tempTasks = FXCollections.observableArrayList(tasks);
		ObservableList<Task> tempNonAtnTasks = FXCollections.observableArrayList();
		ObservableList<Task> tempAtnTasks = FXCollections.observableArrayList();
		for(Task t : tempTasks){
			if(t.hasCart() || t.needsAtn()){
				tempAtnTasks.add(t);
			}else{
				tempNonAtnTasks.add(t);
			}
		}
		
	
		nonAtnTasks.retainAll(tempNonAtnTasks);
		
		for(Task t : tempNonAtnTasks){
			if(!nonAtnTasks.contains(t)){
				nonAtnTasks.add(t);
			}
		}
		
		atnTasks.retainAll(tempAtnTasks);
	
		for(Task t : tempAtnTasks){
			if(!atnTasks.contains(t)){
				atnTasks.add(t);
			}
		}
	}
	
	public ObservableList<Task> getAtnTasks(){
		return atnTasks;
	}
	
	public ObservableList<Task> getNonAtnTasks(){
		return nonAtnTasks;
	}
	
	public boolean safeToGetTasks(){
		return !creatingTasks;
	}

}
