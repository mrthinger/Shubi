package mrthinger.shubi.gui;


import java.util.List;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mrthinger.shubi.type.AdidasGuestCheckout;

public class PayPopup {

	private ListView<AdidasGuestCheckout> profileListView;
	private Button submitButton;
	private Button cancelButton;

	private Stage payPopup;

	private ObservableList<AdidasGuestCheckout> checkoutProfiles;

	private AdidasGuestCheckout pickedProfile;

	private volatile boolean open;

	public PayPopup() {
		open = false;
		payPopup = new Stage();
		payPopup.initModality(Modality.APPLICATION_MODAL);
		payPopup.setTitle("Checkout");
		payPopup.setResizable(false);

		payPopup.setOnCloseRequest((e) -> {
			onCloseWindow(false);
		});

		profileListView = new ListView<>();

		submitButton = new Button("Submit");
		submitButton.setOnAction((event) -> {
			onCloseWindow(true);
			payPopup.close();	
		});

		cancelButton = new Button("Cancel");
		cancelButton.setOnAction((event) -> {
			onCloseWindow(false);
			payPopup.close();	
		});

		HBox box = new HBox(4);
		box.getChildren().addAll(submitButton, cancelButton);

		VBox layout = new VBox(4);
		layout.setPadding(new Insets(8));
		layout.setMinWidth(600);
		layout.getChildren().addAll(profileListView, box);

		payPopup.setScene(new Scene(layout));

	}

	//passing in true will set selected profile to selection while false will set it to a profile with null contents
	private void onCloseWindow(boolean submitProfile) {
		if(!profileListView.getSelectionModel().isEmpty() && submitProfile){
			pickedProfile = profileListView.getSelectionModel().getSelectedItem();
		}else{
			pickedProfile = new AdidasGuestCheckout(null, null, null);
		}
		open = false;
		profileListView.getSelectionModel().clearSelection();
	}

	public AdidasGuestCheckout getPickedAdidasGuestCheckout(){
		return pickedProfile;
	}

	public void setAdidasGuestCheckouts(List<AdidasGuestCheckout> adidasCheckouts){
		this.checkoutProfiles = FXCollections.observableArrayList(adidasCheckouts);
		profileListView.setItems(checkoutProfiles);
	}

	//returns if call resulted in popup being shown
	public boolean prompt(){
		if(!open){
			pickedProfile = null;
			open = true;
			Platform.runLater(() -> payPopup.showAndWait());
			return true;
		}else{
			return false;
		}

	}

}
