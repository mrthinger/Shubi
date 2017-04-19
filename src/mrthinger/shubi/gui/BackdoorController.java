package mrthinger.shubi.gui;

import java.util.List;

import javax.ws.rs.ProcessingException;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mrthinger.shubi.InjectGenerator;
import mrthinger.shubi.ShubiMain;
import mrthinger.shubi.SiteKey;
import mrthinger.shubi.captcha.InjectCaptchaService;
import mrthinger.shubi.task.TaskMaster;
import mrthinger.shubi.type.Backdoor;

public class BackdoorController {

	@FXML
	private Label bankedLabel;

	@FXML
	private RadioButton autoRadio;

	@FXML
	private Button applyButton;

	@FXML
	private Button captchaStopButton;

	@FXML
	private Label siteValueLabel;

	@FXML
	private TextField constantTF;

	@FXML
	private VBox captchaSettingsBox;

	@FXML
	private ListView<Backdoor> bdListView;

	@FXML
	private RadioButton constantRadio;

	@FXML
	private Label numBankersLabel;

	@FXML
	private Button captchaStartButton;

	@FXML
	private Button inspectButton;

	@FXML
	private VBox bdInfoBox;

	@FXML
	private Button createButton;

	@FXML
	private Button updateButton;

	@FXML
	private Label siteKeyLabel;

	@FXML
	private Label bankEnabledLabel;

	@FXML
	private TextField shoeBindingTF;

	@FXML
	private AnchorPane mainPane;

	@FXML
	private Button saveButton;

	private Stage stage;

	private ObservableList<Backdoor> bdList;

	private InjectGenerator iGen;
	private InjectCaptchaService cService;
	private TaskMaster taskMaster;

	private volatile boolean polling;

	public void setTaskMaster(TaskMaster tm){
		this.taskMaster = tm;
	}

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setTitle("Backdoor");
		stage.setResizable(false);
		stage.setMinHeight(mainPane.getPrefHeight());
		stage.setMinWidth(mainPane.getPrefWidth());
		stage.setScene(new Scene(mainPane));

		//Radio button
		final ToggleGroup capToggleGroup = new ToggleGroup();
		constantRadio.setToggleGroup(capToggleGroup);
		autoRadio.setToggleGroup(capToggleGroup);
		constantRadio.setSelected(true);

		//Initial visibility settings
		bdInfoBox.setVisible(false);
		captchaSettingsBox.setVisible(false);
		bankEnabledLabel.setText("DISABLED");

		//ListView setup
		bdListView.setCellFactory(listView -> new ListCell<Backdoor>(){
			@Override
			public void updateItem(Backdoor bd, boolean empty) {
				super.updateItem(bd, empty);
				if (!empty) {
					setText(bd.getName());
				}
			}
		});

		//List Content Setup
		bdList = FXCollections.observableArrayList();
		bdListView.setItems(bdList);

		//Current polling server for BDs status
		polling = false;


