package contest.winter2017;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.Attributes;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.tools.ExecFileLoader;

import javafx.beans.property.SimpleDoubleProperty;

/**
 * Class that will handle execution of basic tests and exploratory security test
 * on a black-box executable jar.
 * 
 * Example code that we used to guide our use of Jacoco code coverage was
 * found @ http://www.eclemma.org/jacoco/trunk/doc/api.html
 * 
 * ICT-2 has made several changes to this class. Many of them are documentation
 * based, or were meant to improve code quality. In addition, toolchain mode has
 * been implemented. Print statements directed to standard out are silenced if
 * toolchain mode is used. ICT-2 has also changed how tests are run. ICT-2 has
 * taken advantage of java's security structure to monitor potentially nefarious
 * behavior.
 * 
 * @author IDT
 */
public class Tester {

	//////////////////////////////////////////
	// STATIC MEMBERS
	//////////////////////////////////////////

	/**
	 * Suffix for all jacoco output files.
	 */
	private static final String JACOCO_OUTPUT_FILE_SUFFIX = "_jacoco.exec";

	/**
	 * Horizontal line shown between test output.
	 */
	private static final String HORIZONTAL_LINE = "-------------------------------------------------------------------------------------------";

	/**
	 * Initialization error message.
	 */
	private static final String INIT_ERROR_MSSG = "ERROR: An exception occurred during initialization.";

	/**
	 * Minimum time goal for tests to run in, default 0 minutes.
	 */
	public static final int MIN_TIME_GOAL = 0;

	/**
	 * Minimum number of black box iterations to run.
	 */
	public static final int MIN_BB_TESTS = 0;

	/**
	 * Minimum time goal for tests to run in, default 0 minutes.
	 */
	public static final int DEFAULT_TIME_GOAL = 5;

	/**
	 * Minimum number of black box iterations to run.
	 */
	public static final int DEFAULT_BB_TESTS = 1000;

	//////////////////////////////////////////
	// INSTANCE MEMBERS
	//////////////////////////////////////////

	/**
	 * ID used to differentiate from where this Tester is run.
	 */
	private String guiID;

	/**
	 * Path to the SecurityWatchdog class.
	 */
	private String watchdogPath = null;

	/**
	 * Path of the jar to test as a String.
	 */
	private String jarToTestPath = null;

	/**
	 * Path of the directory for jacoco output as a String.
	 */
	private String jacocoOutputDirPath = null;

	/**
	 * Path to the jacoco agent library as a String.
	 */
	private String jacocoAgentJarPath = null;

	/**
	 * Path to the file for jacoco output as a String.
	 */
	private String jacocoOutputFilePath = null;

	/**
	 * Number of black box iterations to run, default 1000.
	 */
	private Integer bbTests = DEFAULT_BB_TESTS;

	/**
	 * Target time in minutes for tester to run, default 5 minutes.
	 */
	private Integer timeGoal = DEFAULT_TIME_GOAL;

	/**
	 * Maximum time per test.
	 */
	private Integer maxMillisPerTest = 1000;

	/**
	 * Option to be silent.
	 */
	private Boolean quiet = false;

	/**
	 * Option to monitor permissions.
	 */
	private Boolean watchdog;

	/**
	 * Whether testing is paused.
	 */
	private AtomicBoolean isPaused = new AtomicBoolean(false);

	/**
	 * Whether this tester is killed
	 */
	private AtomicBoolean isKilled = new AtomicBoolean(false);

	/**
	 * Number of predefined tests which have passed.
	 */
	private int passCount = 0;

	/**
	 * Number of predefined tests which have failed.
	 */
	private int failCount = 0;

	/**
	 * Percent of testing completed.
	 */
	private SimpleDoubleProperty percentDone = new SimpleDoubleProperty(0);

	/**
	 * Basic tests which have been extracted from the jar under test.
	 */
	private List<Test> predefinedTests = null;

	/**
	 * Percent of jar under test which has been covered during testing.
	 */
	private double percentCovered = 0.0;

	/**
	 * ParameterFactory that can be used to help figure out parameter signatures
	 * from the blackbox jars.
	 */
	private ParameterFactory parameterFactory = null;

	/**
	 * List of outputs encountered.
	 */
	private List<Output> outputs = Collections.synchronizedList(new ArrayList<Output>(2000));

	/**
	 * Set to hold unique exceptions that have thus far been encountered.
	 */
	private HashSet<String> exceptionSet = new HashSet<String>();

