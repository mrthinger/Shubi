package mrthinger.shubi.gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import mrthinger.shubi.task.Task;

public class MetaController {

	@FXML
	private AnchorPane mainPane;

	@FXML
	private Label linkLabel;

	@FXML
	private Label sizeLabel;

	@FXML
	private Label proxyLabel;

	@FXML
	private Button closeButton;

	@FXML
	private Label shoeLabel;

	@FXML
	private Label remainingLabel;

	@FXML
	private Label accountLabel;

	@FXML
	private Label statusLabel;

	@FXML
	private Label siteLabel;

	@FXML
	private Button selectButton;
	
	@FXML
	private Label selectStatusLabel;

	private Stage stage;

	//Task to be tracked
	private volatile Task task;

	private boolean wantsNewTask;

	private volatile boolean running;

	public boolean wantsNewTask(){
		return wantsNewTask;
	}
	
	public boolean isRunning(){
		return running;
	}

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		stage = new Stage();
		stage.setResizable(false);
		stage.setScene(new Scene(mainPane));
		wantsNewTask = false;
		running = true;

		stage.setOnCloseRequest((event) -> {
			running = false;
		});

		//update task details
		Thread t = new Thread(()->{
			while(running){
				if(task != null){
					Platform.runLater(()->{
						refreshTask();
					});
				}

				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		t.setDaemon(true);
		t.start();

		stage.show();
	}

	private void refreshTask() {
		//Shoe
		if(task.getShoe() != null){
			shoeLabel.setText(task.getShoe());
		}
		//Site
		if(task.getSite() != null){
			siteLabel.setText(task.getSite().toString());
		}
		//Proxy
		if(task.getProxy() != null){
			proxyLabel.setText(task.getProxy().toString());
		}else{
			proxyLabel.setText("localhost");
		}

		//This can't return null
		remainingLabel.setText(String.valueOf(task.getRemainingPairs()));
		
		//Size
		if(task.getSize() != null){
			sizeLabel.setText(task.getSize());
		}
		
		//Username
		if(task.getUserpass() != null){
			accountLabel.setText(task.getUserpass().getUser());
		}
		
		//link
		if(task.getUrl() != null){
			linkLabel.setText(task.getUrl());
		}
		
		//Status Text
		if(task.getStatus() != null){
			statusLabel.setText(task.getStatus());
		}
	}

	public void setTask(Task task){
		this.task = task;
		this.wantsNewTask = false;
		selectStatusLabel.setVisible(false);
		refreshTask();
	}

	@FXML
	void selectOnClick(ActionEvent event) {
		wantsNewTask = true;
		selectStatusLabel.setVisible(true);
	}

	@FXML
	void closeOnClick(ActionEvent event) {
		running = false;
		stage.close();
	}
	
	public void shutOff(){
		closeOnClick(null);
	}

}
