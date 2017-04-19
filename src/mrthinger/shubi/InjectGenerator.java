package mrthinger.shubi;

import java.util.HashMap;
import java.util.Map.Entry;

import javafx.collections.ObservableList;
import mrthinger.shubi.captcha.PooledCaptchaBank;
import mrthinger.shubi.task.Task;
import mrthinger.shubi.type.Backdoor;
import mrthinger.shubi.type.SizeList;

public class InjectGenerator {

	private PooledCaptchaBank cPool;
	private ObservableList<Backdoor> backdoors;
	
	public InjectGenerator(PooledCaptchaBank cPool) {
		this.cPool = cPool;
	}

	public PooledCaptchaBank getcPool() {
		return cPool;
	}

	public void setcPool(PooledCaptchaBank cPool) {
		this.cPool = cPool;
	}

	public ObservableList<Backdoor> getBackdoors() {
		return backdoors;
	}

	public void setBackdoors(ObservableList<Backdoor> backdoors) {
		this.backdoors = backdoors;
	}
	/**
	 * 
	 * @param task to turn into backdoor
	 * @return null if no backdoor was found for task or js executable string
	 */
	public String makeInjectableJs(Task task){
		
		Backdoor selectedBd = null;
		String size = "12";
	
		for(Backdoor b : backdoors){
			if(task.getShoe().equals(b.getBoundShoeName()) && task.getSite().equals(SiteKey.getByName(b.getSite()))){
				selectedBd = b;
				break;
			}
		}	
		
		if(selectedBd == null){
			return null;
		}
	
		Backdoor bd = new Backdoor();
		
		bd.setName(selectedBd.getName());
		bd.setUrl(selectedBd.getUrl());
		bd.setPostParams(new HashMap<>(selectedBd.getPostParams()));
		bd.setExtraInfo(new HashMap<>(selectedBd.getExtraInfo()));
		
		String captchaResponse = null;
		
		if(bd.needsCaptcha()){
			captchaResponse = cPool.waitForResponse(bd.getSiteKey());
			if(captchaResponse == null){
				return null;
			}
		}
		
		for(Entry<String, String> param : bd.getPostParams().entrySet()){
			if(param.getValue().contains(BackdoorConstants.ADIDAS_SIZE)){
				String newValue = param.getValue();
				String adidasSizeCode = SizeList.toAdidasSizeCode(size);
				newValue = newValue.replace(BackdoorConstants.ADIDAS_SIZE, adidasSizeCode);
				bd.getPostParams().replace(param.getKey(), newValue);
			}
			if(param.getValue().contains(BackdoorConstants.CAPTCHA)){
				String newValue = param.getValue();
				newValue = newValue.replace(BackdoorConstants.CAPTCHA, captchaResponse);
				bd.getPostParams().replace(param.getKey(), newValue);
			}
		}

		return bd.toJSInject();

	}
	
	public boolean matchingBackdoorFound(Task task){
		for(Backdoor b : backdoors){
			if(task.getShoe().equals(b.getBoundShoeName()) && task.getSite().equals(SiteKey.getByName(b.getSite()))){
				return true;
			}
		}	
		return false;
	}

}
