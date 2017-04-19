package mrthinger.shubi.gui;

import java.util.ArrayList;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mrthinger.shubi.type.SizeList;

public class SizeSelectionPopup {

	
	private SizeList sizeList;
	
	private ArrayList<String> sizes;
	private String min;
	private String max;

	private CheckBox s70;
	private CheckBox s75;
	private CheckBox s80;
	private CheckBox s85;
	private CheckBox s90;
	private CheckBox s95;
	private CheckBox s100;
	private CheckBox s105;
	private CheckBox s110;
	private CheckBox s115;
	private CheckBox s120;
	private CheckBox s125;
	private CheckBox s130;
	private CheckBox s135;
	private CheckBox s140;
	private CheckBox s145;
	private CheckBox s150;
	
	private TextField minTf;
	private TextField maxTf;
	
	private Stage popup;
	
	public SizeSelectionPopup() {
		popup = new Stage();
		popup.initModality(Modality.APPLICATION_MODAL);
		popup.setResizable(false);

		s70 = new CheckBox("7.0");
		s75 = new CheckBox("7.5");
		s80 = new CheckBox("8.0");
		s85 = new CheckBox("8.5");
		s90 = new CheckBox("9.0");
		s95 = new CheckBox("9.5");
		s100 = new CheckBox("10.0");
		s105 = new CheckBox("10.5");
		s110 = new CheckBox("11.0");
		s115 = new CheckBox("11.5");
		s120 = new CheckBox("12.0");
		s125 = new CheckBox("12.5");
		s130 = new CheckBox("13.0");
		s135 = new CheckBox("13.5");
		s140 = new CheckBox("14.0");
		s145 = new CheckBox("14.5");
		s150 = new CheckBox("15.0");
		
		minTf = new TextField();
		minTf.setPromptText("MIN");
		minTf.setPrefWidth(40);
		maxTf = new TextField();
		maxTf.setPromptText("MAX");
		maxTf.setPrefWidth(40);

		Button submitButton = new Button("Submit");

		submitButton.setOnAction(e -> {
			String maxText = maxTf.getText();
			String minText = minTf.getText();
			if((maxText != null && !maxText.equals("")) || (minText != null && !minText.equals(""))){
				sizeList = new SizeList(generateSizeArray(), minText, maxText);
			}else{
				sizeList = new SizeList(generateSizeArray());
			}
			
			
			popup.close();
		});

		VBox layout = new VBox(4);
		layout.setPadding(new Insets(8));
		
		HBox rangeBox = new HBox(4);
		rangeBox.getChildren().addAll(minTf, maxTf);

		layout.getChildren().addAll(s70, s75, s80, s85, s90, s95,
				s100, s105, s110, s115, s120, s125, s130, s135, s140, s145, s150, rangeBox, submitButton);

		popup.setScene(new Scene(layout));

	}

	public SizeList promptSizeSelection(){
		popup.showAndWait();
		return sizeList;
	}

	private ArrayList<String> generateSizeArray() {
		sizes = new ArrayList<>();
		
		if(s70.isSelected()){
			sizes.add("7");
		}
		if(s75.isSelected()){
			sizes.add("7.5");
		}
		if(s80.isSelected()){
			sizes.add("8");
		}
		if(s85.isSelected()){
			sizes.add("8.5");
		}
		if(s90.isSelected()){
			sizes.add("9");
		}
		if(s95.isSelected()){
			sizes.add("9.5");
		}
		if(s100.isSelected()){
			sizes.add("10");
		}
		if(s105.isSelected()){
			sizes.add("10.5");
		}
		if(s110.isSelected()){
			sizes.add("11");
		}
		if(s115.isSelected()){
			sizes.add("11.5");
		}
		if(s120.isSelected()){
			sizes.add("12");
		}
		if(s125.isSelected()){
			sizes.add("12.5");
		}
		if(s130.isSelected()){
			sizes.add("13");
		}
		if(s135.isSelected()){
			sizes.add("13.5");
		}
		if(s140.isSelected()){
			sizes.add("14");
		}
		if(s145.isSelected()){
			sizes.add("14.5");
		}
		if(s150.isSelected()){
			sizes.add("15");
		}
		
		return sizes;
	}
	
	public SizeList getSizeList(){
		return sizeList;
	}

}
