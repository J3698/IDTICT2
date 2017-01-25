package contest.winter2017;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class BlackBoxExplorer {
	private static final String[] specialStringValues = {
			"", "   ", "zzzzzzz", "*uw&we \n w\r ae\n\ns\tl\te\"f ko \"e",
			"abcdefghijklmnopqrstuvwxyz", "0123456789", "\n\r\t\"",
			"`~!@#$%^&*()_+-=[]\\{}|;':\",./<>?"
	};

	private static final double[] specialDoubleValues = {
	};

	private Tester tester;
	private String jarToTestPath;
	private ParameterFactory parameterFactory;
	private boolean bbTestsPsecified;
	private boolean timeGoalSpecified;
	private int bbTests;
	private int timeGoal;
	private Random rand;

	public BlackBoxExplorer(Tester tester, String jarToTestPath, ParameterFactory parameterFactory,
						boolean bbTestsSpecified, boolean timeGoalSpecified, int bbTests, int timeGoal) {

		this.tester = tester;
		this.jarToTestPath = jarToTestPath;
		this.parameterFactory = parameterFactory;
		this.bbTests = 1000;
		this.timeGoal = 5;
		this.timeGoalSpecified = timeGoalSpecified;
		if (timeGoalSpecified) {
			this.timeGoal = timeGoal;
		}
		this.bbTestsPsecified = bbTestsSpecified;
		if (bbTestsSpecified) {
			this.bbTests = bbTests;
		}

		this.rand = new Random();
	}

	public BlackBoxExplorer(Tester tester, String jarToTestPath, ParameterFactory parameterFactory) {
		this(tester, jarToTestPath, parameterFactory, false, false, 0, 0);
	}

	/**
	 * Method used to randomly explore the jar under test.
	 */
	public void exploreByFizzing() {
		Long start = System.currentTimeMillis();

		List<ParameterList> possibleParamLists = this.parameterFactory.possibleParamLists();
		ParameterList parameterList = possibleParamLists.get(0);

		for (int i = 0; i < this.bbTests; i++) {
			performTest(parameterList);
		}

		while (minutesPassed(start) < this.timeGoal) {
			performTest(parameterList);
		}
	}

	private void performTest(ParameterList parameterList) {
		List<String> arguments = new LinkedList<String>();
		for (Parameter parameter : parameterList) {
			if (parameter.isFormatted()) {
				List<Object> paramValues = new ArrayList<>();
				for (Class var : parameter.getFormatVariables()) {
					if (var.equals(String.class)) {
						paramValues.add(randSpecialString());
					} else if (var.equals(Double.class)) {
						paramValues.add(getDoubleNumber(parameter));
					} else {
						paramValues.add(getIntegerNumber(parameter));
					}
				}
			} else {
				
			}
			arguments.add(parameter.getFormattedParameter(paramValues));
		}
		for (String argument : arguments) {
			System.out.println(argument);
		}
		System.exit(0);
		this.tester.instrumentAndExecuteCode(arguments.toArray(new String[]{}));
	}

	/**
	 * Utility method to get a random integer number. If the passed
	 * Parameter has an associated min or max, these values are used
	 * as the bounds of the random number.
	 * 
	 * @param param - parameter to get possible min and max from
	 * @return Integer within bounds given by param
	 */
	private Integer getIntegerNumber(Parameter param) {
		Integer min;
		try {
			min = Integer.parseInt((String) param.getMin());
		} catch (NumberFormatException nfe) {
			min = null;
		}

		Integer max;
		try {
			max = Integer.parseInt((String) param.getMax());
		} catch (NumberFormatException nfe) {
			max = null;
		}

		if (min == null) {
			min = Integer.MIN_VALUE;
		}
		if (max == null) {
			max = Integer.MAX_VALUE;
		}

		if (max < min) {
			throw new IllegalArgumentException(
					"ERROR: Parameter's min cannot be bigger than its max.");
		}

		return min + rand.nextInt(max - min + 1);
	}

	/**
	 * Utility method to get a random double number. If the passed
	 * Parameter has an associated min or max, these values are used
	 * as the bounds of the random number.
	 * 
	 * @param param - parameter to get possible min and max from
	 * @return Double within bounds given by param
	 */
	private Double getDoubleNumber(Parameter param) {
		Double min;
		try {
			min = Double.parseDouble((String) param.getMin());
		} catch (NumberFormatException nfe) {
			min = null;
		}

		Double max;
		try {
			max = Double.parseDouble((String) param.getMax());
		} catch (NumberFormatException nfe) {
			max = null;
		}

		if (min == null) {
			min = Double.MIN_VALUE;
		}
		if (max == null) {
			max = Double.MAX_VALUE;
		}

		if (max < min) {
			throw new IllegalArgumentException(
					"ERROR: Parameter's min cannot be bigger than its max.");
		}

		return min + (max - min) * rand.nextDouble();
	}


	/**
	 * Utility method used to get a special String value
	 * 
	 * @return special String value
	 */
	private String randSpecialString() {
		int choice = this.rand.nextInt(specialStringValues.length);
		return specialStringValues[choice];
	}

	/**
	 * Utility method used to determine how many minutes
	 * have passed since a given long when System.currentTimeMillis()
	 * was called.
	 * 
	 * @param start - last return of System.currentTimeMillis()
	 * @return minutes since start
	 */
	private double minutesPassed(long start) {
		long diff = (System.currentTimeMillis() - start) / 1_000_000;
		return (double) diff;
	}
}




