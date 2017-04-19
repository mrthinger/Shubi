package mrthinger.shubi.type;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

public class SizeList {

	private ArrayList<String> sizeList;
	private String min;
	private String max;
	private boolean hasRange;
	private int i;

	public SizeList(ArrayList<String> sizeList, String min, String max){
		this(sizeList);

		if(min == null || min.equals("")){
			ArrayList<Float> s = new ArrayList<>();
			for(String size : sizeList){
				s.add(Float.valueOf(size));
			}
			Collections.sort(s);
			this.min = s.get(0).toString();
		}else{
			this.min = min;
		}

		if(max == null || max.equals("")){
			ArrayList<Float> s = new ArrayList<>();
			for(String size : sizeList){
				s.add(Float.valueOf(size));
			}
			Collections.sort(s);
			this.max = s.get(s.size()-1).toString();
		}else{
			this.max = max;
		}

		hasRange = true;
	}

	public SizeList(ArrayList<String> sizeList){
		this.sizeList = sizeList;
		this.i = 0;
		hasRange = false;
	}

	public SizeList (SizeList sizeListToCopy){
		this.sizeList = new ArrayList<String>(sizeListToCopy.getSizeList());
		this.i = 0;
		if(sizeListToCopy.hasRange){
			hasRange = true;
			min = sizeListToCopy.getMin();
			max = sizeListToCopy.getMax();
		}else{
			hasRange = false;
		}
	}

	public String takeSize(){
		if(i > sizeList.size() - 1){
			i = 0;
		}
		String size = sizeList.get(i);
		i++;
		return size;
	}

	public boolean hasSize(){
		return sizeList.size() > 0;
	}

	public ArrayList<String> getSizeList(){
		return sizeList;
	}

	public String getMin(){
		return min;
	}

	public String getMax(){
		return max;
	}

	public boolean hasRange(){
		return hasRange;
	}

	public static String toAdidasSizeCode(String size){
		float sizeFloat = Float.parseFloat(size);
		sizeFloat *= 20;
		sizeFloat += 450;
		return String.valueOf((int)sizeFloat);
	}

	public static String toFootsiteSize(String size){
		float sizeFloat = Float.parseFloat(size);
		DecimalFormat footsiteFormat = new DecimalFormat("00.0");
		return footsiteFormat.format(sizeFloat);
	}

	public static ArrayList<String> allPossibleSizesList(){
		ArrayList<String> all = new ArrayList<>();
		all.add("7");
		all.add("7.5");
		all.add("8");
		all.add("8.5");
		all.add("9");
		all.add("9.5");
		all.add("10");
		all.add("10.5");
		all.add("11");
		all.add("11.5");
		all.add("12");
		all.add("12.5");
		all.add("13");
		all.add("13.5");
		all.add("14");
		all.add("14.5");
		all.add("15");
		return all;
	}

	@Override
	public String toString(){
		StringBuilder sizesText = new StringBuilder();

		if(hasRange){
			sizesText.append("MIN" + this.min + "&");
			sizesText.append("MAX" + this.max + "&");
		}

		for(String size : sizeList){
			sizesText.append(size + "&");
		}

		if(sizesText.length()>1){
			sizesText.deleteCharAt(sizesText.length()-1);
		}
		return sizesText.toString();
	}

}
