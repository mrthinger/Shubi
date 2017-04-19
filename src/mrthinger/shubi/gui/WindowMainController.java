package mrthinger.shubi.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import mrthinger.shubi.Info;
import mrthinger.shubi.ShubiMain;
import mrthinger.shubi.SiteKey;
import mrthinger.shubi.task.Task;
import mrthinger.shubi.task.TaskMaster;
import mrthinger.shubi.type.AccountStore;
import mrthinger.shubi.type.ProxyStore;
import mrthinger.shubi.type.SizeList;

public class WindowMainController{

	//First Table
	@FXML
	private TableView<Task> taskView;

	@FXML
	private TableColumn<Task, String> shoeColumn;

	@FXML
	private TableColumn<Task, String> siteColumn;

	@FXML
	private TableColumn<Task, String> statusColumn;

	//Second table
	@FXML
	private TableView<Task> atnTaskView;

	@FXML
	private TableColumn<Task, String> atnStatusColumn;

	@FXML
	private TableColumn<Task, String> atnShoeColumn;

	@FXML
	private TableColumn<Task, String> atnSizeColumn;

	@FXML
	private TableColumn<Task, String> atnSiteColumn;

	//Buttons
	@FXML
	private Button deleteTaskButton;

	@FXML
	private Button deleteAllTasksButton;

	@FXML
	private Button startButton;

	@FXML
	private Label shoeLabel;

	@FXML
	private TextField sizeTextField;

	@FXML
	private Button acButton;

	@FXML
	private Label shubiLabel;

	@FXML
	private Label sitesLabel;

	@FXML
	private ChoiceBox<SiteKey> sitesChoice;

	@FXML
	private Label sizesLabel;

	@FXML
	private ToolBar toolbar;

	@FXML
	private Label urlLabel;

	@FXML
	private TextField numTasksTextField;

	@FXML
	private TextArea consoleView;

	@FXML
	private Button proxyButton;

	@FXML
	private Button backdoorButton;

	@FXML
	private Label maxPairsLabel;

	@FXML
	private TextField maxPairsTextField;

	@FXML
	private AnchorPane mainPane;

	@FXML
	private Button accountButton;

	@FXML
	private TextField shoeTextField;

	@FXML
	private Button showTaskButton;

	@FXML
	private Button showConsoleButton;

	@FXML
	private TextField linkTextField;

	@FXML
	private VBox rightLayout;

	@FXML
	private VBox topLeftLayout;

	@FXML
	private HBox bottomLeftLayout;

	@FXML
	private VBox taskControlBox;

	@FXML
	private Button sizePickButton;

	@FXML
	private Button restartButton;

	@FXML
	private Button payButton;

	@FXML
	private Button injectButton;

	@FXML
	private Button uploadCartButton;

	@FXML
	private Button cartNetworkButton;

	@FXML
	private Button showTaskMetaButton;

	@FXML
	private Button onoffButton;

	@FXML
	private Button releaseModeButton;

	@FXML
	private Button settingsButton;
	
	//Stage
	private Stage primaryStage;

	//Accounts
	private AccountStore accountStore;

	//Sizes
	private SizeSelectionPopup sizePopup;
	private SizeList sizeList;

	//Proxies
	private ProxyController proxyPopup;
	private ProxyStore proxyStore;
	
	//Settings popup
	private SettingsController settingsPopup;

	//Backdoors
	private BackdoorController backdoorPopup;

	//Master task list
	private TaskMaster taskMaster;

	//Extra metadata for a task (optional)
	private Map<String, String> extraInfo;

	//Main paypopup
	public static PayPopup payPopup;
	public static InjectPopup injectPopup;

	//Task metadata popups
	private List<MetaController> metaList;

	//Release Mode
	private double defaultLeftAnchor;
	private boolean inReleaseMode;

	public static PayPopup getPayPopup(){
		return payPopup;
	}
	public static InjectPopup getInjectPopup(){
		return injectPopup;
	}

