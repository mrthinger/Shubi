package mrthinger.shubi.type;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mrthinger.shubi.FileFormatException;
import mrthinger.shubi.Info;
import mrthinger.shubi.SiteKey;

public class ProxyStore {


	public static final String BASE_URL = "https://api.myprivateproxy.net/v1/fetchProxies/plain/full/";
	private ObservableList<Proxy> proxies;

	public ProxyStore() {
		this.proxies = FXCollections.observableArrayList();
	}

	public ProxyStore(ObservableList<Proxy> proxies){
		this.proxies = proxies;
	}

	public ProxyStore(String mppApiKey) throws IOException, FileFormatException{
		this();

		URL mppUrl = new URL(BASE_URL+mppApiKey);
		URLConnection mppConnection = mppUrl.openConnection();
		Scanner s = new Scanner(mppConnection.getInputStream());

		while(s.hasNextLine()){
			String line = s.nextLine();
			if(!line.equals("")){
				String[] pArgs = line.split(":");

				try{
					proxies.add(new Proxy(pArgs[0], pArgs[1], pArgs[2], pArgs[3]));
				}catch(ArrayIndexOutOfBoundsException e){
					s.close();
					throw new FileFormatException();
				}
			}
		}

		s.close();

		if(proxies.size() == 0){
			throw new FileFormatException();
		}

		if(Info.DEBUG)
			for(Proxy p : proxies){
				System.out.println(p);
			}

	}

	public ProxyStore(File proxyFile) throws FileNotFoundException, FileFormatException{
		this();
		Scanner s = new Scanner(proxyFile);

		while(s.hasNextLine()){
			String line = s.nextLine();
			if(!line.equals("")){
				String[] pArgs = line.split(":");

				try{
					proxies.add(new Proxy(pArgs[0], pArgs[1], pArgs[2], pArgs[3]));
				}catch(ArrayIndexOutOfBoundsException e){
					throw new FileFormatException();
				}
			}
		}

		s.close();

		if(proxies.size() == 0){
			throw new FileFormatException();
		}

		if(Info.DEBUG)
			for(Proxy p : proxies){
				System.out.println(p);
			}
	}

	public void addProxies(String mppApiKey) throws IOException, FileFormatException{
		List<Proxy> newProxies = new ArrayList<>();

		URL mppUrl = new URL(BASE_URL+mppApiKey);
		URLConnection mppConnection = mppUrl.openConnection();
		Scanner s = new Scanner(mppConnection.getInputStream());

		while(s.hasNextLine()){
			String line = s.nextLine();
			if(!line.equals("")){
				String[] pArgs = line.split(":");

				try{
					newProxies.add(new Proxy(pArgs[0], pArgs[1], pArgs[2], pArgs[3]));
				}catch(ArrayIndexOutOfBoundsException e){
					s.close();
					throw new FileFormatException();
				}
			}
		}

		s.close();

		if(newProxies.size() == 0){
			System.out.println("No proxies found.");
		}

		addProxies(newProxies);
	}

	public void addProxies(File proxyFile) throws IOException, FileFormatException{
		List<Proxy> newProxies = new ArrayList<>();

		Scanner s = new Scanner(proxyFile);

		while(s.hasNextLine()){
			String line = s.nextLine();
			if(!line.equals("")){
				String[] pArgs = line.split(":");

				try{
					newProxies.add(new Proxy(pArgs[0], pArgs[1], pArgs[2], pArgs[3]));
				}catch(ArrayIndexOutOfBoundsException e){
					throw new FileFormatException();
				}
			}
		}

		s.close();

		if(newProxies.size() == 0){
			System.out.println("No proxies found.");
		}

		addProxies(newProxies);
	}

	public void addProxies(List<Proxy> proxyList){
		boolean add = true;
		for(Proxy p : proxyList){
			add = true;
			for(Proxy op : proxies){
				if(p.getIp().equals(op.getIp()) && p.getPort().equals(op.getPort())){
					add = false;
				}
			}
			if(add){
				proxies.add(p);
			}
		}
	}


	public void addAndUpdateProxies(List<Proxy> proxyList){
		boolean add = true;
		for(Proxy p : proxyList){
			add = true;
			for(Proxy op : proxies){
				if(p.getIp().equals(op.getIp()) && p.getPort().equals(op.getPort())){
					op.setTested(p.isTested());
					op.setResponseTimes(p.getResponseTimeMap());
					op.setUsage(p.getUsageMap());
					add = false;
				}
			}
			if(add){
				proxies.add(p);
			}
		}
	}

	public ObservableList<Proxy> deepCopyProxyList(){
		ObservableList<Proxy> newProxyList = FXCollections.observableArrayList();

		for(Proxy p : proxies){
			newProxyList.add(new Proxy(p));
		}

		return newProxyList;
	}

	/**
	 * 
	 * @param site that proxy needs to connect to
	 * @return a proxy
	 * @throws Exception no proxies left
	 */
	public Proxy takeProxy(SiteKey site) throws Exception{
		for(Proxy p : proxies){
			if((p.getNumTasksUsing() < Info.MAX_PROCESS) && p.canConnectTo(site)){
				p.addTaskUsing();
				if(Info.DEBUG)System.out.println("Took: " + p);

				return p;
			}
		}
		throw new Exception("Out of proxies!");
	}

	public Proxy takeProxy(SiteKey site, boolean useTestedProxies, boolean useUntestedProxies) throws Exception {
		for(Proxy p : proxies){
			if((p.getNumTasksUsing() < Info.MAX_PROCESS) && p.canConnectTo(site)){
				if(useTestedProxies && useUntestedProxies){
					p.addTaskUsing();
					if(Info.DEBUG)System.out.println("Took: " + p);

					return p;
				}else if(useTestedProxies && !useUntestedProxies){
					if(p.isTested()){
						p.addTaskUsing();
						if(Info.DEBUG)System.out.println("Took: " + p);

						return p;
					}
				}
				else if(!useTestedProxies && useUntestedProxies){
					if(!p.isTested()){
						p.addTaskUsing();
						if(Info.DEBUG)System.out.println("Took: " + p);

						return p;
					}
				}
				
			}

		}
		throw new Exception("Out of proxies!");
	}

	public int getNumProxies(){
		return proxies.size();
	}

	public void removeProxy(Proxy p){
		proxies.remove(p);
	}

	public ObservableList<Proxy> getProxies(){
		return proxies;
	}

}
