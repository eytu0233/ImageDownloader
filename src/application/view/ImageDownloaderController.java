package application.view;

import java.awt.datatransfer.StringSelection;
import java.io.File;

import application.MainApp;
import application.model.CutBook;
import application.model.ImageDownloadTask;
import application.model.UrlScanTask;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;

public class ImageDownloaderController {

	@FXML
	private TableView<ImageDownloadTask> imageDownloaderTable;
	@FXML
	private TableColumn<ImageDownloadTask, String> fileNameColumn;
	@FXML
	private TableColumn<ImageDownloadTask, String> fileSizeColumn;
	@FXML
	private TableColumn<ImageDownloadTask, String> directoryColumn;
	@FXML
	private TableColumn<ImageDownloadTask, String> statusColumn;
	@FXML
	private TableColumn<ImageDownloadTask, Double> progressColumn;
	@FXML
	private TableColumn<ImageDownloadTask, String> urlColumn;
	@FXML
	private TextField urlTextField;
	@FXML
	private ProgressBar totalProgressBar;
	@FXML
	private Label totalProgressBarPercentage;
	@FXML
	private MenuItem reDownloadMenuItem;

	private MainApp mainApp;

	/**
	 * Initializes the controller class. This method is automatically called
	 * after the fxml file has been loaded.
	 */
	@FXML
	private void initialize() {
		fileNameColumn.setCellValueFactory(cellData -> cellData.getValue()
				.getFileNameProperty());
		fileSizeColumn.setCellValueFactory(cellData -> cellData.getValue()
				.getFileSizeProperty());
		directoryColumn.setCellValueFactory(cellData -> cellData.getValue()
				.getDirectoryProperty());
		urlColumn.setCellValueFactory(cellData -> cellData.getValue()
				.getUrlProperty());

		statusColumn
				.setCellValueFactory(new PropertyValueFactory<ImageDownloadTask, String>(
						"message"));
		progressColumn
				.setCellValueFactory(new PropertyValueFactory<ImageDownloadTask, Double>(
						"progress"));
		progressColumn.setCellFactory(ProgressBarTableCell
				.<ImageDownloadTask> forTableColumn());
		
		reDownloadMenuItem.setOnAction(e->{
			ImageDownloadTask downloadTask = mainApp.getImageDownloadTasks().get(imageDownloaderTable.getSelectionModel().getSelectedIndex());			
			System.out.println(downloadTask.getUrl());
			CutBook cutBook = new CutBook();
			cutBook.setBookContents(downloadTask.getUrl());
			
			ImageDownloadTask restartDownloader = new ImageDownloadTask(downloadTask.getFileName(), downloadTask.getDirectory(), downloadTask.getUrl());
			mainApp.getImageDownloadTasks().remove(downloadTask);
			mainApp.getImageDownloadTasks().add(restartDownloader);
		});
	}

	@FXML
	private void chooseDirectoryAndScan() {
		final DirectoryChooser directoryChooser = new DirectoryChooser();
		final File selectedDirectory = directoryChooser.showDialog(mainApp
				.getPrimaryStage());
		if (selectedDirectory != null && !"".equals(urlTextField.getText())) {
			totalProgressBar.setProgress(0);
			
			String dirPath = selectedDirectory.getAbsolutePath();
			UrlScanTask urlScanTask = new UrlScanTask(urlTextField.getText(),
					dirPath, mainApp.getImageDownloadTasks(), totalProgressBar, totalProgressBarPercentage);
			urlScanTask.setDaemon(true);
			urlScanTask.start();
		}
	}

	/**
	 * Is called by the main application to give a reference back to itself.
	 * 
	 * @param mainApp
	 */
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;

		imageDownloaderTable.setItems(mainApp.getImageDownloadTasks());
	}

}
