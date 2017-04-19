package mrthinger.shubi.task;

import java.util.ArrayList;

import mrthinger.shubi.IntPointer;
import mrthinger.shubi.SiteKey;
import mrthinger.shubi.type.Account;
import mrthinger.shubi.type.AccountStore;
import mrthinger.shubi.type.Proxy;
import mrthinger.shubi.type.ProxyStore;
import mrthinger.shubi.type.SizeList;

public class NikeTaskBank extends TaskBank{


	public NikeTaskBank(String shoe, SiteKey site, SizeList sizeList, IntPointer remainingPairs,
			String url, ProxyStore proxyStore, String numTasks, AccountStore nikeAccounts,
			boolean useTestedProxies, boolean useUntestedProxies){
		super(shoe, site, sizeList, remainingPairs, url, proxyStore, numTasks, nikeAccounts, useTestedProxies, useUntestedProxies);


	}

	@Override
	public ArrayList<Task> generateTasks(){
		for(int i = 0; i < this.numTasks; i++){

			Task task = genTask();
			if(task != null){
				tasks.add(task);

			}
		}
		return tasks;
	}

	@Override
	public Task genTask(){
		//Reserve account and size for client
		if(accountStore.isNikeAccountAvailable()){
			Account a = accountStore.takeOutNikeAccount();
			String size = sizeList.takeSize();
			Task task = new Task(site, shoe, size, url, remainingPairs, a, accountStore);
			try {
				Proxy proxy = proxyStore.takeProxy(site, useTestedProxies, useUntestedProxies);
				task.setProxy(proxy);
			}
			//An exception here means that no viable proxy was found for this task
			catch (Exception e) {}
			
			return task;
		}else{
			return null;
		}

	}


	public void returnNikeAccount(Account a){
		accountStore.returnNikeAccount(a);
	}

}
