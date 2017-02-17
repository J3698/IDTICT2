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
 * controlling parameter bounds to test with. This pane takes up the majority of
 * the GUI real estate.
 * 
 * @author ICT-2
 */
public class MainPane extends TabPane {
	/**
	 * Test of this main pane.
	 */
	private GUITestPackage test;

	/**
	 * Parameter pane of this main pane.
	 */
	private ParameterPane parameterPane = new ParameterPane();;

	/**
	 * Run pane of this main pane.
	 */
	private RunPane runPane;

	/**
	 * Tab pane of this main pane.
	 */
	private TabPane outputPane = new TabPane();

	/**
	 * Text area for this test's inputs.
	 */
	private TextArea inputText = new TextArea();

	/**
	 * Text area for this test's standard out.
	 */
	private TextArea stdOutText = new TextArea();

	/**
	 * Text area for this test's standard err.
	 */
	private TextArea stdErrText = new TextArea();

	/**
	 * Text area for this test's tool-chain output.
	 */
	private TextArea toolchainOutput = new TextArea();

	/**
	 * Text area for this test's permissions.
	 */
	private PermissionPane permissionsPane = new PermissionPane();

	/**
	 * Int tests recorded so far.
	 */
	private int testsAdded = 0;

	/**
	 * Constructs a main pane with the given test.
	 */
	public MainPane(GUITestPackage test) {
		this.test = test;

		// run pane
		this.runPane = new RunPane(this.test);
		Tab runTab = new Tab("Run", this.runPane);

		// tabs of output pane

		this.inputText.setEditable(false);
		this.inputText.setWrapText(false);
		this.inputText.setFocusTraversable(false);
		Tab input = new Tab("Input Commands", this.inputText);

		this.stdOutText.setEditable(false);
		this.stdOutText.setWrapText(true);
		this.stdOutText.setFocusTraversable(false);
		Tab stdOut = new Tab("Standard Out", this.stdOutText);

		this.stdErrText.setEditable(false);
		this.stdErrText.setWrapText(true);
		this.stdErrText.setFocusTraversable(false);
		Tab stdErr = new Tab("Standard Error", this.stdErrText);

		this.toolchainOutput.setEditable(false);
		this.toolchainOutput.setWrapText(true);
		this.toolchainOutput.setFocusTraversable(false);
		Tab toolChain = new Tab("Toolchain", this.toolchainOutput);

		Tab permissions = new Tab("Permissions", this.permissionsPane);

		this.outputPane.getTabs().addAll(input, stdOut, stdErr, permissions, toolChain);

		this.outputPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		Tab outputTab = new Tab("Output", this.outputPane);

		// parameter pane

		Tab parameterTab = new Tab("Parameter Bounds", this.parameterPane);

		getTabs().addAll(runTab, outputTab, parameterTab);
		setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
	}

	/**
	 * Adds an output to this main-pane's output pane.
	 * 
	 * @param output
	 *            - output to add
	 */
	public void addOutput(Output output) {
		this.testsAdded++;
		String prefix = "Test " + this.testsAdded + ":\n";
		this.inputText.appendText(prefix + output.getCommand().replace('\n', ' ') + "\n\n");
		this.stdErrText.appendText(prefix + output.getStdErrString().replace('\n', ' ') + "\n\n");
		this.stdOutText.appendText(prefix + output.getStdOutString().replace('\n', ' ') + "\n\n");
		this.permissionsPane.addPermission(output.getPermissionMap());
	}

	/**
	 * Sets the tool chain output text.
	 * 
	 * @param toolChainOut
	 *            - toolChainOut to set
	 */
	public void setToolChainOut(String toolChainOut) {
		this.toolchainOutput.setText(toolChainOut);
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
	 * Returns this test's standard out text.
	 * 
	 * @return this test's standard out text
	 */
	public TextArea getStdOutText() {
		return this.stdOutText;
	}

	/**
	 * Returns this test's standard err text.
	 * 
	 * @return this test's standard err text
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
	 * Returns this main-pane's test.
	 * 
	 * @return test of this main pane
	 */
	public GUITestPackage getTest() {
		return this.test;
	}
}

/**
 * This scroll pane is meant for displaying the permissions encountered during
 * testing.
 * 
 * @author ICT-2
 */
class PermissionPane extends ScrollPane {
	/**
	 * Map of permission names to their permission info panes
	 */
	private Map<String, PermissionInfoPane> permissionInfosMap = new HashMap<String, PermissionInfoPane>();

	/**
	 * Accordion view of permission info panes
	 */
	private Accordion permissionInfos = new Accordion();

	/**
	 * Constructs a permission pane.
	 */
	public PermissionPane() {
		VBox box = new VBox(0);
		Text copyright = new Text("About Permission Information: " + PermissionInfo.COPYRIGHT_NOTICE);
		copyright.setWrappingWidth(300);
		VExternSpace copyrightSpace = new VExternSpace(copyright, 5, 5);

		box.getChildren().addAll(copyrightSpace, permissionInfos);
		setContent(box);
		setFitToWidth(true);
	}

	/**
	 * Adds the given permission to this permission pane.
	 * 
	 * @param permissionLog
	 *            - permission log map to use
	 */
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

/**
 * Titled pane to hold information about a permission. Title shows the
 * permission and how many times it was encountered.
 * 
 * @author ICT-2
 */
class PermissionInfoPane extends TitledPane {
	/**
	 * Number of occurrences of this permission.
	 */
	private int occurrences = 0;

	/**
	 * Name of this permission.
	 */
	private String name;

	/**
	 * Parent permission pane of this permission info pane.
	 */
	private PermissionPane permissionPane;

	/**
	 * Constructs a permission info pane with the given name and permission
	 * pane.
	 * 
	 * @param name
	 *            - name of this permission
	 * @param permissionPane
	 *            - parent permission pane of this permission info pane
	 */
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

	/**
	 * Adds occurrences of this permission to this panes title.
	 * 
	 * @param occurrences
	 *            - occurrences to add
	 */
	public void addOccurance(int occurrences) {
		this.occurrences += occurrences;
		setText(name + " (" + this.occurrences + " occurances)");
	}
}
