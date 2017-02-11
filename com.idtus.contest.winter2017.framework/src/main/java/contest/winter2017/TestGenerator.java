package contest.winter2017;

import java.util.List;
import java.util.Random;

/**
 * Abstract class to generate tests to be run from the Tester.
 * 
 * @author ICT-2
 */
public abstract class TestGenerator {
	/**
	 * ParameterFactory to get info about inputs to jar to test.
	 */
	private ParameterFactory parameterFactory;

	/**
	 * Synchronized list of automatically updated outputs from tests run.
	 */
	private List<Output> outputs;

	/**
	 * Constructs a TestGenerator with the given parameter factory and list of
	 * outputs.
	 * 
	 * @param parameterFactory
	 *            - parameter factory for this test generator.
	 * 
	 * @param outputs
	 *            - list of outputs encountered by any tests run outputs is
	 *            updated automatically by the tester
	 */
	public TestGenerator(ParameterFactory parameterFactory, List<Output> outputs) {
		this.parameterFactory = parameterFactory;
		this.outputs = outputs;
	}

	/**
	 * Gets the next test to be run.
	 * <p>
	 * 
	 * When a test is run, the associated Output object is added to the end of
	 * this class's outputs list.
	 * 
	 * Some algo ideas:
	 * https://www.tutorialspoint.com/software_testing_dictionary/black_box_testing.htm
	 * 
	 * Parameters may come with a min and max for their numbers:
	 * https://www.tutorialspoint.com/software_testing_dictionary/boundary_testing.htm
	 * 
	 * Another idea: Test simple cases, dedicate more time to those which cover
	 * more code (using Jacoco).
	 * 
	 * @return an array of objects which represents parameters to be tested.
	 */
	public abstract Object[] nextTest();

	//
	// Utility method to get a random integer number. If the passed
	// Parameter has an associated min or max, these values are used
	// as the bounds of the random number.
	//
	// @param param - parameter to get possible min and max from
	// @return Integer within bounds given by param
	//
	private Integer getIntegerNumber(Parameter param) {
		Random rand = new Random();
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
			throw new IllegalArgumentException("ERROR: Parameter's min cannot be bigger than its max.");
		}

		return min + rand.nextInt(max - min + 1);
	}

	//
	// Utility method to get a random double number. If the passed
	// Parameter has an associated min or max, these values are used
	// as the bounds of the random number.
	//
	// @param param - parameter to get possible min and max from
	// @return Double within bounds given by param
	//
	private Double getDoubleNumber(Parameter param) {
		Random rand = new Random();
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
			throw new IllegalArgumentException("ERROR: Parameter's min cannot be bigger than its max.");
		}

		return min + (max - min) * rand.nextDouble();
	}

	public ParameterFactory getParameterFactory() {
		return this.parameterFactory;
	}

	public List<Output> getOutputs() {
		return this.outputs;
	}
}