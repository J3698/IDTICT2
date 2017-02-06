package contest.winter2017.gui;

import java.io.File;
import java.util.Map;
import java.util.Set;

import contest.winter2017.Output;
import contest.winter2017.Tester;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
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
 * TabPane to control testing form.
 * 
 * @author ICT-2
 */
public class MainPane extends TabPane {
	/**
	 * Test of this MainPane.
	 */
	private GUITestPackage test;

	/**
	 * ParameterPane of this MainPane.
	 */
	private ParameterPane parameterPane;

	/**
	 * RunPane of this MainPane.
	 */
	private RunPane runPane;

	/**
	 * TabPane of this MainPane.
	 */
	private TabPane outputPane;

	/**
	 * TextArea for this test's std out.
	 */
	private TextArea stdOutText;

	/**
	 * TextArea for this test's std err.
	 */
	private TextArea stdErrText;

	/**
	 * TextArea for this test's permissions.
	 */
	private TextArea permissionsText;

	/**
	 * Int tests recorded so far.
	 */
	private int testsAdded = 0;

	/**
	 * Constructs a MainPane with the given test.
	 */
	public MainPane(GUITestPackage test) {
		this.test = test;

		// runpane
		this.runPane = new RunPane(this.test);
		Tab runTab = new Tab("Run", this.runPane);

		// three tabs of output pane
		outputPane = new TabPane();
		stdOutText = new TextArea();
		stdOutText.setEditable(false);
		stdOutText.setWrapText(true);
		// stdOutText.setMouseTransparent(true);
		stdOutText.setFocusTraversable(false);
		Tab stdOut = new Tab("Standard Out", stdOutText);
		stdErrText = new TextArea();
		stdErrText.setEditable(false);
		stdErrText.setWrapText(true);
		// stdErrText.setMouseTransparent(true);
		stdErrText.setFocusTraversable(false);
		Tab stdErr = new Tab("Standard Error", stdErrText);
		permissionsText = new TextArea();
		permissionsText.setEditable(false);
		permissionsText.setWrapText(true);
		// permissionsText.setMouseTransparent(true);
		permissionsText.setFocusTraversable(false);
		Tab permissions = new Tab("Permissions", permissionsText);
		outputPane.getTabs().addAll(stdOut, stdErr, permissions);
		outputPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		Tab outputTab = new Tab("Output", outputPane);

		// parameter pane
		this.parameterPane = new ParameterPane();
		Tab parameterTab = new Tab("Parameter Bounds", parameterPane);

		// add undeletable tabs
		getTabs().addAll(runTab, outputTab, parameterTab);
		setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
	}

	/**
	 * Adds an output to this MainPane's output pane.
	 * 
	 * @param output
	 *            - output to add
	 */
	public void addOutput(Output output) {
		this.testsAdded++;
		String prefix = "Test " + this.testsAdded + ":\n";
		this.stdErrText.appendText(prefix + output.getStdErrString() + "\n\n");
		this.stdOutText.appendText(prefix + output.getStdOutString() + "\n\n");
		this.permissionsText.appendText(prefix + output.getPermissionLogString() + "\n\n");
	}

	/**
	 * Returns this MainPane's run pane.
	 * 
	 * @return this MainPane's run pane
	 */
	public RunPane getRunPane() {
		return this.runPane;
	}

	/**
	 * Returns this MainPane's output pane.
	 * 
	 * @return this MainPane's output pane
	 */
	public TabPane getOutputPane() {
		return this.outputPane;
	}

	/**
	 * Returns this MainPane's parameter pane.
	 * 
	 * @return this MainPane's parameter pane
	 */
	public ParameterPane getParameterPane() {
		return this.parameterPane;
	}

	/**
	 * Returns this test's std out text.
	 * 
	 * @return this test's std out text
	 */
	public TextArea getStdOutText() {
		return this.stdOutText;
	}

	/**
	 * Returns this test's std err text.
	 * 
	 * @return this test's std err text
	 */
	public TextArea getStdErrText() {
		return this.stdErrText;
	}

	/**
	 * Returns this test's permissions text.
	 * 
	 * @return this test's permissions text
	 */
	public TextArea getPermissionsText() {
		return this.permissionsText;
	}

	/**
	 * Returns this MainPane's test.
	 * 
	 * @return test of this MainPane
	 */
	public GUITestPackage getTest() {
		return this.test;
	}

}

