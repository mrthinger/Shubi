package mrthinger.shubi.proxyload;

import java.io.IOException;
import java.util.List;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;

import mrthinger.shubi.SiteKey;
import mrthinger.shubi.type.Proxy;

public class UrlTest implements Runnable{

	private Proxy p;
	private List<String> urls;
	private volatile boolean running;
	private volatile int complete;
	private volatile int total;
	
	public UrlTest(Proxy p, List<String> urlsToTest){
		this.p = p;
		this.urls = urlsToTest;
		
		running = true;
		complete = 0;
		total = urls.size();
	}

	@Override
	public void run() {

		for(String url : urls){
			//let them know we started the test
			for(SiteKey site : SiteKey.getByUrl(url)){
				p.updateResponseTimeMap(site, 2L);
			}
			
			//This could be made 1nce but just so theirs no interference between tests
			//I'll create a new browser for every test
			WebClient d = new WebClient(BrowserVersion.CHROME);
			ProxyConfig proxy = new ProxyConfig(p.getIp(), Integer.parseInt(p.getPort()));
			d.getOptions().setProxyConfig(proxy);
			DefaultCredentialsProvider provider = new DefaultCredentialsProvider();
	        provider.addCredentials(p.getUser(), p.getPass());
	        d.setCredentialsProvider(provider);
			d.getOptions().setTimeout(30*1000);
			d.getOptions().setJavaScriptEnabled(false);
			d.getOptions().setCssEnabled(false);

			//Start time
			long sT = System.currentTimeMillis();


			try {
				d.getPage(url);
			} catch (FailingHttpStatusCodeException | IOException e) {
				d.close();
				for(SiteKey site : SiteKey.getByUrl(url)){
					p.updateUsageMap(site, false);
					p.updateResponseTimeMap(site, -1L);
					p.setTested(true);
					
				}
				complete++;
				continue;
			}

			//end time
			long eT = System.currentTimeMillis();

			for(SiteKey site : SiteKey.getByUrl(url)){
				p.updateUsageMap(site, true);
				p.updateResponseTimeMap(site, eT - sT);
				p.setTested(true);
			}

			d.close();
			complete++;
		}

		running = false;
		
	}

	public boolean isRunning() {
		return running;
	}

	public int getComplete() {
		return complete;
	}

	public int getTotal() {
		return total;
	}

}
