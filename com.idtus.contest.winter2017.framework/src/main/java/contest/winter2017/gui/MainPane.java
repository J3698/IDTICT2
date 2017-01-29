package contest.winter2017.gui;

import javafx.geometry.Pos;
import javafx.scene.Node;
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

/**
 * TabPane to control testing form.
 * 
 * @author ICT-2
 */
public class MainPane extends TabPane {
	/**
	 * Constructs a MainPane.
	 */
	public MainPane() {
		Tab runTab = new Tab("Run", new RunPane());

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

		Tab parameterTab = new Tab("Parameter Bounds", new ParameterPane());
		getTabs().addAll(runTab, outputTab, parameterTab);
		setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
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

	/**
	 * Constructs a ParameterPane.
	 */
	public ParameterPane() {
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

	/**
	 * Constructs a RunPane.
	 */
	public RunPane() {
		VBox box = new VBox(DEFAULT_SPACE);
		box.setAlignment(Pos.CENTER);

		Button outputPath = new Button("Set Output Path");
		LabeledNode outputPathButton = new LabeledNode("Jacoco Output Path", outputPath);
		Button agentPath = new Button("Set Agent Path");
		LabeledNode agentPathButton = new LabeledNode("Jacoco Agent Path", agentPath);

		TextField tf = new TextField();
		tf.setPrefColumnCount(5);
		LabeledNode nameInput = new LabeledNode("Test Name", tf);
		tf = new TextField();
		tf.setPrefColumnCount(5);
		LabeledNode testsToRunInput = new LabeledNode("Tests to Run", tf);
		tf = new TextField();
		tf.setPrefColumnCount(5);
		LabeledNode timeGoalInput = new LabeledNode("Time Goal", tf);

		Button runButton = new Button("Start Testing");
		runButton.setFont(new Font(15));
		box.getChildren().addAll(outputPathButton, agentPathButton, nameInput);
		box.getChildren().addAll(testsToRunInput, timeGoalInput, runButton);
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