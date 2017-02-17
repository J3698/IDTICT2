package contest.winter2017.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

/**
 * ScrollPane to hold the test-infos.
 * 
 * This component is a scrollable pane containing test infos which show
 * information about different tests. Test infos can be selected to show a test
 * in the main pane.The last component in the scroll pane allows for adding
 * tests.
 * 
 * @author ICT-2
 */
class TestListPane extends ScrollPane {
	/**
	 * Int width of the scroll pane.
	 */
	public static final double WIDTH = 150;

	/**
	 * Double spacing between components in the test list pane.
	 */
	private static final double SPACING = 0;

	/**
	 * Main pane of the GUI.
	 */
	private BorderPane mainPane;

	/**
	 * List of tests for the GUI.
	 */
	private List<GUITestPackage> tests = new ArrayList<GUITestPackage>();

	/**
	 * Set of names for the tests.
	 */
	private Set<String> testNames = new HashSet<String>();

	/**
	 * Int position where to place new test info components.
	 */
	private int infoAdderIndex;

	/**
	 * Content pane for test info.
	 */
	private VBox contentPane = new VBox(SPACING);

	/**
	 * Constructs a test list pane with the specified parent border pane.
	 */
	public TestListPane(BorderPane mainPane) {
		this.mainPane = mainPane;

		setMinWidth(WIDTH);
		setFitToWidth(true);
		setHbarPolicy(ScrollBarPolicy.NEVER);
		setVbarPolicy(ScrollBarPolicy.ALWAYS);
		setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
		setContent(this.contentPane);

		infoAdderIndex = this.contentPane.getChildren().size() - 1;
		makeTestAdder();
	}

	/**
	 * Makes the component used to add more tests.
	 */
	public void makeTestAdder() {
		VBox adder = new VBox();

		// styling and buttons
		adder.setAlignment(Pos.CENTER);
		adder.setStyle("-fx-border-color: lightgray; -fx-border-width: 1;");
		Button addButton = new Button("Select Jar");
		Text drag = new Text("Or Drag and Drop Jar");
		drag.setFont(new Font(10));

		addHandlers(addButton, adder);

		adder.getChildren().addAll(new VExternSpace(addButton, 13, 4), new VExternSpace(drag, 4, 13));
		this.contentPane.getChildren().add(adder);
	}

	/**
	 * Adds handlers to this component.
	 * 
	 * @param addButton
	 *            - component to add handlers to
	 * @param adder
	 *            - component to add handlers to
	 */
	public void addHandlers(Button addButton, VBox adder) {
		// manually select a jar
		addButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				FileChooser fc = new FileChooser();
				fc.setTitle("Choose a Jar");
				fc.getExtensionFilters().addAll(new ExtensionFilter("Java Jar Files", "*.jar"));
				Window window = TestListPane.this.getScene().getWindow();
				File selected = fc.showOpenDialog(window);
				if (selected != null && selected.exists()) {
					TestListPane.this.addTest(selected);
				}
			}
		});

		// light up if an applicable jar is dragged over
		adder.setOnDragEntered(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				Dragboard db = event.getDragboard();
				if (db.hasFiles() && db.getFiles().size() == 1) {
					if (db.getFiles().get(0).getPath().endsWith(".jar")) {
						adder.setStyle("-fx-border-color: #039ED3; -fx-border-width: 2;");
					}
				}

				event.consume();
			}
		});

		// accept jars which are dragged over
		adder.setOnDragOver(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				Dragboard db = event.getDragboard();
				if (db.hasFiles() && db.getFiles().size() == 1) {
					if (db.getFiles().get(0).getPath().endsWith(".jar")) {
						event.acceptTransferModes(TransferMode.COPY);
					}
				}

				event.consume();
			}
		});

		// remove light up when a jar is no longer dragged over
		adder.setOnDragExited(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				if (event.isAccepted()) {
					adder.setStyle("-fx-border-color: lightgray; -fx-border-width: 1;");
				}
				event.consume();
			}
		});

		// add jar when it's dropped over
		adder.setOnDragDropped(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				Dragboard db = event.getDragboard();
				if (db.hasFiles()) {
					TestListPane.this.addTest(db.getFiles().get(0));
				}
				adder.setStyle("-fx-border-color: lightgray; -fx-border-width: 1;");
				event.consume();
			}
		});
	}

	/**
	 * Stops all testing.
	 */
	public void stopTests() {
		for (GUITestPackage test : this.tests) {
			test.killTests();
		}
	}

	public void pauseTests() {
		for (GUITestPackage test : this.tests) {
			test.pauseTests();
		}
	}

	/**
	 * Selects the given test.
	 * 
	 * @param toSelect
	 *            - test to select
	 */
	public void selectTest(GUITestPackage toSelect) {
		// light up only the selected test
		for (GUITestPackage test : this.tests) {
			test.getTestInfo().setStyle("-fx-border-color: lightgray; -fx-border-width: 1;");
		}
		toSelect.getTestInfo().setStyle("-fx-border-color: #039ED3; -fx-border-width: 2;");
		this.mainPane.setCenter(toSelect.getMainPane());
	}

	/**
	 * Adds a test with the given file.
	 * 
	 * @param toTest
	 *            - jar file to add
	 */
	public void addTest(File toTest) {
		// get an available name
		int i = 0;
		while (testNames.contains("test" + i)) {
			i++;
		}
		GUITestPackage newTest = new GUITestPackage(this, "test" + i, toTest);

		// register the test's name
		TestListPane.this.testNames.add("test" + i);
		newTest.getName().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				TestListPane.this.testNames.remove(oldValue);
				TestListPane.this.testNames.add(newValue);
			}
		});

		// add the test to the GUI at some point
		this.tests.add(newTest);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				TestListPane.this.contentPane.getChildren().add(infoAdderIndex, newTest.getTestInfo());
				selectTest(newTest);
			}
		});

		this.infoAdderIndex++;
	}

	/**
	 * Returns the set of test names used.
	 * 
	 * @return Set of test names used
	 */
	public Set<String> getTestNames() {
		return this.testNames;
	}

	/**
	 * Removes the given test.
	 * 
	 * @param guiTestPackage
	 *            - test to remove
	 */
	public void remove(GUITestPackage guiTestPackage) {
		this.mainPane.setCenter(null);
		this.tests.remove(guiTestPackage);
		this.contentPane.getChildren().remove(guiTestPackage.getTestInfo());
		this.infoAdderIndex--;
		this.testNames.remove(guiTestPackage.getName());
	}
}