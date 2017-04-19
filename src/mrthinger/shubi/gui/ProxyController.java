package mrthinger.shubi.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import mrthinger.shubi.FileFormatException;
import mrthinger.shubi.SiteKey;
import mrthinger.shubi.proxyload.ProxyTestDriver;
import mrthinger.shubi.type.Proxy;
import mrthinger.shubi.type.ProxyStore;

public class ProxyController {

	@FXML
	private CheckBox useTestedCheckbox;

	@FXML
	private Button submitButton;

	@FXML
	private ListView<Proxy> untestedProxyList;

	@FXML
	private Button loadMppButton;

	@FXML
	private ProgressBar progressBar;

	@FXML
	private Button testSettingsButton;

	@FXML
	private TableView<Proxy> testedProxyTable;

	@FXML
	private TableColumn<Proxy, String> proxyIpColumn;

	@FXML
	private AnchorPane mainPane;

	@FXML
	private Button loadFileButton;

	@FXML
	private Button testButton;

	@FXML
	private Label statusLabel;

	@FXML
	private CheckBox useUntestedCheckbox;

	private Stage proxyPopup;
	private ProxyStore untestedP;
	private ProxyStore testedP;

	private ProxyConfigPopup mppPop;
	private TestSettingsPopup settingsPop;
	private List<String> urlsToTest;

	private ProxyTestDriver proxyTester;
	
	public boolean useTested(){
		return useTestedCheckbox.isSelected();
	}
	
	public boolean useUntested(){
		return useUntestedCheckbox.isSelected();
	}

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		proxyPopup = new Stage();
		proxyPopup.initModality(Modality.APPLICATION_MODAL);
		proxyPopup.setTitle("MrThinger's Proxy Loader");
		proxyPopup.setResizable(true);
		proxyPopup.setMinHeight(mainPane.getMinHeight());
		proxyPopup.setMinWidth(mainPane.getMinWidth());
		proxyPopup.setScene(new Scene(mainPane));

		mppPop = new ProxyConfigPopup();
		settingsPop = new TestSettingsPopup();

		untestedP = new ProxyStore();
		testedP = new ProxyStore();
		urlsToTest = new ArrayList<>();

		untestedProxyList.setCellFactory(listView -> new ListCell<Proxy>(){
			@Override
			public void updateItem(Proxy proxy, boolean empty) {
				super.updateItem(proxy, empty);
				if (!empty) {
					setText(proxy.toString());
				}
			}
		});
		untestedProxyList.setItems(untestedP.getProxies());

		proxyIpColumn.setCellValueFactory(new PropertyValueFactory<>("ip"));

		proxyTester = new ProxyTestDriver(testedP, progressBar, statusLabel);
		
