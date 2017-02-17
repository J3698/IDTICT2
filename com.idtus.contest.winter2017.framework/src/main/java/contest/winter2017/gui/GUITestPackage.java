package contest.winter2017.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import contest.winter2017.Output;
import contest.winter2017.Tester;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;

/**
 * Class encapsulating a test run in the GUI interface.
 * 
 * This class is primarily responsible for encapsulating the main pane, tester,
 * and test info for a given test.
 * 
 * @author ICT-2
 */
public class GUITestPackage {
	/**
	 * Test list pane of this test.
	 */
	private TestListPane testListPane;

	/**
	 * Test info GUI component of this test.
	 */
	private TestInfo testInfo;

	/**
	 * Main pane of this test.
	 */
	private MainPane mainPane;

	/**
	 * Tester of this test.
	 */
	private Tester tester = new Tester();

	/**
	 * Name of this test.
	 */
	private SimpleStringProperty name;

	/**
	 * Jar to test.
	 */
	private File toTest;

	/**
	 * Synchronized list of outputs for this test.
	 */
	private List<Output> outputs = Collections.synchronizedList(new ArrayList<Output>());

	private boolean testsKilled = false;

	/**
	 * Constructs a GUI test package with the given testListPane, name, and file
	 * to test.
	 * 
	 * @param testListPane
	 *            - testListPane to put test info in
	 * @param name
	 *            - name of this test
	 * @param toTest
	 *            - file to test
	 */
	public GUITestPackage(TestListPane testListPane, String name, File toTest) {
		// order of initialization matters
		this.name = new SimpleStringProperty(name);
		this.testListPane = testListPane;
		this.toTest = toTest;
		this.testInfo = new TestInfo(this);
		this.mainPane = new MainPane(this);
	}

	/**
	 * Starts tests for this test.
	 */
	public void startTests() {
		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() {
				try {
					GUITestPackage.this.tester.executeBasicTests();
					GUITestPackage.this.tester.executeSecurityTests();
					String toolChainOut = GUITestPackage.this.tester.getYAMLOutput();
					GUITestPackage.this.mainPane.setToolChainOut(toolChainOut);
				} catch (Exception e) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							throw e;
						}
					});
				}

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						GUITestPackage.this.testInfo.endTests();
						GUITestPackage.this.mainPane.getRunPane().endTests();
					}
				});
				return null;
			}
		};
		new Thread(task, "Test Runner").start();
	}

	/**
	 * Resumes tests for this test.
	 */
	public void resumeTests() {
		GUITestPackage.this.tester.setPaused(false);
		this.mainPane.getRunPane().setResumed();
		this.testInfo.getProgressBar().setDisable(false);
	}

	/**
	 * Pauses tests for this test.
	 */
	public void pauseTests() {
		GUITestPackage.this.tester.setPaused(true);
		this.mainPane.getRunPane().setPaused();
		this.testInfo.getProgressBar().setDisable(true);
	}

	/**
	 * Kills testing for this test.
	 */
	public void killTests() {
		this.testsKilled = true;
		this.tester.killTests();
	}

	/**
	 * Removes this test from the GUI.
	 */
	public void remove() {
		killTests();
		this.testListPane.remove(this);
	}

	/**
	 * Updates the output for this test.
	 */
	public void updateOutput() {
		while (outputs.size() < this.tester.getOutputs().size()) {
			Output newOutput = this.tester.getOutputs().get(outputs.size());
			outputs.add(newOutput);
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					GUITestPackage.this.mainPane.addOutput(newOutput);
				}
			});
		}
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
	 * Returns whether user bounds are to be used.
	 * 
	 * @return true if user bounds are to be used, otherwise false
	 */
	public boolean hasUserTestBounds() {
		return this.getMainPane().getParameterPane().hasUserTestBounds();
	}

	/**
	 * Returns whether user bounds valid.
	 * 
	 * @return true if user bounds are valid, otherwise false
	 */
	public boolean hasValidUserTestBounds() {
		ParameterPane pane = this.getMainPane().getParameterPane();
		return (pane.getFormatBuilder().getCurrentError() == null);
	}

	/**
	 * Returns user defined test bounds if they exist.
	 * 
	 * @return user defined test bounds or null if they are not defined
	 */
	@SuppressWarnings("rawtypes")
	public Map getUserTestBounds() {
		if (this.getMainPane().getParameterPane().hasUserTestBounds()) {
			FormatBuilder builder = this.getMainPane().getParameterPane().getFormatBuilder();
			List<ParameterEditor> editors = builder.getParameterEditors();
			Map<Object, Object> parameters = new HashMap<Object, Object>();
			if (builder.isDynamic()) {
				for (ParameterEditor editor : editors) {
					Map<Object, Object> parameter = new HashMap<Object, Object>();
					// put dynamic options
					parameter.put("optional", new Boolean(editor.isOptional()));

					List<FormatString> formatStrings = editor.getFormatStrings();
					if (formatStrings.size() == 1) {
						// handle non enumerations
						FormatString formatString = formatStrings.get(0);
						parameter.put("format", formatString.getFormatString());
						parameter.put("min", formatString.getMin());
						parameter.put("max", formatString.getMax());
						parameters.put(editor.getRegexKey(), parameter);
					} else {
						// handle enumerations
						List<String> enumeratedValues = new ArrayList<String>();
						List<Object> minimums = new ArrayList<Object>();
						List<Object> maximums = new ArrayList<Object>();
						for (FormatString formatString : formatStrings) {
							enumeratedValues.add(formatString.getFormatString());
							minimums.add(formatString.getMin());
							maximums.add(formatString.getMax());
						}
						parameter.put("enumerated values", enumeratedValues);
						parameter.put("min", minimums);
						parameter.put("max", maximums);
						parameters.put(editor.getRegexKey(), parameter);
					}
				}
			} else {
				List<Map> fixedParameterList = new ArrayList<Map>();
				for (ParameterEditor editor : editors) {
					Map<Object, Object> parameter = new HashMap<Object, Object>();
					FormatString formatString = editor.getFormatStrings().get(0);
					parameter.put("format", formatString.getFormatString());
					parameter.put("min", formatString.getMin());
					parameter.put("max", formatString.getMax());
					fixedParameterList.add(parameter);
				}
				parameters.put("fixed parameter list", fixedParameterList);
			}
			return parameters;
		} else {
			return null;
		}
	}

	/**
	 * Returns this test's test list pane.
	 * 
	 * @return the test list pane parent of this test
	 */
	public TestListPane getTestListPane() {
		return this.testListPane;
	}

	/**
	 * Returns this test's test info.
	 * 
	 * @return this test's test info
	 */
	public TestInfo getTestInfo() {
		return this.testInfo;
	}

	/**
	 * Returns this test's main pane.
	 * 
	 * @return this test's main pane
	 */
	public MainPane getMainPane() {
		return this.mainPane;
	}

	/**
	 * Returns this test's tester.
	 * 
	 * @return this test's tester
	 */
	public Tester getTester() {
		return this.tester;
	}

	/**
	 * Returns this test's jar to test.
	 * 
	 * @return this test's jar to test
	 */
	public File getToTest() {
		return this.toTest;
	}
}