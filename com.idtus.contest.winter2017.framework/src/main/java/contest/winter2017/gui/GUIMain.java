package contest.winter2017.gui;

import java.util.Random;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Entry point for the GUI version of testing framework.
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

		stage.setTitle("Java JAR Security Tester");
		stage.show();

	}

	public boolean isRunning() {
		return GUIMain.isRunning;
	}

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

class IntroPane extends VBox {
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