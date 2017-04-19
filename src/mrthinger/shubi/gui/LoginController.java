package mrthinger.shubi.gui;

import javax.ws.rs.ProcessingException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import mrthinger.shubi.Info;
import mrthinger.shubi.ShubiMain;
import mrthinger.shubi.warden.WardenService;

public class LoginController {

	@FXML
	private AnchorPane mainPane;

	@FXML
	private Button loginButton;

	@FXML
	private TextField keyTextField;

	@FXML
	private Label statusLabel;

	private Stage primaryStage;

	private WindowMainController shubiController;
	private String sessionKey;

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		keyTextField.setText("k0");
	}
	
	@FXML
	void onLoginClicked(ActionEvent event) {

		submit();

	}

	@FXML
	void onKeyFieldKeyPressed(KeyEvent event) {
		if(event.getCode().equals(KeyCode.ENTER)){
			submit();
		}
	}

	public void setShubiController(WindowMainController shubiController){
		this.shubiController = shubiController;
	}

	public void setPrimaryStage(Stage stage){
		primaryStage = stage;
		primaryStage.setMinHeight(mainPane.getMinHeight());
		primaryStage.setMinWidth(mainPane.getMinWidth());

		primaryStage.setScene(new Scene(mainPane));
		primaryStage.setTitle(Info.TITLE);
		primaryStage.setResizable(false);
	}

	private void submit() {
		String key = keyTextField.getText();
		if(key != null && !key.equals("")){
			statusLabel.setText("Status: Logging in...");
			String response = "";

			try{
				response = ShubiMain.shubiAuthService.login(key);
				System.out.println(response);

				if(response.contains("SUCCESS")){
					sessionKey = response.split(":")[1];
					statusLabel.setText("Status: " + "Login success!");
					ShubiMain.ws = new WardenService(key, sessionKey, primaryStage);
					shubiController.setPrimaryStage(primaryStage);
				}
				else if(response.contains("INVALID")){
					statusLabel.setText("Status: " + "Invalid key!");
				}
				else if(response.contains("INUSE")){
					statusLabel.setText("Status: " + "Key in use!");
				}

			}catch(ProcessingException e){
				statusLabel.setText("Status: Web API down!");
			}

		}
	}

}

