package contest.winter2017.gui;

import java.util.Random;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Entry point for the GUI version of testing framework.
 * 
 * @author ICT-2
 */
public class GUIMain extends Application {
	private static String guiID = null;

	/**
	 * Starts the GUI.
	 * 
	 * @param stage
	 *            - stage for the GUI
	 */
	public void start(Stage stage) {
		BorderPane pane = new BorderPane();
		pane.setLeft(new TestListPane(pane));
		Scene scene = new Scene(pane, 640, 480);
		stage.setScene(scene);
		stage.show();
		/*
		 * BorderPane pane = new BorderPane(); pane.setLeft(new
		 * TestListPane(pane)); TestListPane tPane = new TestListPane(pane);
		 * pane.setCenter(new MainPane(new GUITestPackage(tPane, "", new
		 * File(".")))); Scene scene = new Scene(pane, 640, 480);
		 * stage.setScene(scene); stage.show();
		 */
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
