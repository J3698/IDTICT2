package contest.winter2017.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import contest.winter2017.Output;
import contest.winter2017.Tester;
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
 * Class encapsulating a test run in the GUI interface.
 * 
 * This class is primarily responsible for encapsulating the main pane, tester,
 * and test info for a given test.
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
	private Tester tester = new Tester();

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

	private boolean testsKilled = false;

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
				GUITestPackage.this.tester.executeSecurityTests();
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						GUITestPackage.this.testInfo.endTests();
						GUITestPackage.this.mainPane.getRunPane().endTests();
					}
				});
				return null;
			}
		};
		new Thread(task).start();
	}

	/**
	 * Resumes tests for this test.
	 */
	public void resumeTests() {
		GUITestPackage.this.tester.setPaused(false);
		this.testInfo.getProgressBar().setDisable(false);
	}

	/**
	 * Pauses tests for this test.
	 */
	public void pauseTests() {
		GUITestPackage.this.tester.setPaused(true);
		this.testInfo.getProgressBar().setDisable(true);
	}

	/**
	 * Ungracefully Kills testing for this test.
	 */
	public void killTests() {
		this.testsKilled = true;
		this.tester.killTests();
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
	 * Returns whether user bounds are to be used.
	 * 
	 * @return true if user bounds are to be used, false if they are not
	 */
	public boolean hasUserTestBounds() {
		return this.getMainPane().getParameterPane().hasUserTestBounds();
	}

	/**
	 * Returns whether user bounds valid.
	 * 
	 * @return true if user bounds are valid, false if they are not
	 */
	public boolean hasValidUserTestBounds() {
		ParameterPane pane = this.getMainPane().getParameterPane();
		return (pane.getFormatBuilder().getCurrentError() == null);
	}

	/**
	 * Returns user defined test bounds if they exist.
	 * 
	 * @return user defined test bounds or null if they are not defined
	 */
	@SuppressWarnings("rawtypes")
	public Map getUserTestBounds() {
		if (this.getMainPane().getParameterPane().hasUserTestBounds()) {
			FormatBuilder builder = this.getMainPane().getParameterPane().getFormatBuilder();
			List<ParameterEditor> editors = builder.getParameterEditors();
			Map<Object, Object> parameters = new HashMap<Object, Object>();
			if (builder.isDynamic()) {
				for (ParameterEditor editor : editors) {
					Map<Object, Object> parameter = new HashMap<Object, Object>();
					// put dynamic options
					parameter.put("optional", new Boolean(editor.isOptional()));

					List<FormatString> formatStrings = editor.getFormatStrings();
					if (formatStrings.size() == 1) {
						// handle non enumerations
						FormatString formatString = formatStrings.get(0);
						parameter.put("format", formatString.getFormatString());
						parameter.put("min", formatString.getMin());
						parameter.put("max", formatString.getMax());
						parameters.put(editor.getRegexKey(), parameter);
					} else {
						// handle enumerations
						List<String> enumeratedValues = new ArrayList<String>();
						List<Object> minimums = new ArrayList<Object>();
						List<Object> maximums = new ArrayList<Object>();
						for (FormatString formatString : formatStrings) {
							enumeratedValues.add(formatString.getFormatString());
							minimums.add(formatString.getMin());
							maximums.add(formatString.getMax());
						}
						parameter.put("enumerated values", enumeratedValues);
						parameter.put("min", minimums);
						parameter.put("max", maximums);
						parameters.put(editor.getRegexKey(), parameter);
					}
				}
			} else {
				List<Map> fixedParameterList = new ArrayList<Map>();
				for (ParameterEditor editor : editors) {
					Map<Object, Object> parameter = new HashMap<Object, Object>();
					FormatString formatString = editor.getFormatStrings().get(0);
					parameter.put("format", formatString.getFormatString());
					parameter.put("min", formatString.getMin());
					parameter.put("max", formatString.getMax());
					fixedParameterList.add(parameter);
				}
				parameters.put("fixed parameter list", fixedParameterList);
			}
			return parameters;
		} else {
			return null;
		}
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
class TestInfo extends VBox {
	/**
	 * Test to represent.
	 */
	private GUITestPackage test;

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

		// styling and buttons
		setAlignment(Pos.CENTER);
		setStyle("-fx-border-color: lightgray; -fx-border-width: 1;");
		Text name = new Text("");
		name.textProperty().bind(this.test.getName());
		name.setFont(new Font(20));
		this.percent.setFont(new Font(10));
		this.progressBar.setMouseTransparent(true);
		this.progressBar.setPadding(new Insets(3, 0, 0, 2));

		addHandlers();

		getChildren().addAll(name, this.percent, progressBar);
	}

	/**
	 * Adds handlers to this component.
	 */
	public void addHandlers() {
		// keep track of progress
		this.test.getTester().getPercentDone().addListener(new ChangeListener<Number>() {
			/**
			 * Tracks the percent of testing done.
			 * <p>
			 * If required tests are not completed, progress bar shows the
			 * percent completed. If required tests are completed, but the time
			 * goal has not been fulfilled, the progress bar shows indeterminate
			 * (bounces back and forth).
			 * 
			 * @param observable
			 *            - percent complete being obsesrved
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

	/**
	 * Signify that testing has ended.
	 */
	void endTests() {
		this.progressBar.setProgress(1.0);
		this.percent.setText("Done");
	}

	/**
	 * Returns this test info's progress bar.
	 * 
	 * @return progress bar for this test info
	 */
	public ProgressBar getProgressBar() {
		return this.progressBar;
	}

}