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
	 * Last output to help keep track of last test
	 */
	private Output lastOutput = null;

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
		if (this.outputs.size() != 0) {
			this.lastOutput = outputs.get(0);
		}
	}

	/**
	 * Gets the next test to be run.
	 * <p>
	 * When a test is run, the associated Output object is added to the end of
	 * this class's outputs list.
	 * 
	 * Some algorithm ideas:
	 * <ul>
	 * <li>https://www.tutorialspoint.com/software_testing_dictionary/black_box_testing.htm</li>
	 * <li>https://www.tutorialspoint.com/software_testing_dictionary/boundary_testing.htm</li>
	 * <li>weighted tree-search</li>
	 * </ul>
	 * 
	 * @return an array of objects which represents parameters to be tested.
	 */
	public abstract Object[] nextTest();

	/**
	 * Updates the list of outputs.
	 */
	public void updateOutputs() {
		Output end;
		if (this.outputs.isEmpty()) {
			end = null;
		} else {
			end = this.outputs.get(this.outputs.size() - 1);
		}

		if (lastOutput != end) {
			this.lastOutput = end;
			this.outputs.add(end);
		}
	}

	/**
	 * Returns a random integer number.
	 * <p>
	 * If the passed Parameter has an associated minimum or maximum, these
	 * values are used as the bounds of the random number.
	 * 
	 * @param param
	 *            - parameter to get possible minimum and maximum from
	 * 
	 * @return an integer within the bounds given by the parameter
	 */
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

	/**
	 * Returns a random double number.
	 * <p>
	 * If the passed Parameter has an associated minimum or maximum, these
	 * values are used as the bounds of the random number.
	 * 
	 * @param param
	 *            - parameter to get possible minimum and maximum from
	 * 
	 * @return a double within the bounds given by the parameter
	 */
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

	/**
	 * Returns the parameter factory of this test generator.
	 * 
	 * @return the parameter factory of this test generator
	 */
	public ParameterFactory getParameterFactory() {
		return this.parameterFactory;
	}

	/**
	 * Returns the list of outputs held by this test generator.
	 * 
	 * @return the list of outputs held by this test generator
	 */
	public List<Output> getOutputs() {
		return this.outputs;
	}
}