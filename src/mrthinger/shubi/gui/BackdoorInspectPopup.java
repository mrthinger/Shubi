package mrthinger.shubi.gui;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mrthinger.shubi.type.Backdoor;

public class BackdoorInspectPopup {

	
	private Stage stage;
	private OutputStream os;
	private TextArea tA;
	
	public BackdoorInspectPopup(Backdoor bd){
		stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setTitle(bd.getName());
		stage.setResizable(true);

		
		tA = new TextArea();
		tA.setEditable(false);
		AnchorPane.setTopAnchor(tA, 0D);
		AnchorPane.setBottomAnchor(tA, 0D);
		AnchorPane.setLeftAnchor(tA, 0D);
		AnchorPane.setRightAnchor(tA, 0D);

		
		os = new OutputStream() {
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
		
		
		
		AnchorPane layout = new AnchorPane();
		layout.setPadding(new Insets(8));
		layout.setMinHeight(600);
		layout.setMinWidth(600);
		layout.getChildren().addAll(tA);

		stage.setScene(new Scene(layout));
		
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Backdoor.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			// output pretty printed
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(bd, os);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
		stage.showAndWait();

		
	}
	
	private void updateTextArea(final String text) {
		Platform.runLater(new Runnable(){

			@Override
			public void run() {
				tA.appendText(text);
			}

		});
	}
	
	public OutputStream getOS(){
		return os;
	}
}
