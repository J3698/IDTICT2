package contest.winter2017.gui;

import java.util.Set;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Entry point for the GUI version of testing framework.
 * 
 * @author ICT-2
 */
public class GUIMain extends Application {
	/**
	 * Runs the GUI.
	 * 
	 * @param args
	 *            - args supplied to application
	 */
	public static void main(String[] args) {
		GUIMain.launch(args);
	}

	/**
	 * Starts the GUI.
	 * 
	 * @param stage
	 *            - stage for the GUI
	 */
	public void start(Stage stage) {
		BorderPane pane = new BorderPane();
		// pane.setTop(new TestMenuBar());
		pane.setLeft(new TestListPane());
		pane.setCenter(new MainPane());
		// dirty work
		Scene scene = new Scene(pane, 640, 480);
		stage.setScene(scene);
		stage.show();
	}
}

/**
 * ScrollPane to hold the tests.
 * 
 * @author ICT-2
 */
class TestListPane extends ScrollPane {
	public static final double WIDTH = 150;
	private static final double SPACING = 4;

	/**
	 * ContentPane for test info.
	 */
	private VBox contentPane;

	/**
	 * Constructs a TestListPane.
	 */
	public TestListPane() {
		setMinWidth(WIDTH);
		setHbarPolicy(ScrollBarPolicy.NEVER);
		setVbarPolicy(ScrollBarPolicy.ALWAYS);
		this.contentPane = new VBox(SPACING);
		// this.contentPane.setAlignment(Pos.CENTER);
		setContent(this.contentPane);
		Set<Node> bars = lookupAll(".scroll-bar");

		double width = WIDTH;
		for (final Node node : bars) {
			if (node instanceof ScrollBar && ((ScrollBar) node).isVisible()) {
				width -= ((ScrollBar) node).getWidth();
				break;
			}
		}
		for (int i = 0; i < 20; i++)
			this.contentPane.getChildren().add(new TestInfo(width));
	}
}

class TestInfo extends VBox {
	/**
	 * Constructs a TestInfo.
	 */
	public TestInfo(double width) {
		setAlignment(Pos.CENTER);
		setPrefWidth(width);
		setStyle("-fx-border-color: dimgray; -fx-border-width: 1;");
		Text name = new Text("Test01");
		name.setFont(new Font(20));
		Text percent = new Text("20%");
		percent.setFont(new Font(10));
		ProgressBar progressBar = new ProgressBar(0.5);
		progressBar.setPadding(new Insets(3, 0, 0, 2));
		getChildren().addAll(name, percent, progressBar);
	}
}

/**
 * TabPane to control testing form.
 * 
 * @author ICT-2
 */
class MainPane extends TabPane {
	/**
	 * Constructs a MainPane.
	 */
	public MainPane() {
		Tab runTab = new Tab("Run");
		Tab outputTab = new Tab("Output");
		Tab parameterTab = new Tab("Parameter Bounds");
		getTabs().addAll(runTab, outputTab, parameterTab);
		setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
	}
}

/**
 * 
 * @author ICT-2
 */
class TestMenuBar extends MenuBar {
	/**
	 * Constructs a TestMenuBar.
	 */
	public TestMenuBar() {
		getMenus().add(new Menu("File"));
	}
}
