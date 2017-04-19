package mrthinger.shubi.gui;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mrthinger.shubi.FileFormatException;
import mrthinger.shubi.type.ProxyStore;

public class ProxyConfigPopup {

	private ProxyStore proxyStore;
	
	private TextField mppApiKeyTF;
	private Button mppLoadButton;
	private TextField filePathTF;
	private Button pickFileButton;
	
	private Stage proxyPopup;
	
	public ProxyConfigPopup() {
		
		proxyPopup = new Stage();
		proxyPopup.initModality(Modality.APPLICATION_MODAL);
		proxyPopup.setTitle("MPP Entry");
		proxyPopup.setResizable(false);
		
		mppApiKeyTF = new TextField();
		mppApiKeyTF.setPrefWidth(600);
		
		mppLoadButton = new Button("Load");
		mppLoadButton.setMinWidth(100);
		mppLoadButton.setOnAction(e -> {
			try {
				proxyStore = new ProxyStore(mppApiKeyTF.getText());
				System.out.println("Set proxy file.");
				proxyPopup.close();
			} catch (IOException e1) {
				System.out.println("Couldn't connect to MPP service.");
			} catch (FileFormatException e1){
				System.out.println("Invalid proxy file format. If you're seing this the dev screwed up.");
			}
		});
		
		HBox mppBox = new HBox(4);
		mppBox.getChildren().addAll(mppApiKeyTF, mppLoadButton);
		
		
		filePathTF = new TextField();
		filePathTF.setEditable(false);
		filePathTF.setPrefWidth(600);
		
		pickFileButton = new Button("Load File");
		pickFileButton.setMinWidth(100);
		pickFileButton.setOnAction(e -> {
			FileChooser fileChooser = new FileChooser();
			File proxyFile = fileChooser.showOpenDialog(proxyPopup);
			if(proxyFile != null){
				try {
					proxyStore = new ProxyStore(proxyFile);
					filePathTF.setText(proxyFile.getAbsolutePath());
					System.out.println("Set proxy file.");
					proxyPopup.close();
				} catch(FileFormatException e1) {
					System.out.println("Invald proxy file format. Please use format ip:port:user:pass on each line.");
				} catch(FileNotFoundException e2){
					System.out.println("File not found.");
				}
			}
		});
		
		HBox fileBox = new HBox(4);
		fileBox.getChildren().addAll(filePathTF, pickFileButton);
		
		Label orLabel = new Label("OR");
		
		Label mmpLabel = new Label("My Private Proxy API Key:");
		Label fileLabel = new Label("Proxy File:");
		
		VBox layout = new VBox(4);
		layout.setPadding(new Insets(8));
		layout.setMinWidth(600);
		//layout.getChildren().addAll(fileLabel, fileBox, orLabel, mmpLabel, mppBox);
		layout.getChildren().addAll(mmpLabel, mppBox);
		
		proxyPopup.setScene(new Scene(layout));

	}
	
	public void prompt(){
		proxyPopup.showAndWait();
	}
	
	public ProxyStore getProxyStore(){
		return proxyStore;
	}

}
