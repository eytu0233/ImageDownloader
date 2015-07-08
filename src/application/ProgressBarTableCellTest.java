package application;

import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator ;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class ProgressBarTableCellTest extends Application {

  @Override
  public void start(Stage primaryStage) {
    TableView<TestTask> table = new TableView<TestTask>();
    Random rng = new Random();
    for (int i = 0; i < 20; i++) {
      table.getItems().add(
          new TestTask(rng.nextInt(3000) + 2000, rng.nextInt(30) + 20));
    }

    TableColumn<TestTask, String> statusCol = new TableColumn("Status");
    statusCol.setCellValueFactory(new PropertyValueFactory<TestTask, String>(
        "message"));
    statusCol.setPrefWidth(75);
    
    TableColumn<TestTask, String> testCol = new TableColumn("Test");
    testCol.setCellValueFactory(cellData -> cellData.getValue().getTestProperty());

    TableColumn<TestTask, Double> progressCol = new TableColumn("Progress");
    progressCol.setCellValueFactory(new PropertyValueFactory<TestTask, Double>(
        "progress"));
    progressCol
        .setCellFactory(ProgressBarTableCell.<TestTask> forTableColumn());

    table.getColumns().addAll(statusCol, progressCol, testCol);

    BorderPane root = new BorderPane();
    root.setCenter(table);
    primaryStage.setScene(new Scene(root));
    primaryStage.show();

    ExecutorService executor = Executors.newFixedThreadPool(table.getItems().size(), new ThreadFactory() {
      @Override
      public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
      }
    });


    for (TestTask task : table.getItems()) {
      executor.execute(task);
    }
  }

  public static void main(String[] args) {
    launch(args);
  }

  static class TestTask extends Task<Void> {

    private final int waitTime; // milliseconds
    private final int pauseTime; // milliseconds
    private StringProperty test;
    

    public StringProperty getTestProperty() {
		return test;
	}

    public String getTest() {
		return test.get();
	}

	public void setTest(String test) {
		this.test.set(test);
	}

	public static final int NUM_ITERATIONS = 100;

    TestTask(int waitTime, int pauseTime) {
      this.waitTime = waitTime;
      this.pauseTime = pauseTime;
      this.test = new SimpleStringProperty("test");
    }

    @Override
    protected Void call() throws Exception {
      this.updateProgress(ProgressIndicator.INDETERMINATE_PROGRESS, 1);
      this.updateMessage("Waiting...");
      Platform.runLater(() -> this.setTest("Waiting..."));
      Thread.sleep(waitTime);
      this.updateMessage("Running...");
      Platform.runLater(() -> this.setTest("Running..."));
      for (int i = 0; i < NUM_ITERATIONS; i++) {
        updateProgress((1.0 * i) / NUM_ITERATIONS, 1);
        Thread.sleep(pauseTime);
      }
      this.updateMessage("Done");
      Platform.runLater(() -> this.setTest("Done"));
      this.updateProgress(1, 1);
      return null;
    }

  }
}