/**
 * VBox to expose basic test settings and allow running of a test.
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

	// textfieds for several settings
	private TextField name;
	private TextField toRun;
	private TextField timeGoal;

	// file paths for several settings
	private File outputPathFile = null;
	private File jacocoPathFile = null;

	// whether or not several settings are valid
	private boolean validTimeGoal;
	private boolean validTestNumber;
	private boolean validOutputPath;
	private boolean validJacocoPath;

	/**
	 * Run button for this test.
	 */
	private Button runButton;

	/**
	 * Constructs a RunPane.
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
		this.validOutputPath = false;
		Button agentPath = new Button("Set Agent Path");
		LabeledNode agentPathButton = new LabeledNode("Jacoco Agent Path", agentPath);
		agentPath.setStyle("-fx-border-color: red;");
		this.validJacocoPath = false;

		// label name of jar to test
		Text jarName = new Text();
		jarName.setText(test.getToTest().getName());
		jarName.setFont(new Font(20));
		VExternSpace jarNameSpacer = new VExternSpace(jarName, 0, 40);

		// name setting
		name = new TextField();
		name.setPrefColumnCount(5);
		name.setText(this.test.getName().get());
		LabeledNode nameInput = new LabeledNode("Test Name", name);

		// number of test setting
		toRun = new TextField();
		toRun.setPrefColumnCount(5);
		toRun.setText("" + Tester.DEFAULT_BB_TESTS);
		LabeledNode testsToRunInput = new LabeledNode("Tests to Run", toRun);

		// time goal setting
		timeGoal = new TextField();
		timeGoal.setPrefColumnCount(5);
		timeGoal.setText("" + Tester.DEFAULT_TIME_GOAL);
		LabeledNode timeGoalInput = new LabeledNode("Time Goal", timeGoal);

		// run and stop buttons
		this.runButton = new Button("Start Testing");
		runButton.setFont(new Font(15));
		VExternSpace runButtonSpacer = new VExternSpace(runButton, 40, 0);

		// track whether name is valid
		name.setStyle("-fx-border-color: green;");

		// track whether number of tests to run is valid
		toRun.setStyle("-fx-border-color: green;");
		this.validTestNumber = true;

		// track whether time goal is valid
		timeGoal.setStyle("-fx-border-color: green;");
		this.validTimeGoal = true;

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

		box.getChildren().addAll(jarNameSpacer, nameInput, testsToRunInput, timeGoalInput);
		box.getChildren().addAll(outputPathButton, agentPathButton, runButtonSpacer);
		setCenter(box);
	}

	/**
	 * Adds handlers to this component.
	 * 
	 * @param outputPath
	 *            - componen to add handlers to
	 * @param agentPath
	 *            - componen to add handlers to
	 * @param runButton
	 *            - componen to add handlers to
	 */
	public void addHandlers(Button outputPath, Button agentPath) {
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
					// prevent exception from bubbling up,
					// just leave validTimeGoal as false
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

					// ensure options are all correct
					String jarPath = RunPane.this.test.getToTest().getAbsolutePath();
					if (RunPane.this.validOutputPath) {
					} else {
						alert.setContentText("Invalid jar path.");
						alert.showAndWait();
						return;
					}

					String outputPath = null;
					if (RunPane.this.validOutputPath) {
						outputPath = RunPane.this.outputPathFile.getAbsolutePath();
					} else {
						alert.setContentText("Invalid output path.");
						alert.showAndWait();
						return;
					}

					String agentPath = null;
					if (RunPane.this.validJacocoPath) {
						agentPath = RunPane.this.jacocoPathFile.getAbsolutePath();
					} else {
						alert.setContentText("Invalid agent path.");
						alert.showAndWait();
						return;
					}

					String bbTests = null;
					if (RunPane.this.validTestNumber) {
						bbTests = RunPane.this.toRun.getText();
					} else {
						alert.setContentText("Invalid number of tests.");
						alert.showAndWait();
						return;
					}

					String timeGoal = null;
					if (RunPane.this.validTimeGoal) {
						timeGoal = RunPane.this.timeGoal.getText();
					} else {
						alert.setContentText("Invalid time goal.");
						alert.showAndWait();
						return;
					}

					Map testBounds = null;
					if (RunPane.this.test.hasUserTestBounds()) {
						if (!RunPane.this.test.hasValidUserTestBounds()) {
							alert.setContentText("Invalid user defined parameter bounds.");
							alert.showAndWait();
							return;
						} else {
							testBounds = RunPane.this.test.getUserTestBounds();
						}
					}

					boolean quiet = false;
					boolean watchdog = true;

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
					RunPane.this.runButton.setText("Resume Testing");
				} else {
					RunPane.this.test.resumeTests();
					RunPane.this.runButton.setText("Pause Testing");
				}
			}
		});
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