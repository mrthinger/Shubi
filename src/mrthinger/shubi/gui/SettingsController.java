package mrthinger.shubi.gui;

import java.util.concurrent.TimeUnit;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mrthinger.shubi.Info;

public class SettingsController {
	
	@FXML
	private TextField twoCaptchaTF;

	@FXML
	private TextField adcUKSiteKeyTF;

	@FXML
	private TextField tasksPerProxyTF;

	@FXML
	private TextField maxFailTF;

	@FXML
	private AnchorPane mainPane;

	@FXML
	private Button saveButton;
	
	@FXML
	private Button applyButton;

	@FXML
	private TextField adcUSSiteKeyTF;

	@FXML
	private TextField captchaTimeoutTF;
	
	private Stage stage;
	
	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setTitle("Settings");
		stage.setResizable(false);
		stage.setMinHeight(mainPane.getPrefHeight());
		stage.setMinWidth(mainPane.getPrefWidth());
		stage.setScene(new Scene(mainPane));
		
		//TODO: FILE SAVE SETTINGS

		
	}
	
	public void prompt(){
		twoCaptchaTF.setText(Info.TWOCAPTCHA_KEY);
		captchaTimeoutTF.setText(String.valueOf(TimeUnit.MILLISECONDS.toMinutes(Info.CAPTCHA_TIMEOUT)));
		adcUSSiteKeyTF.setText(Info.ADIDAS_US_SITEKEY);
		adcUKSiteKeyTF.setText(Info.ADIDAS_UK_SITEKEY);
		tasksPerProxyTF.setText(String.valueOf(Info.MAX_PROCESS));
		maxFailTF.setText(String.valueOf(Info.MAX_FAILURES));
		
		stage.showAndWait();
	}

	@FXML
	void applyOnClick(ActionEvent event) {
		applySettings();
	}
	
	@FXML
	void saveOnClick(ActionEvent event) {
		applySettings();
		stage.close();
	}

	private void applySettings() {
		Info.TWOCAPTCHA_KEY = twoCaptchaTF.getText();
		Info.CAPTCHA_TIMEOUT = TimeUnit.MINUTES.toMillis(Long.parseLong(captchaTimeoutTF.getText()));
		Info.ADIDAS_US_SITEKEY = adcUSSiteKeyTF.getText();
		Info.ADIDAS_UK_SITEKEY = adcUKSiteKeyTF.getText();
		Info.MAX_PROCESS = Integer.parseInt(tasksPerProxyTF.getText());
		Info.MAX_FAILURES = Integer.parseInt(maxFailTF.getText());
	}
}
