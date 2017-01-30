package contest.winter2017.gui;

import java.io.File;

import contest.winter2017.Tester;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class GUITestPackage {
	private TestListPane testListPane;
	private TestInfo testInfo;
	private MainPane mainPane;
	private Tester tester;
	private SimpleStringProperty name;
	private File toTest;

	public GUITestPackage(TestListPane testListPane, String name, File toTest) {
		this.name = new SimpleStringProperty(name);
		this.testListPane = testListPane;
		this.toTest = toTest;
		this.testInfo = new TestInfo(this);
		this.mainPane = new MainPane(this);
		this.tester = new Tester();
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
		getChildren().addAll(name, percent, progressBar);

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