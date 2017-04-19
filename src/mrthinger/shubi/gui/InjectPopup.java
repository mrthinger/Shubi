package mrthinger.shubi.gui;


import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mrthinger.shubi.ShubiMain;
import mrthinger.shubi.task.Task;
import mrthinger.shubi.type.Backdoor;

public class InjectPopup {

	private ChoiceBox<String> sizeBox;
	private Button submitButton;
	private Button cancelButton;

	private Stage injectPopup;

	private String backdoor;

	private volatile boolean open;
	
	private volatile Task t;

	public InjectPopup() {
		backdoor = null;
		open = false;
		injectPopup = new Stage();
		injectPopup.initModality(Modality.APPLICATION_MODAL);
		injectPopup.setTitle("Injection");
		injectPopup.setResizable(false);


		injectPopup.setOnCloseRequest((e) -> {
			onCloseWindow(false);
		});
		
		sizeBox = new ChoiceBox<>();

		for(int i = 0; i < 18; i++){
			float s = ((float) i / 2F) + 7F;
			sizeBox.getItems().add(String.valueOf(s));
		}

		submitButton = new Button("Inject");
		submitButton.setOnAction((event) -> {
			if(sizeBox.getValue() == null){
				System.out.println("Pick a size!");
				return;
			}
			
			t.setSize(sizeBox.getValue());
			backdoor = ShubiMain.iGen.makeInjectableJs(t);
			if(backdoor == null){
				onCloseWindow(false);
			}else{
				onCloseWindow(true);
			}
			
			
			
			injectPopup.close();	
		});

		cancelButton = new Button("Cancel");
		cancelButton.setOnAction((event) -> {
			onCloseWindow(false);
			injectPopup.close();	
		});


		HBox box = new HBox(4);
		box.getChildren().addAll(submitButton, cancelButton);
		
		VBox layout = new VBox(4);
		layout.setPadding(new Insets(8));
		layout.setMinWidth(200);
		layout.getChildren().addAll(sizeBox, box);

		injectPopup.setScene(new Scene(layout));

	}

	//passing in true will set selected profile to selection while false will set it to a profile with null contents
	private void onCloseWindow(boolean submitProfile) {
		if(!submitProfile){
			backdoor = "n";
		}
		open = false;
	}

	public String getInjectableJS(){
		return backdoor;
	}
	
	//returns if call resulted in popup being shown
	public boolean prompt(Task t){
		
		if(!ShubiMain.iGen.matchingBackdoorFound(t)){
			System.out.println("No backdoor has been configured for this site + shoe name");
			return false;
		}
		
		if(!open){
			this.t = t;
			backdoor = null;
			open = true;
			Platform.runLater(() -> injectPopup.showAndWait());
			return true;
		}else{
			return false;
		}

	}
	

}
