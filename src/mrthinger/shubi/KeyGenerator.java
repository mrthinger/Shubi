package mrthinger.shubi;

import java.util.ArrayList;

public class KeyGenerator {

	private ArrayList<String> keys;

	public KeyGenerator(){
		keys = new ArrayList<>();
	}

	public String genUniqueKey(int length){
		String uniqueKey = genKey(length);
		
		//key wont escape loop till its unique
		boolean unique = false;
		while(!unique){
			unique = true;
			for(String key : keys){
				if(uniqueKey.equals(key)){
					uniqueKey = genKey(length);
					unique = false;
				}
			}
		}
		keys.add(uniqueKey);
		return uniqueKey;
	}

	private String genKey(int length){
		String key = "";

		for(int i = 0; i < length; i++){
			key += genChar();
		}

		return key;
	}

	private char genChar(){
		//65-90 Uppercase letters
		char upper = (char) ((int)(Math.random() * 25) + 65);
		//97-122 lowercase letters
		char lower = (char) ((int)(Math.random() * 25) + 97);


		//pick if lower or upper 50/50 chance
		char c = (Math.random() > .5)? upper : lower;

		return c;
	}

}