		startUpdateUIThread();
	}

	@FXML
	void loadFileOnClick(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		File proxyFile = fileChooser.showOpenDialog(proxyPopup);
		if(proxyFile != null){
			try {
				untestedP.addProxies(proxyFile);
				untestedProxyList.refresh();
			} catch(FileFormatException e1) {
				System.out.println("Invald proxy file format. Please use format ip:port:user:pass on each line.");
			} catch(FileNotFoundException e2){
				System.out.println("File not found.");
			} catch (IOException e) {
				System.out.println("IOException.");
			}
		}
	}

	
	@FXML
	void loadMppOnClick(ActionEvent event) {
		mppPop.prompt();
		if(mppPop.getProxyStore() != null){
			untestedP.addProxies(mppPop.getProxyStore().getProxies());
			untestedProxyList.refresh();
		}
	}

	@FXML
	void testSettingsOnClick(ActionEvent event) {

		settingsPop.prompt();

		urlsToTest = settingsPop.getUrlsToTest();

		List<TableColumn<Proxy, String>> newColumns = new ArrayList<>();

		for(int i = testedProxyTable.getColumns().size() - 1; i >= 1; i--){
			testedProxyTable.getColumns().remove(i);
		}

		for(String url : urlsToTest){
			System.out.println(url);

			//Setup columns
			for(SiteKey site : SiteKey.getByUrl(url)){
				TableColumn<Proxy, String> tC = new TableColumn<Proxy, String>(site.getName());
				
				//Get the response time value
				tC.setCellValueFactory(new Callback<CellDataFeatures<Proxy, String>, ObservableValue<String>>() {
					public ObservableValue<String> call(CellDataFeatures<Proxy, String> p) {
						return new ReadOnlyObjectWrapper<String>(String.valueOf(p.getValue().getResponseTimeMap().get(site)));
					}
				});
				
				//Box value converter (response value -> meaning)
				tC.setCellFactory(column -> {
				    return new TableCell<Proxy, String>() {
				        @Override
				        protected void updateItem(String item, boolean empty) {
				            super.updateItem(item, empty);

				            if (item == null || empty) {
				                setText(null);
				                setStyle("");
				            } else {
				               long value = Long.parseLong(item);
				               
				               if(value == 1L){
				            	   setText("Waiting");
				            	   setStyle("-fx-background-color: lightgrey");
				               }else if(value == 2L){
				            	   setText("Testing");
				            	   setStyle("-fx-background-color: yellow");
				               }else if(value == -1L){
				            	   setText("DNC");
				            	   setStyle("-fx-background-color: red");
				               }else{
				            	   setText(item);
				            	   setStyle("-fx-background-color: lime");
				               }
				            }
				        }
				    };
				});
				newColumns.add(tC);
			}
		}

		for(TableColumn<Proxy, String> tC : newColumns){
			testedProxyTable.getColumns().add(tC);
		}

		testedProxyTable.refresh();

	}

	@FXML
	void testOnClick(ActionEvent event) {	
		
		if(untestedP.getNumProxies() < 1){
			statusLabel.setText("Status: Load proxies.");
			return;
		}

		if(urlsToTest.size() < 1){
			statusLabel.setText("Status: Pick settings.");
			return;
		}
		
		if(proxyTester.isRunning()){
			statusLabel.setText("Status: Test in progress.");
			return;
		}

		statusLabel.setText("Status: Testing...");

		proxyTester.setpList(new ProxyStore(untestedP.getProxies()));
		proxyTester.setUrlsToTest(urlsToTest);
		
		untestedProxyList.setItems(null);
		untestedP = new ProxyStore();
		untestedProxyList.setItems(untestedP.getProxies());

		testedProxyTable.setItems(proxyTester.getProxyTestingObsList());

		Thread proxyTestThread = new Thread(proxyTester);
		proxyTestThread.setDaemon(true);
		proxyTestThread.start();

	}

	@FXML
	void submitOnClick(ActionEvent event) {
		proxyPopup.close();
	}

	private void startUpdateUIThread(){

		Thread updateTableThread = new Thread(()->{
			while(true){
				Platform.runLater(() -> {
					
					testedProxyTable.refresh();
					
				});

				try {
					Thread.sleep(100);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, "ProxyTableUpdate");

		updateTableThread.setDaemon(true);
		updateTableThread.start();
	}

	public void prompt(){
		proxyPopup.showAndWait();
	}

	public ProxyStore getProxyStore(){
		
		if(useTestedCheckbox.isSelected() && !useUntestedCheckbox.isSelected()){
			return testedP;
		}else if(useUntestedCheckbox.isSelected() && !useTestedCheckbox.isSelected()){
			return untestedP;
		}else if(useTestedCheckbox.isSelected() && useUntestedCheckbox.isSelected()){
			ProxyStore p = new ProxyStore();
			p.addProxies(testedP.getProxies());
			p.addProxies(untestedP.getProxies());
			return p;
		}
		
		return null;
	}
	
	public boolean canTakeProxyStore(){
		if(useTestedCheckbox.isSelected() && proxyTester.isRunning()){
			System.out.println("Proxies still being tested!");
			return false;
		}
		
		return true;
	}

}