	//////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////

	/**
	 * Initialize the tester by loading up the jar to test, and then extracting
	 * parameters, parameter bounds (if any), and basic tests from the jar.
	 * 
	 * @param initJarToTestPath
	 *            - String representing path of the jar to test
	 * @param initJacocoOutputDirPath
	 *            - String representing path of the directory jacoco will use
	 *            for output
	 * @param initJacocoAgentJarPath
	 *            - String representing path of the jacoco agent jar
	 * @return boolean - false if initialization encounters an Exception, true
	 *         if it does not
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean init(Map parameterBounds, String initJarToTestPath, String initJacocoOutputDirPath,
			String initJacocoAgentJarPath, String initbbTests, String initTimeGoal, String initGuiID, boolean initQuiet,
			boolean initWatchdog) {

		this.guiID = initGuiID;
		this.watchdogPath = Tester.class.getResource("Tester.class").getPath();
		this.watchdogPath = this.watchdogPath.replace("contest/winter2017/Tester.class", "");
		this.jarToTestPath = initJarToTestPath;
		this.jacocoOutputDirPath = initJacocoOutputDirPath;
		this.jacocoAgentJarPath = initJacocoAgentJarPath;

		// parse toolChain argument
		this.quiet = initQuiet;
		this.watchdog = initWatchdog;

		// parse bbTests if it exists
		if (initbbTests != null) {
			int tempbbTests = -1;
			try {
				tempbbTests = Integer.parseInt(initbbTests);
			} catch (NumberFormatException e) {
				initError("Option bbTests could not be parsed to an int.");
				return false;
			}
			if (tempbbTests < MIN_BB_TESTS) {
				initError("Minimum value of bbTests is " + MIN_BB_TESTS + ".");
				return false;
			} else {
				this.bbTests = tempbbTests;
			}
		}

		// parse timeGoal if it exists
		if (initTimeGoal != null) {
			int tempTimeGoal = -1;
			try {
				tempTimeGoal = Integer.parseInt(initTimeGoal);
			} catch (NumberFormatException e) {
				initError("Option timeGoal could not be parsed to an int.");
				return false;
			}
			if (tempTimeGoal < MIN_TIME_GOAL) {
				initError("Minimum value of timeGoal is " + MIN_TIME_GOAL + ".");
				return false;
			} else {
				this.timeGoal = tempTimeGoal;
			}
		}

		File jarFileToTest = new File(this.jarToTestPath);
		StringBuffer outputFilePath = new StringBuffer(300);
		// delete previous command line runs if running from the command line
		if (guiID.equals("")) {
			outputFilePath.append(this.jacocoOutputDirPath + "\\");
			outputFilePath.append(jarFileToTest.getName().replaceAll("\\.", "_"));
			outputFilePath.append(JACOCO_OUTPUT_FILE_SUFFIX);

			File jacocoOutputFile = new File("" + outputFilePath);
			if (jacocoOutputFile != null && jacocoOutputFile.exists()) {
				jacocoOutputFile.delete();
			}
		} else {
			// find an available name if running from the GUI
			int availableID = -1;
			File jacocoOutputFile;
			do {
				availableID++;
				outputFilePath = new StringBuffer(300);
				outputFilePath.append(this.jacocoOutputDirPath);
				outputFilePath.append("\\" + this.guiID);
				outputFilePath.append(jarFileToTest.getName().replaceAll("\\.", "_"));
				outputFilePath.append(availableID + JACOCO_OUTPUT_FILE_SUFFIX);
				jacocoOutputFile = new File("" + outputFilePath);
			} while (jacocoOutputFile != null && jacocoOutputFile.exists());
		}
		this.jacocoOutputFilePath = "" + outputFilePath;

		// load up the jar under test so that we can access information its
		// classes
		URL fileURL = null;
		URL jarURL = null;
		JarURLConnection jarURLconn = null;
		URLClassLoader cl = null;
		try {
			fileURL = jarFileToTest.toURI().toURL();
			String jarUrlTemp = "jar:" + jarFileToTest.toURI().toString() + "!/";
			jarURL = new URL(jarUrlTemp);
			cl = URLClassLoader.newInstance(new URL[] { fileURL });
			jarURLconn = null;
			jarURLconn = (JarURLConnection) jarURL.openConnection();
		} catch (IOException ioe) {
			initError("Could not load specified jar file to test.");
			return false;
		}

		// figure out where the entry-point (main class) is in the jar under
		// test
		Attributes attr = null;
		try {
			attr = jarURLconn.getMainAttributes();
		} catch (IOException ioe) {
			initError("Could not load manifest from jar to test.");
			return false;
		}
		String mainClassName = attr.getValue(Attributes.Name.MAIN_CLASS);
		if (mainClassName == null) {
			initError("Cannot test jar without a main class.");
			return false;
		}

		// only get test bounds if they were not supplied
		this.predefinedTests = new ArrayList<Test>();
		Map<String, Object> mainClassTestBoundsMap = null;
		if (parameterBounds == null) {
			// load the TestBounds class from the jar under test
			String mainClassTestBoundsName = mainClassName + "TestBounds";
			Class<?> mainClassTestBounds = null;
			try {
				mainClassTestBounds = cl.loadClass(mainClassTestBoundsName);
			} catch (ClassNotFoundException cnfe) {
				initError("Cannot test jar without a [MainClassName]TestBounds.class file.");
				return false;
			}

			// use reflection to invoke the TestBounds class to get the usage
			// information from the jar
			Method testBoundsMethod = null;
			try {
				testBoundsMethod = mainClassTestBounds.getMethod("testBounds");
			} catch (NoSuchMethodException nsme) {
				initError("Could not get testBounds method from [MainClassName]TestBounds class.");
				return false;
			}
			Object mainClassTestBoundsInstance = null;
			try {
				mainClassTestBoundsInstance = mainClassTestBounds.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				initError("Could not instantiate [MainClassName]TestBounds class from jar to test.");
				return false;
			}

			// get test bounds
			try {
				mainClassTestBoundsMap = (Map<String, Object>) testBoundsMethod.invoke(mainClassTestBoundsInstance);
			} catch (ExceptionInInitializerError | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				initError("Could not invoke method testBounds from [MainClassName]TestBounds class.");
				return false;
			}

			// get a list of basic tests from the TestBounds class
			List testList = (List) mainClassTestBoundsMap.get("tests");
			for (Object inTest : testList) {
				this.predefinedTests.add(new Test((Map) inTest));
			}
		}

		// use user supplied bounds if supplied
		if (parameterBounds != null) {
			mainClassTestBoundsMap = parameterBounds;
		}

		// instantiating a new Parameter Factory using the Test Bounds map
		this.parameterFactory = new ParameterFactory(mainClassTestBoundsMap);

		return true;
	}

	/**
	 * Executes predefined tests on the jar under test.
	 * <p>
	 * This is the half of the framework that IDT has completed. We are able to
	 * pull basic tests directly from the executable jar. We are able to run the
	 * tests and assess the output as PASS/FAIL.
	 * 
	 * ICT-2 has made one minor change to this method. If toolChain mode is
	 * used, this method is now silent.
	 */
	public void executeBasicTests() {
		// iterate through the lists of tests and execute each one
		for (Test test : this.predefinedTests) {
			if (isKilled.get()) {
				return;
			}

			// instrument the code to code coverage metrics, execute the test
			// with given parameters, then show the output
			Output output = instrumentAndExecuteCode(test.getParameters().toArray());
			if (output != null) {
				printBasicTestOutput(output);

				// determine the result of the test based on expected
				// output/error regex
				if (output.getStdOutString().matches(test.getStdOutExpectedResultRegex())
						&& output.getStdErrString().matches(test.getStdErrExpectedResultRegex())) {
					if (!this.quiet) {
						System.out.println("basic test result: PASS");
					}

					this.passCount++;
				} else {
					if (!this.quiet) {
						System.out.println("basic test result: FAIL ");
					}

					this.failCount++;

					if (!this.quiet) {
						// since we have a failed basic test, show the
						// expectation for the stdout
						if (!output.getStdOutString().matches(test.getStdOutExpectedResultRegex())) {
							System.out.println("\t ->stdout: " + output.getStdOutString());
							System.out.println(
									"\t ->did not match expected stdout regex: " + test.getStdOutExpectedResultRegex());
						}

						// since we have a failed basic test, show the
						// expectation for the stderr
						if (!output.getStdErrString().matches(test.getStdErrExpectedResultRegex())) {
							System.out.println("\t ->stderr: " + output.getStdErrString());
							System.out.println(
									"\t ->did not match expected stderr regex: " + test.getStdErrExpectedResultRegex());
						}
					}
				}
			} else {
				this.failCount++;
				if (!this.quiet) {
					System.out.println("ERROR: Could not initialize test.");
				}
			}
			if (!this.quiet) {
				System.out.println(HORIZONTAL_LINE);
			}
		}
		// print the basic test results and the code coverage associated with
		// the basic tests
		double percentCovered = generateSummaryCodeCoverageResults();
		if (!this.quiet) {
			System.out.println("basic test results: " + (passCount + failCount) + " total, " + passCount + " pass, "
					+ failCount + " fail, " + percentCovered + " percent covered");
			System.out.println(HORIZONTAL_LINE);
		}
	}

