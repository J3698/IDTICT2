package contest.winter2017.gui;

import java.io.File;
import java.util.Map;
import java.util.Set;

import contest.winter2017.Tester;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

/**
 * VBox to expose basic functionality of a tester.
 * 
 * This component shows several settings. It provides defaults for several, and
 * attempts to validate settings before tests can be run. The settings are as
 * follows:
 * 
 * <ul>
 * <li>Jacoco output path - where to output jacoco code coverage results</li>
 * <li>Jacoco agent path - path of jacoco code coverage agent jar</li>
 * <li>time goal - goal in minutes to finish tests in</li>
 * <li>number of tests to run</li>
 * <li>name of the test</li>
 * </ul>
 * 
 * This component also displays the name of the jar to test, and has controls
 * for starting, stoping, and resuming testing.
 * 
 * @author ICT-2
 */
class RunPane extends BorderPane {
	/**
	 * Int default space between elements.
	 */
	private static final int DEFAULT_SPACE = 10;

	/**
	 * Test for this RunPane.
	 */
	private GUITestPackage test;

	/**
	 * Text showing percent of jar under test covered.
	 */
	private Text percentageText = new Text("0% Covered");

	/**
	 * Text field for setting the name of this test.
	 */
	private TextField name = new TextField();

	/**
	 * Text field for setting the number of tests to run.
	 */
	private TextField toRun = new TextField();

	/**
	 * Text field for setting the time goal of this test.
	 */
	private TextField timeGoal = new TextField();

	/**
	 * CHeckbox for whether to watch for security permissions.
	 */
	private LabeledNode permissionCheckbox = new LabeledNode("Monitor Permissions", new CheckBox());

	/**
	 * File path to save jacoco output to.
	 */
	private File outputPathFile = null;

	/**
	 * File path for jacoco agent.
	 */
	private File jacocoPathFile = null;

	/**
	 * Whether the time goal is valid.
	 */
	private boolean validTimeGoal = true;

	/**
	 * Whether the number of tests is valid.
	 */
	private boolean validTestNumber = true;

	/**
	 * Whether the jacoco output path.
	 */
	private boolean validOutputPath = false;

	/**
	 * Whether the jacoco agent path is valid.
	 */
	private boolean validJacocoPath = false;

	/**
	 * Run button for this test.
	 */
	private Button runButton = new Button("Start Testing");

	/**
	 * Constructs a run pane with the given test.
	 * 
	 * @param test
	 *            - test for this run pane
	 */
	public RunPane(GUITestPackage test) {
		this.test = test;

		// styling
		VBox box = new VBox(DEFAULT_SPACE);
		box.setAlignment(Pos.CENTER);

		// buttons for output and agent path
		Button outputPath = new Button("Set Output Path");
		LabeledNode outputPathButton = new LabeledNode("Jacoco Output Path", outputPath);
		outputPath.setStyle("-fx-border-color: red;");
		Button agentPath = new Button("Set Agent Path");
		LabeledNode agentPathButton = new LabeledNode("Jacoco Agent Path", agentPath);
		agentPath.setStyle("-fx-border-color: red;");

		// label name of jar to test
		Text jarName = new Text();
		jarName.setText(test.getToTest().getName());
		jarName.setFont(new Font(20));
		VExternSpace jarNameSpacer = new VExternSpace(jarName, 0, 1);

		VExternSpace percentageTextSpacer = new VExternSpace(percentageText, 0, 45);

		// name setting
		name.setPrefColumnCount(5);
		name.setText(this.test.getName().get());
		LabeledNode nameInput = new LabeledNode("Test Name", name);

		// number of test setting
		toRun.setPrefColumnCount(5);
		toRun.setText("" + Tester.DEFAULT_BB_TESTS);
		LabeledNode testsToRunInput = new LabeledNode("Tests to Run", toRun);

		// time goal setting
		timeGoal.setPrefColumnCount(5);
		timeGoal.setText("" + Tester.DEFAULT_TIME_GOAL);
		LabeledNode timeGoalInput = new LabeledNode("Time Goal", timeGoal);

		// checkbox default
		((CheckBox) this.permissionCheckbox.getNode()).setSelected(true);

		// run and stop buttons
		runButton.setFont(new Font(15));
		VExternSpace runButtonSpacer = new VExternSpace(runButton, 40, 0);

		// these fields are initally valid
		name.setStyle("-fx-border-color: green;");
		toRun.setStyle("-fx-border-color: green;");
		timeGoal.setStyle("-fx-border-color: green;");

		// default path for jacoco output
		File defaultFile = new File(".");
		if (defaultFile.exists()) {
			outputPathFile = defaultFile;
			outputPath.setStyle("-fx-border-color: green;");
			outputPath.setText("Current Folder");
			this.validOutputPath = true;
		}

		// default path for jacoco agent
		defaultFile = new File("C:\\idt_contest\\jacoco\\lib\\jacocoagent.jar");
		if (defaultFile.exists()) {
			this.validJacocoPath = true;
			agentPath.setStyle("-fx-border-color: green;");
			this.jacocoPathFile = defaultFile;
			agentPath.setText(clipName(defaultFile.getAbsolutePath()));
		}

		addHandlers(outputPath, agentPath);

		box.getChildren().addAll(jarNameSpacer, percentageTextSpacer, nameInput, testsToRunInput, timeGoalInput);
		box.getChildren().addAll(permissionCheckbox, outputPathButton, agentPathButton, runButtonSpacer);
		setCenter(box);
	}

