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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

import edu.emory.mathcs.backport.java.util.Collections;
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
	 * Maximum time per test, currently not implemented.
	 */
	private Integer maxMillisPerTest = 1000;

	/**
	 * Option to only use toolchain output.
	 */
	private Boolean toolChain = false;

	/**
	 * Number of predefined tests which have passed.
	 */
	private int passCount = 0;

	/**
	 * Number of predefined tests which have failed.
	 */
	private int failCount = 0;

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
	public boolean init(String initJarToTestPath, String initJacocoOutputDirPath, String initJacocoAgentJarPath,
			String initbbTests, String initTimeGoal, String initToolChain) {

		this.watchdogPath = Tester.class.getResource("Tester.class").getPath();
		this.watchdogPath = this.watchdogPath.replace("contest/winter2017/Tester.class", "");
		this.jarToTestPath = initJarToTestPath;
		this.jacocoOutputDirPath = initJacocoOutputDirPath;
		this.jacocoAgentJarPath = initJacocoAgentJarPath;

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

		// parse toolChain argument
		this.toolChain = initToolChain.equals("true");

		// delete previous jacoco outputs
		File jarFileToTest = new File(this.jarToTestPath);
		this.jacocoOutputFilePath = this.jacocoOutputDirPath + "\\" + jarFileToTest.getName().replaceAll("\\.", "_")
				+ JACOCO_OUTPUT_FILE_SUFFIX;
		File jacocoOutputFile = new File(this.jacocoOutputFilePath);
		if (jacocoOutputFile != null && jacocoOutputFile.exists()) {
			jacocoOutputFile.delete();
		}

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
		Map<String, Object> mainClassTestBoundsMap = null;
		try {
			mainClassTestBoundsMap = (Map<String, Object>) testBoundsMethod.invoke(mainClassTestBoundsInstance);
		} catch (ExceptionInInitializerError | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			initError("Could not invoke method testBounds from [MainClassName]TestBounds class.");
			return false;
		}

		// instantiating a new Parameter Factory using the Test Bounds map
		this.parameterFactory = new ParameterFactory(mainClassTestBoundsMap);

		// get a list of basic tests from the TestBounds class
		this.predefinedTests = new ArrayList<Test>();
		List testList = (List) mainClassTestBoundsMap.get("tests");
		for (Object inTest : testList) {
			this.predefinedTests.add(new Test((Map) inTest));
		}

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

			// instrument the code to code coverage metrics, execute the test
			// with given parameters, then show the output
			Output output = instrumentAndExecuteCode(test.getParameters().toArray());
			if (output != null) {
				printBasicTestOutput(output);

				// determine the result of the test based on expected
				// output/error regex
				if (output.getStdOutString().matches(test.getStdOutExpectedResultRegex())
						&& output.getStdErrString().matches(test.getStdErrExpectedResultRegex())) {
					if (!this.toolChain) {
						System.out.println("basic test result: PASS");
					}

					this.passCount++;
				} else {
					if (!this.toolChain) {
						System.out.println("basic test result: FAIL ");
					}

					this.failCount++;

					if (!this.toolChain) {
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
				if (!this.toolChain) {
					System.out.println("ERROR: Could not initialize test.");
				}
			}
			if (!this.toolChain) {
				System.out.println(HORIZONTAL_LINE);
			}
		}
		// print the basic test results and the code coverage associated with
		// the basic tests
		double percentCovered = generateSummaryCodeCoverageResults();
		if (!this.toolChain) {
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
		// List<ParameterList> possibleParamLists =
		// this.parameterFactory.possibleParamLists();
		// ParameterList parameterList = possibleParamLists.get(0);
		Long start = System.currentTimeMillis();

		TestGenerator generator = new TestGenerator(this.parameterFactory, getOutputs());
		for (int i = 0; i < this.bbTests; i++) {
			Object[] params = generator.nextTest();
			// instrumentAndExecuteCode(params);
		}

		while (minutesPassed(start) < this.timeGoal) {
			Object[] params = generator.nextTest();
			// instrumentAndExecuteCode(params);
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

	//////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////

	/**
	 * Reports an initialization error, and then quits the program.
	 * 
	 * @param message
	 *            - information about the initialization error
	 */
	private void initError(String message) {
		System.out.println("ERROR: " + INIT_ERROR_MSSG);
		System.out.print(message);
	}

	/**
	 * Determine how many minutes have passed since a given
	 * System.currentTimeMillis() output
	 * 
	 * @param start
	 *            - last return of System.currentTimeMillis()
	 * @return minutes since start
	 */
	private int minutesPassed(long start) {
		long diff = (System.currentTimeMillis() - start) / 1_000_000;
		return (int) diff;
	}

	/**
	 * Instruments and executes the jar under test with the supplied parameters.
	 * This method should be used for both basic tests and security tests.
	 * 
	 * An assumption is made in this method that the word java is recognized on
	 * the command line because the user has already set the appropriate
	 * environment variable path.
	 * 
	 * ICT-2 has changed this method such that it monitors output from a
	 * security handler which watches the jar under test.
	 * 
	 * @param parameters
	 *            - array of Objects that represents the parameter values to use
	 *            for this execution of the jar under test
	 * 
	 * @return Output representation of the standard out and standard error
	 *         associated with the, in addition to security notifications
	 */
	private Output instrumentAndExecuteCode(Object[] parameters) {
		Process process = null;
		Output output = new Output();

		// we are building up a command line statement that will use java -jar
		// to execute the jar
		// and uses jacoco to instrument that jar and collect code coverage
		// metrics
		String command = "java";
		try {
			// http://stackoverflow.com/questions/6780678/run-class-in-jar-file

			if (runningFromJar()) {
				System.out.println("Running from Jar.");
				command += " -javaagent:" + this.jacocoAgentJarPath + "=destfile=" + this.jacocoOutputFilePath;
				command += " -cp" + " com.idtus.contest.winter2017.framework.jar ";
				command += SecurityWatchdog.class.getCanonicalName();
				command += " \"" + this.jarToTestPath + "\" " + this.toolChain;
			} else {
				command += " -javaagent:" + this.jacocoAgentJarPath + "=destfile=" + this.jacocoOutputFilePath;
				command += " -cp \"" + this.watchdogPath + "\" ";
				command += SecurityWatchdog.class.getCanonicalName();
				command += " \"" + this.jarToTestPath + "\" " + this.toolChain;
			}

			for (Object o : parameters) {
				command += " " + o.toString();
			}

			// show the user the command to run and prepare the process using
			// the command

			if (!this.toolChain) {
				System.out.println("command to run: " + command);
			}

			process = Runtime.getRuntime().exec(command);

			// prepare the stream needed to capture standard output
			InputStream isOut = process.getInputStream();
			InputStreamReader isrOut = new InputStreamReader(isOut);
			BufferedReader brOut = new BufferedReader(isrOut);
			StringBuffer stdOutBuff = new StringBuffer();

			// prepare the stream needed to capture standard error
			InputStream isErr = process.getErrorStream();
			InputStreamReader isrErr = new InputStreamReader(isErr);
			BufferedReader brErr = new BufferedReader(isrErr);
			StringBuffer stdErrBuff = new StringBuffer();

			String line;
			boolean outDone = false;
			boolean errDone = false;

			// while standard out is not complete OR standard error is not
			// complete
			// continue to probe the output/error streams for the applications
			// output
			int i = 0;
			while ((!outDone || !errDone)) {
				// monitoring the standard output from the application
				boolean outReady = brOut.ready();
				if (outReady) {
					line = brOut.readLine();
					if (line == null) {
						outDone = true;
					} else if (line.equals("<<WATCHDOG_OUTPUT_START>>")) {
						handleWatchdogOutput(brOut, output);
					} else {
						stdOutBuff.append(line);
					}
				}

				// monitoring the standard error from the application
				boolean errReady = brErr.ready();
				if (errReady) {
					line = brErr.readLine();
					if (line == null) {
						errDone = true;
					} else {
						if (line.equals("<<WATCHDOG_OUTPUT_START>>")) {
							handleWatchdogError(brErr);
						} else {
							stdErrBuff.append(line + "\n");
						}
					}
				}

				// if standard out and standard error are not ready, wait for
				// 250ms
				// and try again to monitor the streams
				if (!outReady && !errReady) {
					i++;
					if (i > 6) {
						break;
					}
					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
						// NOP
					}
				}
			}

			while (brErr.ready()) {
				stdErrBuff.append(brErr.readLine());
			}

			// we now have the output as an object from the run of the black-box
			// jar
			// this output object contains both the standard output and the
			// standard error

			output.setStdOutString(stdOutBuff.toString());
			// trim extra newline character
			if (stdErrBuff.length() != 0) {
				stdErrBuff.deleteCharAt(stdErrBuff.length() - 1);
			}
			output.setStdErrString(stdErrBuff.toString());

		} catch (IOException e) {
			if (!this.toolChain) {
				System.out.println("ERROR: IOException has prevented execution of the command: " + command);
			}
			e.printStackTrace();
			return null;
		} catch (WatchdogException e) {
			if (!this.toolChain) {
				System.out.println("ERROR: WatchdogException has prevented execution of the command: " + command);
			}
			e.printStackTrace();
			return null;
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
	private void handleWatchdogOutput(BufferedReader brOut, Output output) throws IOException {
		String next;
		output.resetPermissionLog();
		while (!(next = brOut.readLine()).equals("<<WATCHDOG_OUTPUT_END>>")) {
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
	private void handleWatchdogError(BufferedReader brErr) throws IOException, WatchdogException {
		String next;
		StringBuffer errBuff = new StringBuffer();
		while (!(next = brErr.readLine()).equals("<<WATCHDOG_OUTPUT_END>>")) {
			errBuff.append(next + "\n");
		}
		System.err.println(errBuff);

		// TODO: handle Watchdog Error
		throw new WatchdogException();
	}

	/**
	 * Prints the basic test output (std out/err).
	 * 
	 * @param output
	 *            - Output object containing std out/err to print
	 */
	private void printBasicTestOutput(Output output) {
		if (!this.toolChain) {
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
	private void printRawCoverageStats() {
		if (!this.toolChain) {
			System.out.printf("exec file: %s%n", this.jacocoOutputFilePath);
			System.out.println("CLASS ID         HITS/PROBES   CLASS NAME");
		}

		try {
			File executionDataFile = new File(this.jacocoOutputFilePath);
			final FileInputStream in = new FileInputStream(executionDataFile);
			final ExecutionDataReader reader = new ExecutionDataReader(in);
			reader.setSessionInfoVisitor(new ISessionInfoVisitor() {
				public void visitSessionInfo(final SessionInfo info) {
					if (!Tester.this.toolChain) {
						System.out.printf("Session \"%s\": %s - %s%n", info.getId(), new Date(info.getStartTimeStamp()),
								new Date(info.getDumpTimeStamp()));
					}
				}
			});
			reader.setExecutionDataVisitor(new IExecutionDataVisitor() {
				public void visitClassExecution(final ExecutionData data) {
					if (!Tester.this.toolChain) {
						System.out.printf("%016x  %3d of %3d   %s%n", Long.valueOf(data.getId()),
								Integer.valueOf(getHitCount(data.getProbes())),
								Integer.valueOf(data.getProbes().length), data.getName());
					}
				}
			});
			reader.read();
			in.close();
		} catch (IOException e) {
			if (!this.toolChain) {
				System.out.println("Unable to display raw coverage stats due to IOException related to "
						+ this.jacocoOutputFilePath);
			}
			e.printStackTrace();
		}
		if (!this.toolChain) {
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
				// of some of the files,
				// so we are commenting it out, but it shows that you can get
				// the coverage status of each line
				// if you wanted to add debug argument to display this level of
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
	 * Translate the Jacoco line coverage status integers to Strings.
	 * 
	 * @param status
	 *            - integer representation of line coverage status provided by
	 *            Jacoco
	 * @return String representation of line coverage status (not covered,
	 *         partially covered, fully covered)
	 */
	@SuppressWarnings("unused")
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
	 * Translate the counter data and units into a human readable metric result
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
	 *
	 * 
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
 * Thic class represents an exception thrown by the security watchdog, and not
 * the jar under test.
 * 
 * @author ICT-2
 */
@SuppressWarnings("serial")
class WatchdogException extends Exception {
	// no overriden methods
}