	/**
	 * Executes security tests on the jar under test.
	 * <p>
	 * This is the half of the framework that IDT has not completed. We want you
	 * to implement your exploratory security vulnerability testing here. In an
	 * effort to demonstrate some of the features of the framework that you can
	 * already utilize, we have provided some example code in the method. The
	 * examples only demonstrate how to use existing functionality.
	 */
	public void executeSecurityTests() {
		Long start = System.currentTimeMillis();
		TestGenerator generator = new DummyTestGenerator(this.parameterFactory, this.outputs);
		for (int i = 0; i < this.bbTests; i++) {
			if (isKilled.get()) {
				return;
			}

			Object[] params = generator.nextTest();
			instrumentAndExecuteCode(params);
		}

		System.out.println(minutesPassed(start));
		System.out.println(this.timeGoal);
		while (minutesPassed(start) < this.timeGoal) {
			if (isKilled.get()) {
				return;
			}

			Object[] params = generator.nextTest();
			instrumentAndExecuteCode(params);
		}
	}

	/**
	 * Returns YAML toolchain output for this tester.
	 * 
	 * @return String output of this tester
	 */
	public String getYAMLOutput() {
		StringBuffer buffer = new StringBuffer(1000);
		buffer.append("Total predefined tests run: ");
		buffer.append(this.failCount + this.passCount + "\n");
		buffer.append("Number of predefined tests that passed: ");
		buffer.append(this.passCount + "\n");
		buffer.append("Number of predefined tests that failed: ");
		buffer.append(this.failCount + "\n");
		buffer.append("Total code coverage percentage: ");
		buffer.append(percentCovered + "\n");
		buffer.append("Unique error count: ");
		buffer.append(this.exceptionSet.size() + "\n");
		buffer.append("Errors seen:\n");
		for (String error : this.exceptionSet) {
			buffer.append("  -" + error.replace('\n', ' ') + "\n");
		}

		return buffer.toString();
	}

