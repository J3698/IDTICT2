package contest.winter2017.gui;

import java.io.File;
import java.util.Set;

import contest.winter2017.Tester;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
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
	private Tester tester;
	private GUITestPackage test;

	/**
	 * Constructs a MainPane.
	 */
	public MainPane(GUITestPackage test) {
		this.test = test;
		tester = new Tester();

		Tab runTab = new Tab("Run", new RunPane(this.test));

		TabPane output = new TabPane();

		TextArea text = new TextArea();
		text.setEditable(false);
		text.setWrapText(true);
		text.setMouseTransparent(true);
		text.setFocusTraversable(false);
		Tab stdOut = new Tab("Standard Out", text);
		text = new TextArea();
		text.setEditable(false);
		text.setWrapText(true);
		text.setMouseTransparent(true);
		text.setFocusTraversable(false);
		Tab stdErr = new Tab("Standard Error", text);
		text = new TextArea();
		text.setEditable(false);
		text.setWrapText(true);
		text.setMouseTransparent(true);
		text.setFocusTraversable(false);
		Tab permissions = new Tab("Permissions", text);

		output.getTabs().addAll(stdOut, stdErr, permissions);
		output.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

		Tab outputTab = new Tab("Output", output);

		Tab parameterTab = new Tab("Parameter Bounds", new ParameterPane(this.test));
		getTabs().addAll(runTab, outputTab, parameterTab);
		setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
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
 * Pane to change parameters.
 */
class ParameterPane extends ScrollPane {
	/**
	 * Content for this pane.
	 */
	private VBox content;
	private GUITestPackage test;

	/**
	 * Constructs a ParameterPane.
	 */
	public ParameterPane(GUITestPackage test) {
		this.test = test;
		setFitToWidth(true);
		this.content = new VBox();
		setContent(this.content);
		this.content.setAlignment(Pos.CENTER);

		CheckBox box = new CheckBox("Use Parameter Test Bounds from Jar");
		VExternSpace testBoundsBox = new VExternSpace(box, 20, 20);
		this.content.getChildren().add(testBoundsBox);

		for (int i = 0; i < 2; i++)
			this.content.getChildren().add(new TitledPane("Parameter #", new HBox()));

		Button b = new Button("New Parameter");
		b.setFont(new Font(15));
		VExternSpace addParamButton = new VExternSpace(b, 20, 20);
		this.content.getChildren().add(addParamButton);
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
	private TextField name;
	private TextField toRun;
	private TextField timeGoal;
	private File outputPathFile = null;
	private File jacocoPathFile = null;
	private GUITestPackage test;

	private boolean validName;
	private boolean validTimeGoal;
	private boolean validTestNumber;
	private boolean validOutputPath;
	private boolean validJacocoPath;

	/**
	 * Constructs a RunPane.
	 */
	public RunPane(GUITestPackage test) {
		this.test = test;
		VBox box = new VBox(DEFAULT_SPACE);
		box.setAlignment(Pos.CENTER);

		Button outputPath = new Button("Set Output Path");
		LabeledNode outputPathButton = new LabeledNode("Jacoco Output Path", outputPath);
		Button agentPath = new Button("Set Agent Path");
		LabeledNode agentPathButton = new LabeledNode("Jacoco Agent Path", agentPath);

		Text jarName = new Text();
		jarName.setText(test.getToTest().getName());
		jarName.setFont(new Font(20));
		VExternSpace jarNameSpacer = new VExternSpace(jarName, 0, 60);

		name = new TextField();
		name.setPrefColumnCount(5);
		name.setText(this.test.getName().get());
		LabeledNode nameInput = new LabeledNode("Test Name", name);
		toRun = new TextField();
		toRun.setPrefColumnCount(5);
		toRun.setText("" + Tester.DEFAULT_BB_TESTS);
		LabeledNode testsToRunInput = new LabeledNode("Tests to Run", toRun);
		timeGoal = new TextField();
		timeGoal.setPrefColumnCount(5);
		timeGoal.setText("" + Tester.DEFAULT_TIME_GOAL);
		LabeledNode timeGoalInput = new LabeledNode("Time Goal", timeGoal);

		Button runButton = new Button("Start Testing");
		runButton.setFont(new Font(15));

		name.setStyle("-fx-border-color: green;");
		this.validName = false;
		name.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> arg0, String oldVal, String newVal) {
				newVal = newVal.trim();
				Set<String> usedNames = RunPane.this.test.getTestListPane().getTestNames();
				if (!usedNames.contains(newVal) && !newVal.equals("")) {
					RunPane.this.test.setName(newVal);
					usedNames.remove(oldVal);
					RunPane.this.validName = true;
					name.setStyle("-fx-border-color: green;");
				} else {
					RunPane.this.validName = true;
					name.setStyle("-fx-border-color: red;");
				}
			}
		});

		toRun.setStyle("-fx-border-color: green;");
		this.validTestNumber = true;
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

		timeGoal.setStyle("-fx-border-color: green;");
		this.validTimeGoal = true;
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

		outputPath.setStyle("-fx-border-color: red;");
		this.validOutputPath = false;
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
					if (path.length() > 30) {
						path = path.substring(path.length() - 1 - 30);
						outputPath.setText("..." + path.substring(3));
					} else {
						outputPath.setText(path);
					}
				}
			}
		});

		agentPath.setStyle("-fx-border-color: red;");
		this.validJacocoPath = false;
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
					if (path.length() > 30) {
						path = path.substring(path.length() - 1 - 30);
						agentPath.setText("..." + path.substring(3));
					} else {
						agentPath.setText(path);
					}
				}
			}
		});

		runButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				runButton.setDisable(true);

				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("Tester Failed to Initialize");

				String jarPath = RunPane.this.test.getToTest().getAbsolutePath();
				if (RunPane.this.validOutputPath) {
				} else {
					alert.setContentText("Invalid jar path.");
					alert.showAndWait();
				}

				String outputPath = null;
				if (RunPane.this.validOutputPath) {
					outputPath = RunPane.this.outputPathFile.getAbsolutePath();
				} else {
					alert.setContentText("Invalid output path.");
					alert.showAndWait();
				}

				String agentPath = null;
				if (RunPane.this.validJacocoPath) {
					agentPath = RunPane.this.jacocoPathFile.getAbsolutePath();
				} else {
					alert.setContentText("Invalid agent path.");
					alert.showAndWait();
				}

				String bbTests = null;
				if (RunPane.this.validTestNumber) {
					bbTests = RunPane.this.toRun.getText();
				} else {
					alert.setContentText("Invalid number of tests.");
					alert.showAndWait();
				}

				String timeGoal = null;
				if (RunPane.this.validTimeGoal) {
					timeGoal = RunPane.this.timeGoal.getText();
				} else {
					alert.setContentText("Invalid time goal.");
					alert.showAndWait();
				}

				String toolChain = "false";

				if (!RunPane.this.test.getTester().init(jarPath, outputPath, agentPath, bbTests, timeGoal, toolChain)) {
					alert.setContentText("Unknown initialization error.");
					alert.showAndWait();
					runButton.setDisable(false);
				} else {
					test.startTests();
					runButton.setDisable(true);
				}
			}
		});

		box.getChildren().addAll(jarNameSpacer, nameInput, testsToRunInput, timeGoalInput);
		box.getChildren().addAll(outputPathButton, agentPathButton, runButton);
		setCenter(box);
	}
}

/**
 * HBox to hold a label and node. This component steals horizontal space.
 * 
 * @author ICT-2
 */
class LabeledNode extends HBox {
	/**
	 * Int default space between elements.
	 */
	private static final int DEFAULT_SPACE = 10;

	/**
	 * Label for this labeled thing.
	 */
	private Label label;

	/**
	 * Node for this labeled node.
	 */
	private Node node;

	/**
	 * Constructs a LabeledNode with the specified label and node.
	 * 
	 * @param label
	 *            - label to use
	 * @param node
	 *            - node to use
	 */
	public LabeledNode(String label, Node node) {
		super(DEFAULT_SPACE);
		this.node = node;
		setAlignment(Pos.CENTER);
		this.label = new Label(label);
		getChildren().addAll(this.label, this.node);
	}

	/**
	 * Returns this LabeledTextField's label.
	 * 
	 * @return label of this labeled text-field
	 */
	public Label getLabel() {
		return this.label;
	}

	/**
	 * Returns this LabeledTextField's text field.
	 * 
	 * @return text field of this labeled text-field
	 */
	public Node getNode() {
		return this.node;
	}
}