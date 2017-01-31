package contest.winter2017.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
 * ScrollPane to hold the tests.
 * 
 * @author ICT-2
 */
class TestListPane extends ScrollPane {
	/**
	 * Int width of the ScrollPane.
	 */
	public static final double WIDTH = 150;

	/**
	 * Double spacing between components in the TestListPane.
	 */
	private static final double SPACING = 4;

	/**
	 * MainPane of the GUI.
	 */
	private BorderPane mainPane;

	/**
	 * List of tests for the GUI.
	 */
	private List<GUITestPackage> tests = new ArrayList<GUITestPackage>();

	/**
	 * Set of names for the tests.
	 */
	private Set<String> testNames;

	/**
	 * Int position where to place new TestInfo components.
	 */
	private int infoAdderIndex;

	/**
	 * ContentPane for test info.
	 */
	private VBox contentPane;

	/**
	 * Constructs a TestListPane with the specified parent BorderPane.
	 */
	public TestListPane(BorderPane mainPane) {
		this.mainPane = mainPane;

		setMinWidth(WIDTH);
		setFitToWidth(true);
		setHbarPolicy(ScrollBarPolicy.NEVER);
		setVbarPolicy(ScrollBarPolicy.ALWAYS);
		setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");

		this.testNames = new HashSet<String>();

		this.contentPane = new VBox(SPACING);
		setContent(this.contentPane);

		infoAdderIndex = this.contentPane.getChildren().size();
		makeTestAdder();
	}

	/**
	 * Makes the test adder component
	 */
	public void makeTestAdder() {
		VBox adder = new VBox();
		adder.setAlignment(Pos.CENTER);
		adder.setStyle("-fx-border-color: lightgray; -fx-border-width: 1;");

		Button addButton = new Button("Select Jar");
		Text drag = new Text("Or Drag and Drop Jar");
		drag.setFont(new Font(10));

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
		adder.setOnDragExited(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				if (event.isAccepted()) {
					adder.setStyle("-fx-border-color: lightgray; -fx-border-width: 1;");
				}
				event.consume();
			}
		});
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

		adder.getChildren().addAll(new VExternSpace(addButton, 13, 4), new VExternSpace(drag, 4, 13));
		this.contentPane.getChildren().add(adder);
	}

	/**
	 * Selects the given test.
	 * 
	 * @param toSelect
	 *            - test to select
	 */
	public void selectTest(GUITestPackage toSelect) {
		for (GUITestPackage test : this.tests) {
			test.getTestInfo().setStyle("-fx-border-color: lightgray; -fx-border-width: 1;");
		}
		toSelect.getTestInfo().setStyle("-fx-border-color: #039ED3; -fx-border-width: 2;");
		this.mainPane.setCenter(toSelect.getMainPane());
	}

	/**
	 * Adds a test.
	 * 
	 * @param toTest
	 *            - jar file to add
	 */
	public void addTest(File toTest) {
		int i = 0;
		while (testNames.contains("test" + i)) {
			i++;
		}

		GUITestPackage newTest = new GUITestPackage(this, "test" + i, toTest);
		newTest.getName().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				TestListPane.this.testNames.remove(oldValue);
				TestListPane.this.testNames.add(newValue);
			}

		});

		this.tests.add(newTest);
		this.contentPane.getChildren().add(infoAdderIndex, newTest.getTestInfo());
		selectTest(newTest);
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
}