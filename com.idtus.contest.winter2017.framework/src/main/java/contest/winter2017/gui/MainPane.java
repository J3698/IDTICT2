package contest.winter2017.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import contest.winter2017.Output;
import contest.winter2017.PermissionInfo;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Accordion;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * TabPane to control testing from. This component has three tabs. The first tab
 * is for running tests, the second is for test output, and the third is for
 * controlling parameter bounds to test with. This pane Takes up the majority of
 * the GUI real estate.
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
	private ParameterPane parameterPane = new ParameterPane();;

	/**
	 * RunPane of this MainPane.
	 */
	private RunPane runPane;

	/**
	 * TabPane of this MainPane.
	 */
	private TabPane outputPane = new TabPane();

	/**
	 * TextArea for this test's std out.
	 */
	private TextArea stdOutText = new TextArea();

	/**
	 * TextArea for this test's std err.
	 */
	private TextArea stdErrText = new TextArea();

	/**
	 * TextArea for this test's permissions.
	 */
	private PermissionPane permissionsPane = new PermissionPane();

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
		stdOutText.setEditable(false);
		stdOutText.setWrapText(true);
		stdOutText.setFocusTraversable(false);
		Tab stdOut = new Tab("Standard Out", stdOutText);
		stdErrText.setEditable(false);
		stdErrText.setWrapText(true);
		stdErrText.setFocusTraversable(false);
		Tab stdErr = new Tab("Standard Error", stdErrText);
		Tab permissions = new Tab("Permissions", permissionsPane);
		outputPane.getTabs().addAll(stdOut, stdErr, permissions);
		outputPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		Tab outputTab = new Tab("Output", outputPane);

		// parameter pane
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
		this.permissionsPane.addPermission(output.getPermissionMap());
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
	 * Returns this test's permissions pane.
	 * 
	 * @return this test's permissions pane
	 */
	public PermissionPane getPermissionsPane() {
		return this.permissionsPane;
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

class PermissionPane extends ScrollPane {
	private Map<String, PermissionInfoPane> permissionInfosMap = new HashMap<String, PermissionInfoPane>();

	private Accordion permissionInfos = new Accordion();

	public PermissionPane() {
		setContent(permissionInfos);
		setFitToWidth(true);
	}

	public void addPermission(Map<String, Integer> permissionLog) {
		for (Entry<String, Integer> entry : permissionLog.entrySet()) {
			PermissionInfoPane info = this.permissionInfosMap.get(entry.getKey());
			if (info == null) {
				info = new PermissionInfoPane(entry.getKey(), PermissionPane.this);
				this.permissionInfosMap.put(entry.getKey(), info);
				this.permissionInfos.getPanes().add(info);
			}
			info.addOccurance(entry.getValue());
		}
	}
}

class PermissionInfoPane extends TitledPane {
	private int occurances = 0;
	private String name;
	private PermissionPane permissionPane;

	public PermissionInfoPane(String name, PermissionPane permissionPane) {
		VBox content = new VBox();
		setContent(content);
		setExpanded(false);
		setText(name);
		this.name = name;
		this.permissionPane = permissionPane;

		Text allowsTitle = new Text("What it Allows");
		allowsTitle.setFont(new Font(15));
		VExternSpace allowsSpacer = new VExternSpace(allowsTitle, 0, 10);

		Text riskTitle = new Text("What it Risks");
		riskTitle.setFont(new Font(15));
		VExternSpace riskSpacer = new VExternSpace(riskTitle, 0, 10);

		Text allows = new Text(PermissionInfo.getAllowance(this.name));
		allows.setWrappingWidth(this.permissionPane.getWidth() - 30);
		allows.setFocusTraversable(false);

		Text risks = new Text(PermissionInfo.getRisk(this.name));
		risks.setWrappingWidth(this.permissionPane.getWidth() - 30);
		risks.setFocusTraversable(false);

		this.permissionPane.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				allows.setWrappingWidth(arg2.doubleValue() - 30);
				risks.setWrappingWidth(arg2.doubleValue() - 30);
			}
		});

		content.getChildren().addAll(allowsSpacer, allows, riskSpacer, risks);
	}

	public void addOccurance(int occurances) {
		this.occurances += occurances;
		setText(name + " (" + this.occurances + " occurances)");
	}
}
