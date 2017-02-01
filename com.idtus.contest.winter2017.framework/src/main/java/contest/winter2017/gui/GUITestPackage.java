package contest.winter2017.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import contest.winter2017.Output;
import contest.winter2017.Tester;
import edu.emory.mathcs.backport.java.util.Collections;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Class encapsulting a test run in the GUI interface.
 * 
 * @author ICT-2
 */
public class GUITestPackage {
	/**
	 * TestListPane of this test.
	 */
	private TestListPane testListPane;

	/**
	 * TestInfo GUI component of this test.
	 */
	private TestInfo testInfo;

	/**
	 * MainPane of this test.
	 */
	private MainPane mainPane;

	/**
	 * Tester of this test.
	 */
	private Tester tester;

	/**
	 * Name of this test.
	 */
	private SimpleStringProperty name;

	/**
	 * Jar to test.
	 */
	private File toTest;

	/**
	 * Synchronized list of outputs for this test.
	 */
	private List<Output> outputs = Collections.synchronizedList(new ArrayList<Output>());

	/**
	 * Constructs a GUITestPackage with the given testListPane, name, and file
	 * to test.
	 * 
	 * @param testListPane
	 *            - testListPane to put test info in
	 * @param name
	 *            - name of this test
	 * @param toTest
	 *            - file to test
	 */
	public GUITestPackage(TestListPane testListPane, String name, File toTest) {
		// order of initialization matters
		this.name = new SimpleStringProperty(name);
		this.testListPane = testListPane;
		this.toTest = toTest;
		this.tester = new Tester();
		this.testInfo = new TestInfo(this);
		this.mainPane = new MainPane(this);
	}

	/**
	 * Starts tests for this test.
	 */
	public void startTests() {
		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() {
				GUITestPackage.this.tester.executeBasicTests();
				// GUITestPackage.this.tester.executeSecurityTests();
				return null;
			}
		};
		new Thread(task).start();
	}

	/**
	 * Updates the output for this test.
	 */
	public void updateOutput() {
		while (outputs.size() < this.tester.getOutputs().size()) {
			Output newOutput = this.tester.getOutputs().get(outputs.size());
			outputs.add(newOutput);
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					GUITestPackage.this.mainPane.addOutput(newOutput);
				}
			});
		}
	}

	/**
	 * Returns this test's name.
	 * 
	 * @return StringProperty name for this test
	 */
	public SimpleStringProperty getName() {
		return this.name;
	}

	/**
	 * Sets this test's name.
	 * 
	 * param name - new name for this test
	 */
	public void setName(String name) {
		this.name.setValue(name);
	}

	/**
	 * Returns this test's TestListPane.
	 * 
	 * @return TestListPane parent
	 */
	public TestListPane getTestListPane() {
		return this.testListPane;
	}

	/**
	 * Returns this test's TestInfo.
	 * 
	 * @return this test's TestInfo
	 */
	public TestInfo getTestInfo() {
		return this.testInfo;
	}

	/**
	 * Returns this test's MainPane.
	 * 
	 * @return this test's MainPane
	 */
	public MainPane getMainPane() {
		return this.mainPane;
	}

	/**
	 * Returns this test's Tester.
	 * 
	 * @return this test's Tester
	 */
	public Tester getTester() {
		return this.tester;
	}

	/**
	 * Returns this test's Jar to test.
	 * 
	 * @return this test's Jar to test
	 */
	public File getToTest() {
		return this.toTest;
	}
}

/**
 * VBox used to hold basic info for tests.
 * 
 * @author ICT-2
 */
class TestInfo extends VBox {
	/**
	 * Test to represent.
	 */
	private GUITestPackage test;

	/**
	 * Constructs a TestInfo.
	 */
	public TestInfo(GUITestPackage test) {
		this.test = test;

		// styling and buttons
		setAlignment(Pos.CENTER);
		setStyle("-fx-border-color: lightgray; -fx-border-width: 1;");
		Text name = new Text("");
		name.textProperty().bind(this.test.getName());
		name.setFont(new Font(20));
		Text percent = new Text("0%");
		percent.setFont(new Font(10));
		ProgressBar progressBar = new ProgressBar(0);
		progressBar.setMouseTransparent(true);
		progressBar.setPadding(new Insets(3, 0, 0, 2));

		// keep track of progress
		this.test.getTester().getPercentDone().addListener(new ChangeListener<Number>() {
			/**
			 * Tracks the percent of testing done.
			 * <p>
			 * If required tests are not completed, progress bar shows the
			 * percent completed. If required tests are completed, but the time
			 * goal has not been fulfilled, the progress bar shows
			 * indeterminate.
			 * 
			 * @param observable
			 *            - percent complete being obsesrved
			 * @param oldValue
			 *            - the previous percent complete
			 * @param newValue
			 *            - the new percent complete.
			 */
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				TestInfo.this.test.updateOutput();

				double progress = newValue.doubleValue();
				if (progress <= 1) {
					progressBar.setProgress(progress);
					String progressRep = "" + (100 * progress);
					if (progressRep.length() > 4) {
						progressRep = progressRep.substring(0, 4);
					}
					percent.setText(progressRep + "%");
				} else if (!progressBar.isIndeterminate()) {
					percent.setText("Extra Tests");
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
						}
					});
				}
			}
		});

		getChildren().addAll(name, percent, progressBar);

		// grabs focus if testinfo is selected
		setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (event.getButton() == MouseButton.PRIMARY) {
					TestInfo.this.test.getTestListPane().selectTest(TestInfo.this.test);
				}
			}
		});
	}
}