	/**
	 * Returns unmodifiable list of this tester's outputs.
	 * 
	 * @return list of this testers outputs
	 */
	public List<Output> getOutputs() {
		return Collections.unmodifiableList(this.outputs);
	}

	/**
	 * Returns this tester's percent of tests completed.
	 * 
	 * @return percent of tests completed
	 */
	public SimpleDoubleProperty getPercentDone() {
		return percentDone;
	}

	/**
	 * Stops this tester's current testing
	 */
	public void killTests() {
		this.isKilled.set(true);
	}

	/**
	 * Returns if this tester is paused.
	 * 
	 * @return true if this tester is paused or false if not
	 */
	public boolean isPaused() {
		return this.isPaused.get();
	}

	/**
	 * Sets this tester as paused or not.
	 * 
	 * @param paused
	 *            - paused state to set
	 */
	public void setPaused(boolean paused) {
		this.isPaused.set(paused);
	}

	//////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////

	/**
	 * Prints an initialization error.
	 * 
	 * @param message
	 *            - information about the initialization error
	 */
	private void initError(String message) {
		System.out.println("ERROR: " + INIT_ERROR_MSSG);
		System.out.println(message);
	}

	/**
	 * Determines how many minutes have passed since a given
	 * System.currentTimeMillis() output
	 * 
	 * @param start
	 *            - last return of System.currentTimeMillis()
	 * @return minutes since start
	 */
	private int minutesPassed(long start) {
		long diff = (System.currentTimeMillis() - start) / 1_000 / 60;
		return (int) diff;
	}

