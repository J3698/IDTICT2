package contest.winter2017.gui;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * VBox used to hold basic info for a given test.
 * 
 * This component may be selected to show the test in the main pane.
 * 
 * This component shows the name of the test, a status string for the test, and
 * a progress bar for the test. The text shows the percent of tests done, the
 * fact that extra tests are being run, or "done". The progress bar changes
 * accordingly.
 * 
 * @author ICT-2
 */
public class TestInfo extends StackPane {
	/**
	 * Test to represent.
	 */
	private GUITestPackage test;

	/**
	 * VBox for holding information.
	 */
	private VBox box = new VBox();

	/**
	 * Anchor pane for holding close button.
	 */
	private AnchorPane closePane = new AnchorPane();

	/**
	 * Button for closing a test.
	 */
	private Button closeButton = new Button("X");
	/**
	 * Progress bar for this test.
	 */
	private ProgressBar progressBar = new ProgressBar(0);

	/**
	 * Completion status of this test.
	 */
	private Text percent = new Text("0%");

	/**
	 * Constructs a TestInfo with the given test.
	 */
	public TestInfo(GUITestPackage test) {
		this.test = test;

		// pane for close button AnchorPane
		closePane.setPickOnBounds(false);
		closeButton.setFont(new Font(8));
		AnchorPane.setTopAnchor(closeButton, 0.0);
		AnchorPane.setLeftAnchor(closeButton, 0.0);
		closePane.getChildren().add(closeButton);

		// style editor box this.content.setAlignment(Pos.CENTER);

		// styling and buttons
		box.setAlignment(Pos.CENTER);
		box.setStyle("-fx-border-color: lightgray; -fx-border-width: 1;");
		Text name = new Text("");
		name.textProperty().bind(this.test.getName());
		name.setFont(new Font(20));
		this.percent.setFont(new Font(10));
		this.progressBar.setMouseTransparent(true);
		this.progressBar.setPadding(new Insets(3, 0, 0, 2));

		addHandlers();

		box.getChildren().addAll(name, this.percent, progressBar);
		getChildren().addAll(box, closePane);
	}

	/**
	 * Adds handlers to this component.
	 */
	public void addHandlers() {
		this.closeButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Delete Test");
				alert.setContentText("Are you sure you want to delete this test? This action cannot be undone.");
				alert.showAndWait();
				if (alert.getResult().getButtonData() == ButtonData.OK_DONE) {
					TestInfo.this.test.remove();
				}
			}
		});

		this.test.getTester().getPercentDone().addListener(new ChangeListener<Number>() {
			/**
			 * Tracks the percent of testing done.
			 * <p>
			 * If tests are not completed, progress bar shows the percent
			 * completed. If tests are completed, but the time goal has not been
			 * fulfilled, the progress bar shows indeterminate.
			 * 
			 * @param observable
			 *            - percent complete being observed
			 * @param oldValue
			 *            - the previous percent complete
			 * @param newValue
			 *            - the new percent complete.
			 */
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				TestInfo.this.test.updateOutput();

				// don't update a disabled progress bar
				if (TestInfo.this.progressBar.isDisabled()) {
					if (TestInfo.this.progressBar.isIndeterminate()) {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								TestInfo.this.progressBar.setProgress(0);
							}
						});
					}
					return;
				}

				double progress = newValue.doubleValue();
				if (progress <= 1) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							TestInfo.this.progressBar.setProgress(progress);
							String progressRep = "" + (100 * progress);
							if (progressRep.length() > 4) {
								progressRep = progressRep.substring(0, 4);
							}
							TestInfo.this.percent.setText(progressRep + "%");
						}
					});
				} else if (!TestInfo.this.progressBar.isIndeterminate()) {
					TestInfo.this.percent.setText("Extra Tests");
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							TestInfo.this.progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
						}
					});
				}
			}
		});

		this.box.setOnMouseClicked(new EventHandler<MouseEvent>() {
			/**
			 * Selects this test info's test.
			 * 
			 * @param event
			 *            - nmouse event to handle
			 */
			@Override
			public void handle(MouseEvent event) {
				if (event.getButton() == MouseButton.PRIMARY) {
					TestInfo.this.test.getTestListPane().selectTest(TestInfo.this.test);
				}
			}

		});
	}

	/**
	 * Displays that testing has ended.
	 */
	void endTests() {
		this.progressBar.setProgress(1.0);
		this.percent.setText("Done");
	}

	/**
	 * Returns this test-info's progress bar.
	 * 
	 * @return the progress bar for this test info
	 */
	public ProgressBar getProgressBar() {
		return this.progressBar;
	}

}