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

public class MemoryMonitor implements Runnable {
	private GUIMain guiMain;
	private TestListPane testLists;
	private boolean showing = false;
	private boolean twoFifthsShown = false;
	private boolean quarterShown = false;

	public MemoryMonitor(GUIMain guiMain, TestListPane testLists) {
		this.testLists = testLists;
		this.guiMain = guiMain;
	}

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

class MemoryDialog extends Dialog<ButtonType> {
	private static MemoryDialog singleton = null;
	private CheckBox box = new CheckBox("Don't Show Again");
	private Label text = new Label();

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
	 * 
	 * @param thresh
	 *            - memory threshold reached
	 * @return true if tests should be paused, false if they should not be
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