	/**
	 * Instruments and executes the jar under test with the supplied parameters.
	 * <p>
	 * This method should be used for both basic tests and security tests.
	 * 
	 * An assumption is made in this method that the word java is recognized on
	 * the command line because the user has already set the appropriate
	 * environment variable path.
	 * 
	 * ICT-2 has changed this method such that it can monitor output from a
	 * security handler which watches the jar under test. ICT-2 has also changed
	 * how this method reads I/O from the jar under test, preventing I/O
	 * deadlocks.
	 * 
	 * @param parameters
	 *            - array of Objects to use as parameters for this execution of
	 *            the jar under test
	 * 
	 * @return Output representation of the std out, std error, and security
	 *         notifications encountered during this test
	 * 
	 */
	private Output instrumentAndExecuteCode(Object[] parameters) {
		while (this.isPaused.get()) {
			// do nothing while testing paused
		}

		Process process = null;
		Output output = new Output();

		// we are building up a command line statement that will use java -jar
		// to execute the jar
		// and uses jacoco to instrument that jar and collect code coverage
		// metrics
		StringBuffer cmdBuffer = new StringBuffer(300);
		String command = null;
		cmdBuffer.append("java");
		try {
			if (this.watchdog) {
				if (runningFromJar()) {
					System.out.println("Running from Jar.");
					cmdBuffer.append(" -javaagent:" + this.jacocoAgentJarPath + "=destfile=");
					cmdBuffer.append(this.jacocoOutputFilePath + "temp");
					cmdBuffer.append(" -cp" + " com.idtus.contest.winter2017.framework.jar ");
					cmdBuffer.append(SecurityWatchdog.class.getCanonicalName());
					cmdBuffer.append(" \"" + this.jarToTestPath + "\" " + this.quiet);
				} else {
					cmdBuffer.append(" -javaagent:" + this.jacocoAgentJarPath + "=destfile=");
					cmdBuffer.append(this.jacocoOutputFilePath + "temp");
					cmdBuffer.append(" -cp \"" + this.watchdogPath + "\" ");
					cmdBuffer.append(SecurityWatchdog.class.getCanonicalName());
					cmdBuffer.append(" \"" + this.jarToTestPath + "\" " + this.quiet);
				}
			} else {
				cmdBuffer.append(" -javaagent:" + this.jacocoAgentJarPath + "=destfile=");
				cmdBuffer.append(this.jacocoOutputFilePath + "temp");
				cmdBuffer.append(" -jar " + this.jarToTestPath);
			}

			for (Object o : parameters) {
				cmdBuffer.append(" " + o.toString());
			}

			command = "" + cmdBuffer;

			// show the user the command to run and prepare the process using
			// the command
			if (!this.quiet) {
				System.out.println("command to run: " + command);
			}

			process = Runtime.getRuntime().exec(command);

			InputStream isOut = process.getInputStream();
			InputStream isErr = process.getErrorStream();

			ProcessStreamReader stdOutReader = new ProcessStreamReader(isOut);
			ProcessStreamReader stdErrReader = new ProcessStreamReader(isErr);

			StringBuffer stdOutBuff = new StringBuffer();
			StringBuffer stdErrBuff = new StringBuffer();

			stdOutReader.start();
			stdErrReader.start();

			long start = System.currentTimeMillis();
			while ((System.currentTimeMillis() - start) < this.maxMillisPerTest) {
				String outLine = stdOutReader.pollLine();
				if (outLine != null) {
					if (outLine.equals("<<WATCHDOG_OUTPUT_START>>")) {
						handleWatchdogOutput(stdOutReader, output);
					} else {
						stdOutBuff.append(outLine);
					}
				}

				String errLine = stdErrReader.pollLine();
				if (errLine != null) {
					if (errLine.equals("<<WATCHDOG_OUTPUT_START>>")) {
						handleWatchdogError(stdErrReader);
					} else {
						stdErrBuff.append(errLine + "\n");
					}
				}

				if (stdErrReader.isDone() && stdOutReader.isDone()) {
					break;
				}
			}

			stdErrReader.endProcess();
			stdOutReader.endProcess();

			output.setStdOutString("" + stdOutBuff);
			// trim extra newline character
			if (stdErrBuff.length() != 0) {
				stdErrBuff.deleteCharAt(stdErrBuff.length() - 1);
			}
			output.setStdErrString("" + stdErrBuff);

		} catch (IOException e) {
			if (!this.quiet) {
				System.out.println("ERROR: IOException has prevented execution of the command: " + command);
			}
			e.printStackTrace();
			return null;
		} catch (WatchdogException e) {
			if (!this.quiet) {
				System.out.println("ERROR: WatchdogException has prevented execution of the command: " + command);
			}
			e.printStackTrace();
			return null;
		}

		File toLoad = new File(this.jacocoOutputFilePath + "temp");
		File toSave = new File(this.jacocoOutputFilePath);

		// wait while the file is locked
		while (!toSave.renameTo(toSave)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException ie) {
			}
		}

