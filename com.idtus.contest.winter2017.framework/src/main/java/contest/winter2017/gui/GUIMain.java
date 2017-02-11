package contest.winter2017.gui;

import java.math.BigDecimal;
import java.util.Random;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
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
		Scene scene = new Scene(pane, 640, 480);
		stage.setScene(scene);
		scene.getWindow().setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				testLists.stopTests();
				isRunning = false;
			}
		});

		stage.show();

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (GUIMain.this.isRunning) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException ie) {
					}
					Runtime runtime = Runtime.getRuntime();
					BigDecimal free = new BigDecimal(runtime.freeMemory() + ".00");
					BigDecimal total = new BigDecimal(runtime.totalMemory() + ".00");
					BigDecimal percent = free.divide(total, BigDecimal.ROUND_HALF_UP);
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							System.out.println(percent);
						}
					});
				}
			}

		}).start();
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
