package application;

import javafx.scene.paint.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.event.ChangeListener;

import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button ;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;


public class MainController {

	public static final int IMAGE_WIDTH = 640, IMAGE_HEIGHT = 512;
	public static final int IMAGE_CROPPED_WIDTH = 640, IMAGE_CROPPED_HEIGHT = 200;	
	public static final int CROP_OFFSET_X = 0, CROP_OFFSET_Y = 110;;

	@FXML private BorderPane mainBorderPane;
	@FXML private ImageView mainImageView;
	@FXML private Canvas mainImageCanvas;
	@FXML private Button previousButton, nextButton, saveButton, inputFolderButton, outputFolderButton;
	@FXML private TextField inputFolderText, outputFolderText;
	@FXML private Label fileNameLabel, filesCountLabel;
	@FXML private Spinner<Integer> offsetSpinner;
	@FXML private ListView<Category> categoryListView;
	@FXML private ListView<Path> filesListView;
	
	private InMemoryFiles imf;

	public MainController() {
	}
	
	 @FXML 
	 public void initialize() {
		 initListView();
		 filesListView.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
			 if (imf != null && imf.getFilePaths() != null && newValue.intValue() != -1) {
				try {
					Image img = ImageConversions.convertBinaryToImage(Files.readAllBytes(imf.getFilePaths().get(newValue.intValue())), IMAGE_WIDTH, IMAGE_HEIGHT);
					Utils.updateFXControl(mainImageView.imageProperty(), img);
					imf.setFileIndex(newValue.intValue());
					filesCountLabel.setText(String.valueOf(imf.getFilePaths().size()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			 
		});
		 inputFolderText.textProperty().addListener((observable, oldValue, newValue) -> {
			 imf = new InMemoryFiles(newValue); 
			 if (imf.getFilePaths().size() > 0) {
				 try {
					Image img = ImageConversions.convertBinaryToImage(Files.readAllBytes(imf.getFilePaths().get(0)), IMAGE_WIDTH, IMAGE_HEIGHT);
					Utils.updateFXControl(mainImageView.imageProperty(), img);
					filesListView.setItems(FXCollections.observableArrayList(imf.getFilePaths()));
					filesListView.getSelectionModel().select(0);
					
					mainImageCanvas.getGraphicsContext2D().setStroke(Color.WHITE);
					mainImageCanvas.getGraphicsContext2D().setLineWidth(2);
					mainImageCanvas.getGraphicsContext2D().strokeRect(CROP_OFFSET_X, CROP_OFFSET_Y, IMAGE_CROPPED_WIDTH, IMAGE_CROPPED_HEIGHT);
				} catch (IOException e) {
					e.printStackTrace();
				}				 
			 }
		});
				 
	 }
	 
	 @FXML 
	 public void selectFolder(ActionEvent event) {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setInitialDirectory(new File("D:\\ThesisProjectImages\\"));

		File file = directoryChooser.showDialog(mainBorderPane.getScene().getWindow());

		if (file != null) {
			Button source = (Button) event.getSource();
			if (source.equals(inputFolderButton)) inputFolderText.setText(file.getAbsolutePath());
			else if (source.equals(outputFolderButton)) outputFolderText.setText(file.getAbsolutePath());
		}
	 }
	 
	 @FXML
	 public void nextButtonClicked() throws IOException {
		 if (imf != null && imf.getFilePaths() != null) {
			Image img = ImageConversions.convertBinaryToImage(Files.readAllBytes(imf.getNext()), IMAGE_WIDTH, IMAGE_HEIGHT);
			Utils.updateFXControl(mainImageView.imageProperty(), img);
			refreshListView();
		 }
	 }
	 
	 @FXML
	 public void prevButtonClicked() throws IOException {
		 if (imf != null && imf.getFilePaths() != null) {
			Image img = ImageConversions.convertBinaryToImage(Files.readAllBytes(imf.getPrev()), IMAGE_WIDTH, IMAGE_HEIGHT);
			Utils.updateFXControl(mainImageView.imageProperty(), img);
			refreshListView();
		 }
	 }
	 
	 @FXML
	 public void saveButtonClicked() throws IOException {
		 if (imf != null && imf.getFilePaths() != null && imf.getFilePaths().size() > 0 && !Utils.isNullOrEmpty(outputFolderText.getText()) && categoryListView.getSelectionModel().getSelectedItem() != null) {
			 Path dirPath = Paths.get(outputFolderText.getText(), categoryListView.getSelectionModel().getSelectedItem().getPath());
			 Path filePath = Paths.get(outputFolderText.getText(), categoryListView.getSelectionModel().getSelectedItem().getPath(), imf.getActualFile().getFileName().toString());
			 Files.createDirectories(dirPath);
			 if (!Files.exists(filePath)) {
				 Files.createFile(filePath);
				 Files.write(filePath, Files.readAllBytes(imf.getActualFile()), StandardOpenOption.CREATE);			 
			 }
			 Files.delete(imf.removeActualFile());
			 filesListView.setItems(FXCollections.observableArrayList(imf.getFilePaths()));
			 refreshListView();	
		 }
	 }
	 
	 private void refreshListView() {
		filesListView.getSelectionModel().select(imf.getFileIndex());
		filesListView.scrollTo(imf.getFileIndex());
	 }
	 
	 private void initListView() {
		 ObservableList<Category> items = FXCollections.observableArrayList (new Category("Ruka se zbožím", "ruka_se_zbozim"), new Category("Ruka v regále", "ruka_v_regale"),  new Category("Prázdná ruka", "prazdna_ruka"));
		 categoryListView.setItems(items);
	 }
	 
	 public void initSceneListener() {
		 mainBorderPane.getScene().setOnKeyPressed((event) -> {
				try {
		            switch (event.getCode()) {
		                case DIGIT1: 
		                	categoryListView.getSelectionModel().select(0);
							break;
		                case DIGIT2: 
		                	categoryListView.getSelectionModel().select(1);
		                	break;
		                case DIGIT3:  
		                	categoryListView.getSelectionModel().select(2);
		                	break;
		                case DIGIT4: 
		                	categoryListView.getSelectionModel().select(3);
		                	break;
		                case DIGIT5:
		                	categoryListView.getSelectionModel().select(4);
		                	break;
		                case DIGIT6: 
		                	categoryListView.getSelectionModel().select(5);
		                	break;
		                case DIGIT7: 
		                	categoryListView.getSelectionModel().select(6);
		                	break;
		                case DIGIT8:
		                	categoryListView.getSelectionModel().select(7);
		                	break;
		                case DIGIT9: 
		                	categoryListView.getSelectionModel().select(8);
							break;
		                case LEFT: 
		                	prevButtonClicked();
		                	break;
		                case RIGHT: 
		                	nextButtonClicked();
		                	break;
		                case S: 
		                	saveButtonClicked();
		                	break;
		                default: break;
		            }  
				} catch (Exception e) {
					System.out.print("Cannot select with key " + event.getCode());
				}
	       });
	 }

}
