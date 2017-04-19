package mrthinger.shubi.gui;


import java.util.List;

import javax.ws.rs.ProcessingException;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mrthinger.shubi.Info;
import mrthinger.shubi.ShubiMain;
import mrthinger.shubi.type.Backdoor;

public class BackdoorPopup {

	private Button getBackdoorsButton;
	private ListView<String> backdoorList;
	private Button submitButton;

	private Stage backdoorPopup;

	private ObservableList<String> backdoorNames;
	private List<Backdoor> backdoors;

	private Backdoor pickedBackdoor;

	private volatile boolean polling;

	public BackdoorPopup() {
		polling = false;

		backdoorPopup = new Stage();
		backdoorPopup.initModality(Modality.APPLICATION_MODAL);
		backdoorPopup.setTitle("Backdoor");
		backdoorPopup.setResizable(false);

		getBackdoorsButton = new Button("Refresh");
		getBackdoorsButton.setOnAction((event) -> pollServer());

		backdoorNames = FXCollections.observableArrayList();

		backdoorList = new ListView<>();
		backdoorList.setItems(backdoorNames);

		submitButton = new Button("Submit");
		submitButton.setOnAction((event) -> {

			if(!backdoorList.getSelectionModel().isEmpty()){
				for(Backdoor bd : backdoors){
					if(bd.getName().equals(backdoorList.getSelectionModel().getSelectedItem())){
						pickedBackdoor = bd;
						break;
					}
				}
			}
			backdoorPopup.close();	
		});

		VBox layout = new VBox(4);
		layout.setPadding(new Insets(8));
		layout.setMinWidth(600);
		layout.getChildren().addAll(getBackdoorsButton, backdoorList, submitButton);

		backdoorPopup.setScene(new Scene(layout));

	}

	public Backdoor getPickedBackdoor(){
		return pickedBackdoor;
	}

	public void prompt(){
		backdoorPopup.showAndWait();
	}

	public void pollServer(){
		if(!polling){
			Thread t = new Thread(() -> {
				polling = true;
				try{
					backdoors = ShubiMain.ws.getBackdoors();

					Platform.runLater(() -> {
						for(int i = backdoorNames.size() - 1; i >= 0; i--){
							backdoorNames.remove(i);
						}
						for(Backdoor bd : backdoors){
							backdoorNames.add(bd.getName());
						}
						backdoorList.refresh();
						if(Info.WARDEN_VERBOSE)System.out.println(backdoorNames);
					});
				}catch(ProcessingException e){
					e.printStackTrace();
					System.out.println("Web API down!");
				}
				polling = false;
			});
			t.start();
		}
	}

}