		try {
			ExecFileLoader loader = new ExecFileLoader();
			loader.load(toLoad);
			loader.save(toSave, true);
			CoverageBuilder builder = new CoverageBuilder();
			Analyzer analyzer = new Analyzer(loader.getExecutionDataStore(), builder);
			analyzer.analyzeAll(new File(this.jarToTestPath));

			// omit test bounds from coverage
			IClassCoverage testBounds = null;
			for (final IClassCoverage cc : builder.getClasses()) {
				if (cc.getName().endsWith("TestBounds")) {
					testBounds = cc;
					break;
				}
			}
			if (testBounds != null) {
				builder.getClasses().remove(testBounds);
			}

			output.setCoverageBuilder(builder);
		} catch (IOException e) {
			if (!this.quiet) {
				System.out.println("ERROR: Unable to save and load Jacoco output.");
			}
			e.printStackTrace();
			output.setCoverageBuilder(null);
		}

		this.outputs.add(output);
		this.exceptionSet.addAll(output.getExceptions());

		double totalTests = this.bbTests + this.predefinedTests.size();
		percentDone.set(outputs.size() / totalTests);

		return output;
	}

	/**
	 * Returns whether this class is running from a Jar.
	 * 
	 * @return boolean whether this class is running from a jar.
	 */
	private boolean runningFromJar() {
		String name = getClass().getName();
		name = name.replace('.', '/');
		String path = "" + Tester.class.getResource("/" + name + ".class");
		System.out.println(path);
		if (path.startsWith("jar:") || path.startsWith("rsrc:") || path.endsWith(".jar")) {
			return true;
		}
		return false;
	}

	/**
	 * Handles standard output specifically for the tester.
	 * 
	 * @param brOut
	 * @param output
	 * @throws IOException
	 */
	private void handleWatchdogOutput(ProcessStreamReader brOut, Output output) throws IOException {
		String next;
		output.resetPermissionLog();
		while (!"<<WATCHDOG_OUTPUT_END>>".equals(next = brOut.pollLine())) {
			if (next == null) {
				continue;
			}
			output.logPermission(next);
		}
	}

	/**
	 * Handles standard error output specifically for the tester.
	 * 
	 * Method used to handle errors passed by the security watchdog
	 * 
	 * @param error
	 *            - the error to handle
	 */
	private void handleWatchdogError(ProcessStreamReader brErr) throws IOException, WatchdogException {
		String next;
		StringBuffer errBuff = new StringBuffer();
		while (!"<<WATCHDOG_OUTPUT_END>>".equals(next = brErr.pollLine())) {
			if (next == null) {
				continue;
			}
			errBuff.append(next + "\n");
		}

		// TODO: handle Watchdog Error
		throw new WatchdogException("" + errBuff);
	}

	/**
	 * Prints the basic test output (std out/err).
	 * 
	 * @param output
	 *            - Output object containing std out/err to print
	 */
	private void printBasicTestOutput(Output output) {
		if (!this.quiet) {
			System.out.println("stdout of execution: " + output.getStdOutString());
			System.out.println("stderr of execution: " + output.getStdErrString());
			System.out.println("permissions used: " + output.getPermissionLogString());
		}
	}

	/**
	 * Prints raw code coverage stats including hits/probes
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private void printRawCoverageStats() {
		if (!this.quiet) {
			System.out.printf("exec file: %s%n", this.jacocoOutputFilePath);
			System.out.println("CLASS ID         HITS/PROBES   CLASS NAME");
		}

		try {
			File executionDataFile = new File(this.jacocoOutputFilePath);
			final FileInputStream in = new FileInputStream(executionDataFile);
			final ExecutionDataReader reader = new ExecutionDataReader(in);
			reader.setSessionInfoVisitor(new ISessionInfoVisitor() {
				public void visitSessionInfo(final SessionInfo info) {
					if (!Tester.this.quiet) {
						System.out.printf("Session \"%s\": %s - %s%n", info.getId(), new Date(info.getStartTimeStamp()),
								new Date(info.getDumpTimeStamp()));
					}
				}
			});
			reader.setExecutionDataVisitor(new IExecutionDataVisitor() {
				public void visitClassExecution(final ExecutionData data) {
					if (!Tester.this.quiet) {
						System.out.printf("%016x  %3d of %3d   %s%n", Long.valueOf(data.getId()),
								Integer.valueOf(getHitCount(data.getProbes())),
								Integer.valueOf(data.getProbes().length), data.getName());
					}
				}
			});
			reader.read();
			in.close();
		} catch (IOException e) {
			if (!this.quiet) {
				System.out.println("Unable to display raw coverage stats due to IOException related to "
						+ this.jacocoOutputFilePath);
			}
			e.printStackTrace();
		}
		if (!this.quiet) {
			System.out.println();
		}
	}

	/**
	 * Gets the hit count from the code coverage metrics.
	 * 
	 * @param data
	 *            - boolean array of coverage data where true indicates hits
	 * @return int representation of count of total hits from supplied data
	 */
	private int getHitCount(final boolean[] data) {
		int count = 0;
		for (final boolean hit : data) {
			if (hit) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Generates code coverage metrics including instructions, branches, lines,
	 * methods and complexity.
	 * 
	 * @return double representation of the percentage of code covered during
	 *         testing
	 */
	private double generateSummaryCodeCoverageResults() {
		long total = 0;
		long covered = 0;
		try {
			// creating a new file for output in the jacoco output directory
			// (one of the application arguments)
			File executionDataFile = new File(this.jacocoOutputFilePath);
			ExecFileLoader execFileLoader = new ExecFileLoader();
			execFileLoader.load(executionDataFile);

			// use CoverageBuilder and Analyzer to assess code coverage from
			// jacoco output file
			final CoverageBuilder coverageBuilder = new CoverageBuilder();
			final Analyzer analyzer = new Analyzer(execFileLoader.getExecutionDataStore(), coverageBuilder);

			// analyzeAll is the way to go to analyze all classes inside a
			// container (jar or zip or directory)
			analyzer.analyzeAll(new File(this.jarToTestPath));

			for (final IClassCoverage cc : coverageBuilder.getClasses()) {

				// report code coverage from all classes that are not the
				// TestBounds class within the jar
				if (!cc.getName().endsWith("TestBounds")) {
					total += cc.getInstructionCounter().getTotalCount();
					total += cc.getBranchCounter().getTotalCount();
					total += cc.getLineCounter().getTotalCount();
					total += cc.getMethodCounter().getTotalCount();
					total += cc.getComplexityCounter().getTotalCount();

					covered += cc.getInstructionCounter().getCoveredCount();
					covered += cc.getBranchCounter().getCoveredCount();
					covered += cc.getLineCounter().getCoveredCount();
					covered += cc.getMethodCounter().getCoveredCount();
					covered += cc.getComplexityCounter().getCoveredCount();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		percentCovered = ((double) covered / (double) total) * 100.0;
		return percentCovered;
	}

	/**
	 * Shows an example of how to generate code coverage metrics from Jacoco.
	 * 
	 * @return String representing code coverage results
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private String generateDetailedCodeCoverageResults() {
		String executionResults = "";
		try {
			File executionDataFile = new File(this.jacocoOutputFilePath);
			ExecFileLoader execFileLoader = new ExecFileLoader();
			execFileLoader.load(executionDataFile);

			final CoverageBuilder coverageBuilder = new CoverageBuilder();
			final Analyzer analyzer = new Analyzer(execFileLoader.getExecutionDataStore(), coverageBuilder);

			analyzer.analyzeAll(new File(this.jarToTestPath));

			for (final IClassCoverage cc : coverageBuilder.getClasses()) {
				executionResults += "Coverage of class " + cc.getName() + ":\n";
				executionResults += getMetricResultString("instructions", cc.getInstructionCounter());
				executionResults += getMetricResultString("branches", cc.getBranchCounter());
				executionResults += getMetricResultString("lines", cc.getLineCounter());
				executionResults += getMetricResultString("methods", cc.getMethodCounter());
				executionResults += getMetricResultString("complexity", cc.getComplexityCounter());

				// adding this to a string is a little impractical with the size
				// of some of the files, so we are commenting it out, but it
				// shows that you can get the coverage status of each line if
				// you wanted to add debug argument to display this level of
				// detail at command line level....
				//
				// for (int i = cc.getFirstLine(); i <= cc.getLastLine(); i++) {
				// executionResults += "Line " + Integer.valueOf(i) + ": " +
				// getStatusString(cc.getLine(i).getStatus()) + "\n";
				// }
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return executionResults;
	}

	/**
	 * Translates the Jacoco line coverage status integers to Strings.
	 * 
	 * @param status
	 *            - integer representation of line coverage status provided by
	 *            Jacoco
	 * @return String representation of line coverage status (not covered,
	 *         partially covered, fully covered)
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private String getStatusString(final int status) {
		switch (status) {
		case ICounter.NOT_COVERED:
			return "not covered";
		case ICounter.PARTLY_COVERED:
			return "partially covered";
		case ICounter.FULLY_COVERED:
			return "fully covered";
		}
		return "";
	}

	/**
	 * Translates the counter data and units into a human readable metric result
	 * String.
	 * 
	 * @param unit
	 * @param counter
	 * @return
	 */
	private String getMetricResultString(final String unit, final ICounter counter) {
		final Integer missedCount = Integer.valueOf(counter.getMissedCount());
		final Integer totalCount = Integer.valueOf(counter.getTotalCount());
		return missedCount.toString() + " of " + totalCount.toString() + " " + unit + " missed\n";
	}

	/*
	 * Is not meant to be part of the final framework. It was included to
	 * demonstrate three different ways to tap into the code coverage
	 * results/metrics using jacoco.
	 * 
	 * This method is deprecated and will be removed from the final product
	 * after your team completes development. Please do not add additional
	 * dependencies to this method.
	 */

	/*
	 * @Deprecated private void showCodeCoverageResultsExample() {
	 * 
	 * // Below is the first example of how to tap into code coverage metrics
	 * double result = generateSummaryCodeCoverageResults(); if
	 * (!this.toolChain) { System.out.println("\n");
	 * System.out.println("percent covered: " + result); }
	 * 
	 * // Below is the second example of how to tap into code coverage metrics
	 * if (!this.toolChain) { System.out.println("\n"); }
	 * printRawCoverageStats();
	 * 
	 * // Below is the third example of how to tap into code coverage metrics if
	 * (!this.toolChain) { System.out.println("\n");
	 * System.out.println(generateDetailedCodeCoverageResults()); } }
	 */
}

/**
 * Class to read output from a process without causing the input stream to
 * block.
 * 
 * @author ICT-2
 */
class ProcessStreamReader extends Thread {
	/**
	 * Input stream to read from.
	 */
	private InputStream iStream;

	/**
	 * Boolean whether this thread should stop due to time constraints.
	 */
	private AtomicBoolean timeUp;

	/**
	 * Boolean whether this thread is done reading.
	 */
	private AtomicBoolean isDone;

	/**
	 * List of lines read from the process.
	 */
	private List<String> lines;

	/**
	 * Constructs a proces stream reader with the given input stream.
	 * 
	 * @param iStream
	 *            - input stream to read from
	 */
	public ProcessStreamReader(InputStream iStream) {
		this.iStream = iStream;
		this.timeUp = new AtomicBoolean(false);
		this.isDone = new AtomicBoolean(false);
		this.lines = Collections.synchronizedList(new LinkedList<String>());
	}

	/**
	 * Reads from the given input stream.
	 * <p>
	 * Stops reading once the stream ends or time is up.
	 */
	@Override
	public void run() {
		InputStreamReader iReader = new InputStreamReader(this.iStream);
		BufferedReader bReader = new BufferedReader(iReader);
		String line;
		try {
			while ((line = bReader.readLine()) != null && !this.timeUp.get()) {
				lines.add(line);
			}
		} catch (Exception e) {
		}
		this.isDone.set(true);
	}

	/**
	 * Notifies the process stream reader that time is up.
	 */
	public void endProcess() {
		this.timeUp.set(true);
	}

	/**
	 * Gets and removes the next line from the proess stream reader.
	 * 
	 * @return the next line from the process stream reader
	 */
	public String pollLine() {
		if (lines.isEmpty()) {
			return null;
		} else {
			return lines.remove(0);
		}
	}

	/**
	 * Returns whether this process stream reader is done.
	 * 
	 * @return true if this process stream reader is done, or false if not.
	 */
	public boolean isDone() {
		return this.isDone.get();
	}
}

/**
 * This class represents an exception thrown by the security watchdog, and not
 * the jar under test.
 * 
 * @author ICT-2
 */
@SuppressWarnings("serial")
class WatchdogException extends Exception {
	// no overriden methods
	public WatchdogException(String error) {
		super(error);
	}
}
