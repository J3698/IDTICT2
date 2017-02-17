package contest.winter2017.gui;

import java.math.BigDecimal;
import java.util.Optional;

import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 * This class monitors free allocated memory for the JVM. Warnings are displayed
 * if memory is low.
 * 
 * @author ICT-2
 */
public class MemoryMonitor implements Runnable {
	/**
	 * The application to monitor.
	 */
	private GUIMain guiMain;

	/**
	 * The test list pane for this application
	 */
	private TestListPane testLists;

	/**
	 * Whether the security monitor is showing a warning.
	 */
	private boolean showing = false;

	/**
	 * Whether the second memory warning has been shown.
	 */
	private boolean twoFifthsShown = false;

	/**
	 * Whether the first memory warning has been shown.
	 */
	private boolean quarterShown = false;

	/**
	 * Constructs a memory monitor with the given application and test list
	 * pane.
	 * 
	 * @param guiMain
	 *            - application to monitor
	 * @param testLists
	 *            - test list pane for this application
	 */
	public MemoryMonitor(GUIMain guiMain, TestListPane testLists) {
		this.testLists = testLists;
		this.guiMain = guiMain;
	}

	/**
	 * Run method for the thread of this memory monitor.
	 */
	@Override
	public void run() {
		while (this.guiMain.isRunning()) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
			Runtime runtime = Runtime.getRuntime();
			BigDecimal free = new BigDecimal(runtime.freeMemory() + ".00");
			BigDecimal total = new BigDecimal(runtime.totalMemory() + ".00");
			BigDecimal percent = free.divide(total, BigDecimal.ROUND_HALF_UP);
			double doublePercent = percent.doubleValue();
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					if (showing) {
						return;
					}

					boolean pause = false;
					if (doublePercent < 0.25) {
						if (!quarterShown) {
							quarterShown = true;
							showing = true;
							pause = MemoryDialog.show(25);
							showing = false;
						}
					} else if (doublePercent < 0.4) {
						if (!twoFifthsShown) {
							twoFifthsShown = true;
							showing = true;
							pause = MemoryDialog.show(40);
							showing = false;
						}
					}

					if (pause) {
						MemoryMonitor.this.testLists.pauseTests();
					}
				}
			});
		}
	}
}

/**
 * This class is an alert for when memory is low. Only one is ever created, thus
 * this class is mostly static.
 * 
 * @author ICT-2
 */
class MemoryDialog extends Dialog<ButtonType> {
	/**
	 * The singleton memory dialog for this class.
	 */
	private static MemoryDialog singleton = null;

	/**
	 * The "don't show again" checkbox for this dialog
	 */
	private CheckBox box = new CheckBox("Don't Show Again");

	/**
	 * The information text for this dialog.
	 */
	private Label text = new Label();

	/**
	 * Constructs a memory dialog. Is private to prevent this class being used
	 * in a non-static manner.
	 */
	private MemoryDialog() {
		setTitle("Memory Warning");

		DialogPane dialogPane = getDialogPane();
		dialogPane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
		VBox content = new VBox(5);
		text.setFont(new Font(15));
		content.getChildren().addAll(text, box);
		dialogPane.setContent(content);
	}

	/**
	 * Shows this memory dialog.
	 * <p>
	 * If don't show again is selected, a dialog is not shown.
	 * 
	 * @param thresh
	 *            - memory threshold reached
	 * @return true if tests should be paused, otherwise false
	 */
	public static boolean show(double thresh) {
		if (MemoryDialog.singleton == null) {
			MemoryDialog.singleton = new MemoryDialog();
		}

		if (MemoryDialog.singleton.box.isSelected()) {
			return false;
		}

		MemoryDialog.singleton.text.setText("Free allocated memory has dropped below " + thresh
				+ "%. Application may display\nunexpected behavior when memory is low. Pause tests?");

		Optional<ButtonType> val = MemoryDialog.singleton.showAndWait();
		if (val.isPresent() && val.get() == ButtonType.OK) {
			return true;
		} else {
			return false;
		}
	}
}
