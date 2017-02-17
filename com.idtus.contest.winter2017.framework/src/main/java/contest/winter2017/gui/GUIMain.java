package contest.winter2017.gui;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Random;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Entry point for the GUI version of the testing framework.
 * 
 * @author ICT-2
 */
public class GUIMain extends Application {
	private static String guiID = null;
	private static boolean isRunning = true;

	/**
	 * Starts the GUI.
	 * 
	 * @param stage
	 *            - stage for the GUI
	 */
	public void start(Stage stage) {
		BorderPane pane = new BorderPane();
		TestListPane testLists = new TestListPane(pane);
		pane.setLeft(testLists);
		pane.setCenter(new IntroPane());

		Scene scene = new Scene(pane, 640, 480);
		stage.setScene(scene);
		scene.getWindow().setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				testLists.stopTests();
				isRunning = false;
			}
		});

		new Thread(new MemoryMonitor(this, testLists)).start();

		Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread arg0, Throwable arg1) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						handleException(testLists, arg1);
					}
				});
			}
		});

		stage.setTitle("Java JAR Security Tester");
		stage.show();
	}

	/**
	 * Handles an exception thrown by the application.
	 * 
	 * <p>
	 * Shows an alert with the stack trace of the exception.
	 * 
	 * @param testList
	 *            - test list to pause tests of
	 * @param thrown
	 *            - exception thrown
	 */
	public void handleException(TestListPane testList, Throwable thrown) {
		testList.pauseTests();

		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Exception");
		alert.setHeaderText("An exception occurred. Testing paused.");
		alert.setContentText(
				"Further application funcionality may be compromised. Application restart recommended. To report an exception, please email antiochsanders@gmail.com.\n");

		TextArea area = new TextArea();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		thrown.printStackTrace(ps);
		area.setText(new String(baos.toByteArray()));
		area.setEditable(false);
		area.setWrapText(true);

		alert.getDialogPane().setExpandableContent(area);
		alert.showAndWait();
	}

	/**
	 * Whether this application is continuing to run.
	 * <p>
	 * The memory monitor uses this function to determine when it should kill
	 * itself.
	 * 
	 * @return true if this application is continuing to run, otherwise false
	 */
	public boolean isRunning() {
		return GUIMain.isRunning;
	}

	/**
	 * Returns the ID of this GUI.
	 * <p>
	 * If no ID has been created, a 15 digit random ID is created.
	 * 
	 * @return the ID of this GUI
	 */
	public static String getGuiID() {
		if (guiID != null) {
			return guiID;
		} else {
			Random rng = new Random();
			for (int i = 0; i < 15; i++) {
				guiID += rng.nextInt(10);
			}
		}
		return guiID;
	}
}

/**
 * The introduction pane of the GUI. Shows instructions for initiating a test.
 * 
 * @author ICT-2
 */
class IntroPane extends VBox {
	/**
	 * Constructs an intro pane.
	 */
	public IntroPane() {
		super();
		this.setAlignment(Pos.CENTER);
		Text information = new Text("Welcome to GUI of this Java JAR Security Tester. This center pane will show"
				+ "\ncontent for testing once a jar is chosen to test. To start, drag a jar over the"
				+ "\nspecified area to left, or click the \"Select Jar\" button, and select a jar.");
		information.setTextAlignment(TextAlignment.CENTER);
		information.setFont(new Font(14));
		getChildren().add(information);
	}
}