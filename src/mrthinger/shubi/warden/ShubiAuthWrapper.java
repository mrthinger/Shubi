package mrthinger.shubi.warden;

import java.util.List;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import mrthinger.shubi.Info;
import mrthinger.shubi.type.Backdoor;

public class ShubiAuthWrapper {

	private Client client;
	private WebTarget base;
	private WebTarget loginPath;
	private WebTarget backdoorPath;
	
	public ShubiAuthWrapper(){
		client = ClientBuilder.newClient();
		base = client.target("http://" + Info.SERVER_IP + ":" + Info.SERVER_PORT + "/shubiauth");
		loginPath = base.path("key");
		backdoorPath = base.path("backdoor");
	}
	
	public String login(String loginKey) throws ProcessingException{
		return loginPath.queryParam("authKey", loginKey).request(MediaType.TEXT_PLAIN).get().readEntity(String.class);
	}
	
	public String checkIn(String sessionKey) throws ProcessingException{
		return loginPath.queryParam("sessionKey", sessionKey).request(MediaType.TEXT_PLAIN).put(Entity.entity("", MediaType.TEXT_PLAIN_TYPE)).readEntity(String.class);	
	}
	
	public boolean logout(String sessionKey) throws ProcessingException{
		return loginPath.queryParam("sessionKey", sessionKey).request(MediaType.TEXT_PLAIN).delete().readEntity(Boolean.class);	
	}
	
	public List<Backdoor> getBackdoors(String sessionKey) throws ProcessingException{
		return backdoorPath.queryParam("sessionKey", sessionKey).request(MediaType.APPLICATION_XML).get().readEntity(new GenericType<List<Backdoor>>() {});
	}
	
}
