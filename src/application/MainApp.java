package application;
	
import java.io.IOException;

import application.model.ImageDownloadTask;
import application.view.ImageDownloaderController;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;


public class MainApp extends Application {
	
	private Stage primaryStage;
	private BorderPane rootLayout;	
	private ObservableList<ImageDownloadTask> imageDownloadTasks = FXCollections.observableArrayList();
	

	public ObservableList<ImageDownloadTask> getImageDownloadTasks() {
		return imageDownloadTasks;
	}
	
	public Stage getPrimaryStage(){
		return primaryStage;
	}
	
	@Override
	public void start(Stage primaryStage) {
		
		this.primaryStage = primaryStage;
        this.primaryStage.setTitle("ImageDownloader");
		
        initRootLayout();

        showImageDownloaderOverview();
	}
	
	private void showImageDownloaderOverview() {
		// TODO Auto-generated method stub
		try {
            // Load person overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/ImageDownloaderOverview.fxml"));
            AnchorPane personOverview = (AnchorPane) loader.load();

            // Set person overview into the center of root layout.
            rootLayout.setCenter(personOverview);
            
            // Give the controller access to the main app.
            ImageDownloaderController controller = loader.getController();
            controller.setMainApp(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	private void initRootLayout() {
		// TODO Auto-generated method stub
		try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
            rootLayout = loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	public static void main(String[] args) {
		launch(args);
	}
}