		Thread bankedMonitor = new Thread(() -> {
			while(true){
				Backdoor selectBd = bdListView.getSelectionModel().getSelectedItem();
				if(selectBd != null){
					if(selectBd.isCaptchaGenEnabled()){
						//handle automatic captcha scaling
						if(autoRadio.isSelected()){
							int users = 0;
							if(shoeBindingTF.getText() != null && !shoeBindingTF.getText().isEmpty()){
								users = taskMaster.getNumTasksWithShoeAndSite(shoeBindingTF.getText(), SiteKey.getByName(selectBd.getSite()));
							}
							
							selectBd.setUsers(users);
							String userString = String.valueOf(users);
							Platform.runLater(() -> {
								constantTF.setText(userString);
							});
						}

						//find out how many captchas were making
						String numBankers = String.valueOf(cService.getBank().getNumBankers(selectBd.getSiteKey()));
						String numBanked = String.valueOf(cService.getBank().getNumBanked(selectBd.getSiteKey()));
						Platform.runLater(() -> {
							numBankersLabel.setText(numBankers);
							bankedLabel.setText(numBanked);
						});
					}
				}
				try {
					Thread.sleep(1000);
				} catch (Exception e) {}
			}
		});
		bankedMonitor.setDaemon(true);
		bankedMonitor.start();

	}

	public void prompt(){
		pollServer();
		stage.showAndWait();
	}

	@FXML
	void bdListViewOnClick(MouseEvent event) {
		updateUI();
	}

	private void updateUI() {
		Backdoor selectBd = bdListView.getSelectionModel().getSelectedItem();

		if(selectBd != null){
			//Site value
			siteValueLabel.setText(selectBd.getSite());

			//Bound shoe
			if(selectBd.getBoundShoeName() != null && !selectBd.getBoundShoeName().isEmpty()){
				shoeBindingTF.setText(selectBd.getBoundShoeName());
			}

			//Captcha config
			if(selectBd.needsCaptcha()){
				captchaSettingsBox.setVisible(true);
				String siteKey = selectBd.getSiteKey();
				String formatSitekey = siteKey;
				formatSitekey = formatSitekey.substring(formatSitekey.length() - 10, formatSitekey.length());
				siteKeyLabel.setText(formatSitekey);
				numBankersLabel.setText(String.valueOf(cService.getBank().getNumBankers(siteKey)));
				bankedLabel.setText(String.valueOf(cService.getBank().getNumBanked(siteKey)));
				//updateEnabledLabel
			}

			bdInfoBox.setVisible(true);
		}
	}

	@FXML
	void updateOnClick(ActionEvent event) {
		pollServer();
	}

	@FXML
	void inspectOnClick(ActionEvent event) {

		Backdoor selectBd = bdListView.getSelectionModel().getSelectedItem();

		if(selectBd != null){
			new BackdoorInspectPopup(selectBd);
		}

	}

	@FXML
	void createOnClick(ActionEvent event) {

	}

	@FXML
	void applyOnClick(ActionEvent event) {
		saveChanges();
	}

	private void saveChanges(){
		Backdoor selectBd = bdListView.getSelectionModel().getSelectedItem();

		if(selectBd != null){
			if(shoeBindingTF.getText() != null && !shoeBindingTF.getText().isEmpty()){
				selectBd.setBoundShoeName(shoeBindingTF.getText());
			}

			if(selectBd.needsCaptcha()){
				if(constantRadio.isSelected() && constantTF.getText() != null){
					constantTF.setEditable(true);
					int u = Integer.parseInt(constantTF.getText());
					selectBd.setUsers(u);
				}

			}

		}
	}

	@FXML
	void captchaStartOnClick(ActionEvent event) {
		saveChanges();

		Backdoor selectBd = bdListView.getSelectionModel().getSelectedItem();

		if(selectBd != null){
			selectBd.setCaptchaGenEnabled(true);
			bankEnabledLabel.setText("ENABLED");
		}

	}

	@FXML
	void autoRadioOnClick(ActionEvent event) {
		saveChanges();
	}

	@FXML
	void captchaStopOnClick(ActionEvent event) {
		saveChanges();

		Backdoor selectBd = bdListView.getSelectionModel().getSelectedItem();

		if(selectBd != null){
			selectBd.setCaptchaGenEnabled(false);
			bankEnabledLabel.setText("DISABLED");
		}

	}

	@FXML
	void saveOnClick(ActionEvent event) {
		saveChanges();
		stage.close();
	}

	public void pollServer(){
		if(!polling){
			Thread t = new Thread(() -> {
				polling = true;
				try{
					List<Backdoor> serverBdList = ShubiMain.ws.getBackdoors();
					ObservableList<Backdoor> tempClientList = FXCollections.observableArrayList(bdList);

					for(Backdoor serverBd : serverBdList){
						boolean add = true;
						for(Backdoor clientBd: tempClientList){
							if(serverBd.getName().equals(clientBd.getName()) && serverBd.getSite().equals(clientBd.getSite())){
								add = false;
							}
						}
						if(add) bdList.add(serverBd);
					}

					Platform.runLater(() -> {
						bdListView.refresh();
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

	public InjectGenerator getiGen() {
		return iGen;
	}

	public void setiGen(InjectGenerator iGen) {
		this.iGen = iGen;
		this.iGen.setBackdoors(bdList);
		cService = new InjectCaptchaService(iGen.getcPool(), bdList);
	}

}
