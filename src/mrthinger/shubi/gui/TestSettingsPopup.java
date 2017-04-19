package mrthinger.shubi.gui;

import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mrthinger.shubi.SiteKey;

public class TestSettingsPopup {

	private Stage popup;
	private List<CheckBox> siteOptions;
	
	public TestSettingsPopup(){
		popup = new Stage();
		popup.initModality(Modality.APPLICATION_MODAL);
		popup.setTitle("Test Settings");
		popup.setResizable(false);
		
		//Scene
		VBox choices = new VBox(4);
		choices.setPadding(new Insets(8));
		
		siteOptions = new ArrayList<>();
		
		List<SiteKey> sitesToAdd = new ArrayList<>();
		
		for(SiteKey site : SiteKey.values()){
			boolean alreadyAdded = false;
			for(SiteKey addedSite : sitesToAdd){
				if(site.getHomeUrl().equals(addedSite.getHomeUrl())){
					alreadyAdded = true;
				}
			}
			if(!alreadyAdded){
				sitesToAdd.add(site);
			}
		}
		
		for(SiteKey site : sitesToAdd){
			CheckBox siteBox = new CheckBox(site.getName());
			siteOptions.add(siteBox);
		}
		
		
		
		Button submit = new Button("Save");
		submit.setOnAction(e -> popup.close());
		
		choices.getChildren().addAll(siteOptions);
		choices.getChildren().add(submit);
		
		popup.setScene(new Scene(choices));
	}
	
	public void prompt(){
		popup.showAndWait();
	}

	public List<SiteKey> getSelectedSiteKeys(){
		List<SiteKey> sites = new ArrayList<>();
		
		for(CheckBox checkBox : siteOptions){
			if(checkBox.isSelected()){
				for(SiteKey site : SiteKey.values()){
					if(site.getName().equals(checkBox.getText())){
						sites.add(site);
						break;
					}
				}
			}
		}
		
		return sites;
	}
	
	
	public List<String> getUrlsToTest(){
		List<String> urls = new ArrayList<>();
		
		for(CheckBox checkBox : siteOptions){
			if(checkBox.isSelected()){
				for(SiteKey site : SiteKey.values()){
					if(site.getName().equals(checkBox.getText())){
						urls.add(site.getHomeUrl());
						break;
					}
				}
			}
		}
		
		return urls;
	}
	
}