	/**
	 * Adds handlers to this component.
	 * 
	 * @param outputPath
	 *            - component to add handlers to
	 * @param agentPath
	 *            - component to add handlers to
	 * @param runButton
	 *            - component to add handlers to
	 */
	public void addHandlers(Button outputPath, Button agentPath) {
		this.test.getTester().getPercentDone().addListener(new ChangeListener<Number>() {
			private long changes = 0;

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				changes++;
				if (changes % 3 == 0) {
					String percent = "" + RunPane.this.test.getTester().generateSummaryCodeCoverageResults();
					if (percent.length() > 4) {
						percent = percent.substring(0, 4);
					}
					RunPane.this.percentageText.setText(percent + "% Covered");
				}
			}
		});

		name.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> arg0, String oldVal, String newVal) {
				newVal = newVal.trim();
				Set<String> usedNames = RunPane.this.test.getTestListPane().getTestNames();
				if (!usedNames.contains(newVal) && !newVal.equals("")) {
					RunPane.this.test.setName(newVal);
					usedNames.remove(oldVal);
					name.setStyle("-fx-border-color: green;");
				} else {
					name.setStyle("-fx-border-color: red;");
				}
			}
		});

		toRun.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> arg0, String oldVal, String newVal) {
				RunPane.this.validTestNumber = false;
				try {
					if (Integer.parseInt(newVal.trim()) >= Tester.MIN_BB_TESTS) {
						RunPane.this.validTestNumber = true;
					}
				} catch (NumberFormatException e) {
					// prevent exception from bubbling up
				}

				if (RunPane.this.validTestNumber) {
					toRun.setStyle("-fx-border-color: green;");
				} else {
					toRun.setStyle("-fx-border-color: red;");
				}
			}
		});

		timeGoal.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> arg0, String oldVal, String newVal) {
				RunPane.this.validTimeGoal = false;
				try {
					if (Integer.parseInt(newVal.trim()) >= Tester.MIN_TIME_GOAL) {
						RunPane.this.validTimeGoal = true;
					}
				} catch (NumberFormatException e) {
					// prevent exception from bubbling up
				}

				if (RunPane.this.validTimeGoal) {
					timeGoal.setStyle("-fx-border-color: green;");
				} else {
					timeGoal.setStyle("-fx-border-color: red;");
				}
			}
		});

		// get a directory for the jacoco output path
		outputPath.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				DirectoryChooser dc = new DirectoryChooser();
				dc.setTitle("Choose a Jacoco Output Path");
				Window window = RunPane.this.getScene().getWindow();
				RunPane.this.outputPathFile = dc.showDialog(window);
				if (RunPane.this.outputPathFile != null && RunPane.this.outputPathFile.exists()) {
					outputPath.setStyle("-fx-border-color: green;");
					RunPane.this.validOutputPath = true;
					String path = RunPane.this.outputPathFile.getAbsolutePath();
					outputPath.setText(clipName(path));
				}
			}
		});

		// get a jacoco agent jar
		agentPath.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				FileChooser fc = new FileChooser();
				fc.setTitle("Choose a Jacoco Jar Agent");
				fc.getExtensionFilters().addAll(new ExtensionFilter("Java Jar Files", "*.jar"));
				Window window = RunPane.this.getScene().getWindow();
				RunPane.this.jacocoPathFile = fc.showOpenDialog(window);
				if (jacocoPathFile != null && jacocoPathFile.exists()) {
					agentPath.setStyle("-fx-border-color: green;");
					RunPane.this.validJacocoPath = true;
					String path = RunPane.this.jacocoPathFile.getAbsolutePath();
					agentPath.setText(clipName(path));
				}
			}
		});

		// run the tests
		RunPane.this.runButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				if (RunPane.this.runButton.getText().equals("Start Testing")) {
					// prepare an error alert
					Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.setTitle("Tester Failed to Initialize");
					alert.setContentText("");

					Map<?, ?> testBounds = null;
					// ensure options are all correct
					if (!RunPane.this.validOutputPath) {
						alert.setContentText("Invalid jar path.");
					} else if (!RunPane.this.validOutputPath) {
						alert.setContentText("Invalid output path.");
					} else if (!RunPane.this.validJacocoPath) {
						alert.setContentText("Invalid agent path.");
					} else if (!RunPane.this.validTestNumber) {
						alert.setContentText("Invalid number of tests.");
					} else if (!RunPane.this.validTimeGoal) {
						alert.setContentText("Invalid time goal.");
					} else if (RunPane.this.test.hasUserTestBounds()) {
						if (!RunPane.this.test.hasValidUserTestBounds()) {
							alert.setContentText("Invalid user defined parameter bounds.");
						} else {
							testBounds = RunPane.this.test.getUserTestBounds();
						}
					}

					// show potential errors
					if (!alert.getContentText().equals("")) {
						alert.showAndWait();
						return;
					}

					String jarPath = RunPane.this.test.getToTest().getAbsolutePath();
					String outputPath = RunPane.this.outputPathFile.getAbsolutePath();
					String agentPath = RunPane.this.jacocoPathFile.getAbsolutePath();
					String bbTests = RunPane.this.toRun.getText();
					String timeGoal = RunPane.this.timeGoal.getText();
					boolean quiet = true;
					CheckBox cb = (CheckBox) RunPane.this.permissionCheckbox.getNode();
					boolean watchdog = cb.isSelected();

					// initialize and run tester
					if (!RunPane.this.test.getTester().init(testBounds, jarPath, outputPath, agentPath, bbTests,
							timeGoal, GUIMain.getGuiID(), quiet, watchdog)) {
						alert.setContentText("Unknown initialization error.");
						alert.showAndWait();
						RunPane.this.runButton.setDisable(false);
					} else {
						RunPane.this.test.startTests();
						RunPane.this.runButton.setText("Pause Testing");
					}
				} else if (RunPane.this.runButton.getText().equals("Pause Testing")) {
					RunPane.this.test.pauseTests();
				} else {
					RunPane.this.test.resumeTests();
				}
			}
		});
	}

	/**
	 * Sets the run button text to paused.
	 */
	public void setPaused() {
		RunPane.this.runButton.setText("Resume Testing");
	}

	/**
	 * Sets the run button text to resumed.
	 */
	public void setResumed() {
		RunPane.this.runButton.setText("Pause Testing");
	}

	/**
	 * Disables run button.
	 */
	public void endTests() {
		this.runButton.setDisable(true);
	}

	/**
	 * Clips a path name and prefixes it with ellipses.
	 * 
	 * @param name
	 *            - name to clip
	 * @return the clipped name
	 */
	public String clipName(String name) {
		if (name.length() > 30) {
			name = name.substring(name.length() - 1 - 30);
			return "..." + name.substring(3);
		} else {
			return name;
		}
	}
}