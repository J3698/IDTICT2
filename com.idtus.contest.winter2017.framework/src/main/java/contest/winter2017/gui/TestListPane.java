package contest.winter2017.gui;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
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
	public static final double WIDTH = 150;
	private static final double SPACING = 4;
	private int infoAdderIndex;

	/**
	 * ContentPane for test info.
	 */
	private VBox contentPane;

	/**
	 * Constructs a TestListPane with the specified parent BorderPane.
	 */
	public TestListPane() {
		setMinWidth(WIDTH);
		setFitToWidth(true);
		setHbarPolicy(ScrollBarPolicy.NEVER);
		setVbarPolicy(ScrollBarPolicy.ALWAYS);
		setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");

		this.contentPane = new VBox(SPACING);
		setContent(this.contentPane);

		infoAdderIndex = this.contentPane.getChildren().size();
		this.contentPane.getChildren().add(new TestInfoAdder(this));
	}

	/**
	 * Selects the given testInfo.
	 * 
	 * @param testInfo
	 *            - TestInfo to select
	 */
	public void selectTest(TestInfo testInfo) {
		for (Node node : this.contentPane.getChildren()) {
			if (node instanceof TestInfo) {
				node.setStyle("-fx-border-color: lightgray; -fx-border-width: 1;");
			}
		}
		testInfo.setStyle("-fx-border-color: #039ED3; -fx-border-width: 2;");
		((BorderPane) getParent()).setCenter(testInfo.getMainPane());
	}

	/**
	 * Adds a test.
	 * 
	 * @param test
	 *            - test to add
	 */
	public void addTest(File testInfo) {
		this.contentPane.getChildren().add(infoAdderIndex, new TestInfo(this));
		this.infoAdderIndex++;
	}
}

/**
 * VBox used to hold basic info for tests.
 * 
 * @author ICT-2
 */
class TestInfo extends VBox {
	/**
	 * MainPane of the test.
	 */
	private MainPane mainPane;

	/**
	 * Constructs a TestInfo.
	 */
	public TestInfo(TestListPane pane) {
		setAlignment(Pos.CENTER);
		setStyle("-fx-border-color: lightgray; -fx-border-width: 1;");// -fx-focus-color:black;

		this.mainPane = new MainPane();

		Text name = new Text("Test01");
		name.setFont(new Font(20));
		Text percent = new Text("0%");
		percent.setFont(new Font(10));
		ProgressBar progressBar = new ProgressBar(0);
		progressBar.setMouseTransparent(true);
		progressBar.setPadding(new Insets(3, 0, 0, 2));
		getChildren().addAll(name, percent, progressBar);

		setOnMouseClicked(new EventHandler<MouseEvent>() {
			/**
			 * Handles an event.
			 * 
			 * @param event
			 *            - event to handle
			 */
			public void handle(MouseEvent event) {
				if (event.getButton() == MouseButton.PRIMARY) {
					pane.selectTest(TestInfo.this);
				}
			}
		});
	}

	/**
	 * Returns the MainPane for this test.
	 * 
	 * @return MainPane for this test
	 */
	public MainPane getMainPane() {
		return this.mainPane;
	}
}

/**
 * VBox used to create new tests.
 * 
 * @author ICT-2
 */
class TestInfoAdder extends VBox {

	/**
	 * Constructs a new TestInfoAdder.
	 */
	public TestInfoAdder(TestListPane testList) {
		setAlignment(Pos.CENTER);
		setStyle("-fx-border-color: lightgray; -fx-border-width: 1;");

		Button addButton = new SelectJarButton(testList);
		Text drag = new Text("Or Drag and Drop Jar");
		drag.setFont(new Font(10));
		getChildren().addAll(new VExternSpace(addButton, 13, 4), new VExternSpace(drag, 4, 13));

		setOnDragEntered(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				Dragboard db = event.getDragboard();
				if (db.hasFiles() && db.getFiles().size() == 1) {
					if (db.getFiles().get(0).getPath().endsWith(".jar")) {
						setStyle("-fx-border-color: #039ED3; -fx-border-width: 2;");
					}
				}

				event.consume();
			}
		});
		setOnDragOver(new EventHandler<DragEvent>() {
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
		setOnDragExited(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				if (event.isAccepted()) {
					setStyle("-fx-border-color: lightgray; -fx-border-width: 1;");
				}
				event.consume();
			}
		});
		setOnDragDropped(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				Dragboard db = event.getDragboard();
				System.out.println(db.hashCode());
				if (db.hasFiles()) {
					testList.addTest(db.getFiles().get(0));
				}
				setStyle("-fx-border-color: lightgray; -fx-border-width: 1;");
				event.consume();
			}
		});
	}
}

/**
 * Button used to select a Jar to test.
 * 
 * @author ICT-2
 */
class SelectJarButton extends Button {
	/**
	 * Constructs a SelectJarButton with the TestListPane specified.
	 * 
	 * @param testLists
	 *            - testLists to add a test to
	 */
	public SelectJarButton(TestListPane testLists) {
		super("Select Jar");

		setOnAction(new EventHandler<ActionEvent>() {
			/**
			 * Handles an event.
			 * 
			 * @param event
			 *            - event to handle
			 */
			@Override
			public void handle(ActionEvent event) {
				FileChooser fc = new FileChooser();
				fc.setTitle("Choose a Jar");
				fc.getExtensionFilters().addAll(new ExtensionFilter("Java Jar Files", "*.jar"));
				Window window = SelectJarButton.this.getScene().getWindow();
				File selected = fc.showOpenDialog(window);
				if (selected != null && selected.exists()) {
					testLists.addTest(selected);
				}
			}
		});
	}

}