	public void setPrimaryStage(Stage stage){
		primaryStage = stage;
		primaryStage.setMinHeight(mainPane.getPrefHeight());
		primaryStage.setMinWidth(mainPane.getPrefWidth());

		primaryStage.setScene(new Scene(mainPane));
		primaryStage.setTitle(Info.TITLE);

		Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

		primaryStage.setX((screenBounds.getWidth() - primaryStage.getWidth()) / 2); 
		primaryStage.setY((screenBounds.getHeight() - primaryStage.getHeight()) / 2);  
		primaryStage.setResizable(true);


		//On close
		primaryStage.setOnCloseRequest((event) -> {
			//If key was being used, log out its session
			if(ShubiMain.ws != null){
				ShubiMain.ws.logout();
				System.out.println("logout called from close");
			}

			for(MetaController mc : metaList){
				mc.shutOff();
			}
		});

		redirectSystemStreams();
		startUpdateTableThread();

		//System.out.println(ShubiMain.ws.getBackdoors());
	}

	public TaskMaster getTaskMaster(){
		return taskMaster;
	}

	public void setProxyController(ProxyController proxyPopup){
		this.proxyPopup = proxyPopup;
	}
	
	public void setSettingsController(SettingsController settingsController){
		this.settingsPopup = settingsController;
	}
	
	public void setBackdoorController(BackdoorController bdController){
		this.backdoorPopup = bdController;
	}

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		//Setup releasemode
		defaultLeftAnchor = AnchorPane.getLeftAnchor(rightLayout);
		inReleaseMode = false;

		//Setup popups
		sizePopup = new SizeSelectionPopup();
		payPopup = new PayPopup();
		injectPopup = new InjectPopup();
		metaList = new ArrayList<>();
		
		//if(!Info.LOGIN)backdoorButton.setDisable(true);

		taskMaster = new TaskMaster();
		accountStore = new AccountStore();

		//Setup table
		shoeColumn.setCellValueFactory(new PropertyValueFactory<>("shoe"));
		siteColumn.setCellValueFactory(new PropertyValueFactory<>("site"));
		statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

		//Setup atn table2
		atnShoeColumn.setCellValueFactory(new PropertyValueFactory<>("shoe"));
		atnSiteColumn.setCellValueFactory(new PropertyValueFactory<>("site"));
		atnStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
		atnSizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));

		//OLD COLUMNS
		//remainingColumn.setCellValueFactory(new PropertyValueFactory<>("remainingPairs"));
		//sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
		//usernameColumn.setCellValueFactory(new PropertyValueFactory<>("userpass"));
		//urlColumn.setCellValueFactory(new PropertyValueFactory<>("url"));
		//proxyColumn.setCellValueFactory(new PropertyValueFactory<>("proxy"));

		//Set site choices
		for(SiteKey sk : SiteKey.values()){
			if(!sk.equals(SiteKey.ADIDAS_US_BACKDOOR) && !sk.equals(SiteKey.ADIDAS_UK_BACKDOOR) ){
				sitesChoice.getItems().add(sk);
			}
		}
		sitesChoice.getSelectionModel().selectFirst();
		sitesChoice.getSelectionModel().selectedItemProperty().addListener(
				(ObservableValue<? extends SiteKey> observable, SiteKey oldValue, SiteKey newValue) -> {

					if(oldValue.equals(SiteKey.ADIDAS_US_BACKDOOR) || oldValue.equals(SiteKey.ADIDAS_UK_BACKDOOR)){
						linkTextField.setText(null);
						urlLabel.setVisible(true);
						linkTextField.setVisible(true);
						extraInfo = null;
						sitesChoice.getItems().removeAll(SiteKey.ADIDAS_US_BACKDOOR, SiteKey.ADIDAS_UK_BACKDOOR);
					}

					//Special yeezy auto fillin
					if(newValue.equals(SiteKey.YEEZY_US)){
						urlLabel.setVisible(false);
						linkTextField.setVisible(false);
						linkTextField.setText(Info.YEEZY_US_URL);
						shoeTextField.setText(Info.YEEZY_NAME);

						ArrayList<String> _sizes = new ArrayList<>();
						_sizes.add("10");
						sizeList = new SizeList(_sizes, "13", "7");

						sizeTextField.setText("10");

						maxPairsLabel.setVisible(false);
						maxPairsTextField.setText("1");
						maxPairsTextField.setVisible(false);

						sizesLabel.setVisible(false);
						sizeTextField.setVisible(false);
						sizePickButton.setVisible(false);
					}

					if(oldValue.equals(SiteKey.YEEZY_US)){
						linkTextField.setText(null);
						urlLabel.setVisible(true);
						linkTextField.setVisible(true);

						sizeList = null;
						sizeTextField.setText("");

						maxPairsLabel.setVisible(true);
						maxPairsTextField.setText(null);
						maxPairsTextField.setVisible(true);

						sizesLabel.setVisible(true);
						sizeTextField.setVisible(true);
						sizePickButton.setVisible(true);
					}
				});

	}


	@FXML
	void releaseModeOnClick(ActionEvent event) {
		if(!inReleaseMode){
			topLeftLayout.setVisible(false);
			topLeftLayout.setManaged(false);
			bottomLeftLayout.setVisible(false);
			bottomLeftLayout.setManaged(false);
			taskView.setVisible(false);
			taskView.setManaged(false);
			
			accountButton.setVisible(false);
			accountButton.setManaged(false);
			
			proxyButton.setVisible(false);
			proxyButton.setManaged(false);
			
			backdoorButton.setVisible(false);
			backdoorButton.setManaged(false);
			
			acButton.setVisible(false);
			acButton.setManaged(false);

			releaseModeButton.setText("Setup");
			
			AnchorPane.setLeftAnchor(rightLayout, 0D);
			
			//hide console going into release mode
			if(consoleView.isVisible()){
				consoleView.setVisible(false);
				consoleView.setManaged(false);
			}
			
			inReleaseMode = true;
		}else{
			topLeftLayout.setVisible(true);
			topLeftLayout.setManaged(true);
			bottomLeftLayout.setVisible(true);
			bottomLeftLayout.setManaged(true);
			taskView.setVisible(true);
			taskView.setManaged(true);
			
			accountButton.setVisible(true);
			accountButton.setManaged(true);
			
			proxyButton.setVisible(true);
			proxyButton.setManaged(true);
			
			backdoorButton.setVisible(true);
			backdoorButton.setManaged(true);
			
			acButton.setVisible(true);
			acButton.setManaged(true);

			releaseModeButton.setText("Release");
			
			AnchorPane.setLeftAnchor(rightLayout, defaultLeftAnchor);
			inReleaseMode = false;
		}

	}

	@FXML
	void onTaskViewClicked(MouseEvent event) {
		if(taskView.getSelectionModel().getSelectedItem() != null){
			atnTaskView.getSelectionModel().clearSelection();
		}


		refreshDriverButtons();
		handleTaskMetaDataWindows();
	}


	@FXML
	void onAtnTaskViewClicked(MouseEvent event) {
		if(atnTaskView.getSelectionModel().getSelectedItem() != null){
			taskView.getSelectionModel().clearSelection();
		}

		refreshDriverButtons();
		handleTaskMetaDataWindows();
	}
	
	@FXML
	void settingsOnClick(){
		settingsPopup.prompt();
	}

	private void handleTaskMetaDataWindows() {
		for(int i = metaList.size() - 1; i >= 0; i--){
			//See if meta box wants a new task, if so give it
			if(metaList.get(i).isRunning()){
				if(metaList.get(i).wantsNewTask()){

					if(taskView.getSelectionModel().getSelectedItem() != null){
						metaList.get(i).setTask(taskView.getSelectionModel().getSelectedItem());
					}

					if(atnTaskView.getSelectionModel().getSelectedItem() != null){
						metaList.get(i).setTask(atnTaskView.getSelectionModel().getSelectedItem());
					}
				}
			}
			//clean out old/non-running meta boxes
			else{
				metaList.remove(i);
			}
		}
	}
	
	private void refreshDriverButtons() {
		Task table1Task = taskView.getSelectionModel().getSelectedItem();
		Task table2Task = atnTaskView.getSelectionModel().getSelectedItem();
		selectedTaskForButtons(table1Task);
		selectedTaskForButtons(table2Task);
		//disable buttons if nothing is selected
		if(table1Task == null && table2Task == null)
		{
			onoffButton.setDisable(true);
			showTaskButton.setDisable(true);
			restartButton.setDisable(true);
			deleteTaskButton.setDisable(true);
			injectButton.setDisable(true);
			payButton.setDisable(true);
			showTaskMetaButton.setDisable(true);
		}

		//disable delete all if their are no tasks
		if(taskView.getItems().size() == 0 && atnTaskView.getItems().size() == 0){
			deleteAllTasksButton.setDisable(true);
		}else{
			deleteAllTasksButton.setDisable(false);
		}
	}

	private void selectedTaskForButtons(Task t) {
		if(t != null){
			//Meta button control (doesnt require driver to be set)
			showTaskMetaButton.setDisable(false);

			//Driver specific controlls
			if(t.getDriver() != null){
				//If task has driver allow it to be controlled
				showTaskButton.setDisable(false);
				restartButton.setDisable(false);
				deleteTaskButton.setDisable(false);
				onoffButton.setDisable(false);
				injectButton.setDisable(false);

				//payment button enabler
				if(t.getDriver().hasPaymentButtonFunctionality()){
					payButton.setDisable(false);
				}else{
					payButton.setDisable(true);
				}
			}
		}
	}

	@FXML
	void backdoorOnClick(ActionEvent event) {
		backdoorPopup.prompt();
	}

	@FXML
	void showConsoleOnClick(ActionEvent event){
		if(consoleView.isVisible()){
			consoleView.setVisible(false);
			consoleView.setManaged(false);
		}else{
			consoleView.setVisible(true);
			consoleView.setManaged(true);
		}
	}

	@FXML
	void injectOnClick(ActionEvent event) {
		//send inject request
		Task selectedTask = null; 

		if(atnTaskView.getSelectionModel().getSelectedItem() != null){
			selectedTask = atnTaskView.getSelectionModel().getSelectedItem();
		}else if(taskView.getSelectionModel().getSelectedItem() != null){
			selectedTask = taskView.getSelectionModel().getSelectedItem();
		}

		if(selectedTask != null && selectedTask.getDriver() != null){
			selectedTask.getDriver().requestInject();
		}
	}

	@FXML
	void payOnClick(ActionEvent event) {
		//send pay request
		Task selectedTask = null; 

		if(atnTaskView.getSelectionModel().getSelectedItem() != null){
			selectedTask = atnTaskView.getSelectionModel().getSelectedItem();
		}else if(taskView.getSelectionModel().getSelectedItem() != null){
			selectedTask = taskView.getSelectionModel().getSelectedItem();
		}

		if(selectedTask != null && selectedTask.getDriver() != null){
			selectedTask.getDriver().requestPay();
		}
	}

	@FXML
	void onSizeSelect(ActionEvent event) {

		sizeList = sizePopup.promptSizeSelection();

		if(sizeList != null){
			sizeTextField.setText(sizeList.toString());
		}


	}

	@FXML
	void accountOnClick(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		File accountsFile = fileChooser.showOpenDialog(primaryStage);

		if(accountsFile != null){
			try{
				accountStore.addAccounts(accountsFile);
				payPopup.setAdidasGuestCheckouts(accountStore.getAdidasGuestCheckouts());
			}catch(FileNotFoundException e){
				System.out.println("Account file not found.");
			}

		}

	}

	@FXML
	void proxyOnClick(ActionEvent event) {
		proxyPopup.prompt();
	}

	@FXML
	void showTaskMetaOnClick(ActionEvent e){

		//load new task meta window
		FXMLLoader loader = new FXMLLoader();
		loader.setBuilderFactory(new JavaFXBuilderFactory());
		loader.setLocation(ClassLoader.getSystemClassLoader().getResource("mrthinger/shubi/gui/TaskMeta.fxml"));
		try {
			loader.load();
		} catch (IOException e1) {
			e1.printStackTrace();
		}


		MetaController metaWin = (MetaController) loader.getController();

		if(taskView.getSelectionModel().getSelectedItem() != null){
			metaWin.setTask(taskView.getSelectionModel().getSelectedItem());
		}

		if(atnTaskView.getSelectionModel().getSelectedItem() != null){
			metaWin.setTask(atnTaskView.getSelectionModel().getSelectedItem());
		}

		//Add to master list
		metaList.add(metaWin);
	}

	@FXML
	void onoffOnClick(ActionEvent e){
		//send pause
		Task selectedTask = null; 

		if(atnTaskView.getSelectionModel().getSelectedItem() != null){
			selectedTask = atnTaskView.getSelectionModel().getSelectedItem();
		}else if(taskView.getSelectionModel().getSelectedItem() != null){
			selectedTask = taskView.getSelectionModel().getSelectedItem();
		}

		if(selectedTask != null && selectedTask.getDriver() != null){
			selectedTask.getDriver().requestPause();
		}
	}

	@FXML
	void acOnClick(ActionEvent event) {
		//TODO: Account Creator UI
	}

	@FXML
	void uploadCartOnClick(ActionEvent e){
		//TODO: Cart network
	}

	@FXML
	void cartNetworkOnClick(ActionEvent e){

	}

	@FXML
	void onStartTasksClicked(ActionEvent event) {

		if(sizeList == null || !sizeList.hasSize()){
			System.out.println("Missing sizes!");
			return;
		}

		if(proxyPopup.canTakeProxyStore()){
			if(proxyPopup.getProxyStore() != null && proxyPopup.getProxyStore().getNumProxies() > 0){
				System.out.println("Using proxies!");
				proxyStore = proxyPopup.getProxyStore();
			}else{
				System.out.println("Not using proxies!");
			}
		}else{
			return;
		}

		taskMaster.addTasks(accountStore, proxyStore, shoeTextField.getText(), sitesChoice.getValue(),
				sizeList, linkTextField.getText(), maxPairsTextField.getText(), numTasksTextField.getText(),
				extraInfo, proxyPopup.useTested(), proxyPopup.useUntested());

		taskMaster.updateAtnLists();

		taskView.setItems(taskMaster.getNonAtnTasks());
		atnTaskView.setItems(taskMaster.getAtnTasks());

		refreshDriverButtons();
	}

	@FXML
	void onDeleteAllTasksClicked(ActionEvent event) {

		List<Task> removedTasks = new ArrayList<>();

		for(Task t : taskMaster.getTasks()){
			t.getDriver().requestClose();
			removedTasks.add(t);
		}

		taskMaster.getTasks().removeAll(removedTasks);

		taskMaster.updateAtnLists();
		refreshDriverButtons();
	}

	@FXML
	void onDeleteTaskClicked(ActionEvent event) {
		Task selectedTask = null; 

		if(atnTaskView.getSelectionModel().getSelectedItem() != null){
			selectedTask = atnTaskView.getSelectionModel().getSelectedItem();
		}else if(taskView.getSelectionModel().getSelectedItem() != null){
			selectedTask = taskView.getSelectionModel().getSelectedItem();
		}

		if(selectedTask != null && selectedTask.getDriver() != null){
			selectedTask.getDriver().requestClose();
			taskMaster.getTasks().remove(selectedTask);
		}

		taskMaster.updateAtnLists();
		refreshDriverButtons();
	}

	@FXML
	void onRestartClicked(ActionEvent event) {
		Task selectedTask = null; 

		if(atnTaskView.getSelectionModel().getSelectedItem() != null){
			selectedTask = atnTaskView.getSelectionModel().getSelectedItem();
		}else if(taskView.getSelectionModel().getSelectedItem() != null){
			selectedTask = taskView.getSelectionModel().getSelectedItem();
		}

		if(selectedTask != null && selectedTask.getDriver() != null){
			selectedTask.getDriver().requestRestart();
		}
	}

	@FXML
	void showTask(ActionEvent event) {
		Task selectedTask = null; 

		if(atnTaskView.getSelectionModel().getSelectedItem() != null){
			selectedTask = atnTaskView.getSelectionModel().getSelectedItem();
		}else if(taskView.getSelectionModel().getSelectedItem() != null){
			selectedTask = taskView.getSelectionModel().getSelectedItem();
		}


		if(selectedTask != null && selectedTask.getDriver() != null){
			//show selected task
			selectedTask.getDriver().requestToFront();
			//And hide the others
			ArrayList<Task> ts = new ArrayList<>(taskMaster.getTasks());
			ts.remove(selectedTask);
			for(Task t : ts){
				if(t != null && t.getDriver() != null){
					t.getDriver().requestHide();
				}
			}
		}
	}

	private void startUpdateTableThread(){

		Thread updateTableThread = new Thread(()->{
			while(true){

				Platform.runLater(() -> {
					taskMaster.updateAtnLists();

					taskView.refresh();
					atnTaskView.refresh();
				});

				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, "TableUpdate");

		updateTableThread.setDaemon(true);
		updateTableThread.start();
	}


	private void updateTextArea(final String text) {
		Platform.runLater(new Runnable(){

			@Override
			public void run() {
				consoleView.appendText(text);
			}

		});
	}

	//Move sysout to consoleView textArea
	private void redirectSystemStreams() {
		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				updateTextArea(String.valueOf((char) b));
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				updateTextArea(new String(b, off, len));
			}

			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}
		};

		System.setOut(new PrintStream(out, true));
		if(!Info.DEBUG)System.setErr(new PrintStream(out, true));
	}

}