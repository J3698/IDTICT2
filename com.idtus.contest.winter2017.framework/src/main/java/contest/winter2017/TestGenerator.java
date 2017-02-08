package contest.winter2017;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to generate tests to be run from the Tester.
 * 
 * @author ICT-2
 */
public class TestGenerator {
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
	 * Currently returns the same test every time for a given jar.
	 * 
	 * When a test is run, the associated Output object is added to the end of
	 * this class's outputs list. See the header comment of the Output class.
	 * 
	 * When using output.getCoverageBuilder(), see Tester.java for the method
	 * generateSummaryCodeCoverageResults() for reference. In particular, note
	 * how they don't look at the results from the TestBounds class.
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
	public Object[] nextTest() {
		/////////// START EXAMPLE CODE /////////////

		// This example demonstrates how to use the ParameterFactory to figure
		// out the parameter types of parameters
		// for each of the jars under test - this can be a difficult task
		// because of the concepts of fixed and
		// dependent parameters (see the explanation at the top of the
		// ParameterFactory class). As we figure out
		// what each parameter type is, we are assigning it a simple (dumb)
		// value so that we can use those parameters
		// to execute the black-box jar. By the time we finish this example, we
		// will have an array of concrete
		// parameters that we can use to execute the black-box jar.

		// start with a blank parameter list since we are going to start with
		// the first parameter
		List<String> previousParameterStrings = new ArrayList<String>();
		List<Parameter> potentialParameters = this.parameterFactory.getNext(previousParameterStrings);
		Parameter potentialParameter;
		while (!potentialParameters.isEmpty()) {
			String parameterString = "";
			potentialParameter = potentialParameters.get(0);

			// if(potentialParameter.isOptional()) //TODO? - your team might
			// want to look at this flag and handle it as well!

			// an enumeration parameter is one that has multiple options
			if (potentialParameter.isEnumeration()) {
				// dumb logic - given a list of options, always use the first
				// one
				parameterString = potentialParameter.getEnumerationValues().get(0) + " ";

				// if the parameter has internal format (eg.
				// "<number>:<number>PM EST")
				if (potentialParameter.isFormatted()) {

					// loop over the areas of the format that must be replaced
					// and choose values
					List<Object> formatVariableValues = new ArrayList<Object>();
					for (Class<?> type : potentialParameter.getFormatVariables(parameterString)) {
						if (type == Integer.class) {
							// dumb logic - always use 1 for an integer
							formatVariableValues.add(new Integer(1));
						} else if (type == String.class) {
							// dumb logic - always use 'one' for an integer
							formatVariableValues.add(new String("one"));
						}
					}

					// build the formatted parameter string with the chosen
					// values (eg. 1:1PM EST)
					parameterString = potentialParameter.getFormattedParameter(parameterString, formatVariableValues);
				}
				previousParameterStrings.add(parameterString);
				// if it is not an enumeration parameter, it is either an
				// Integer, Double, or String
			} else {
				if (potentialParameter.getType() == Integer.class) {
					// dumb logic - always use '1' for an Integer
					parameterString = Integer.toString(1) + " ";
					previousParameterStrings.add(parameterString);
				} else if (potentialParameter.getType() == Double.class) {
					// dumb logic - always use '1.0' for a Double
					parameterString = Double.toString(1.0) + " ";
					previousParameterStrings.add(parameterString);
				} else if (potentialParameter.getType() == String.class) {

					// if the parameter has internal format (eg.
					// "<number>:<number>PM EST")
					if (potentialParameter.isFormatted()) {

						// loop over the areas of the format that must be
						// replaced and choose values
						List<Object> formatVariableValues = new ArrayList<Object>();
						for (Class<?> type : potentialParameter.getFormatVariables()) {
							if (type == Integer.class) {
								// dumb logic - always use '1' for an Integer
								formatVariableValues.add(new Integer(1));
							} else if (type == String.class) {
								// dumb logic - always use 'one' for a string
								formatVariableValues.add(new String("one"));
							}
						}

						// build the formatted parameter string with the chosen
						// values (eg. 1:1PM EST)
						parameterString = potentialParameter.getFormattedParameter(formatVariableValues);
					} else {
						// dumb logic - always use 'one' for a String
						parameterString = "one ";
					}

					previousParameterStrings.add(parameterString);
				} else {
					parameterString = "unknown type";
				}
			}
			// because of the challenge associated with dependent parameters, we
			// must go one parameter at a time, building up the parameter list -
			// getNext is the method that we are using to get the next set of
			// options, given an accumulating parameter list.
			potentialParameters = this.parameterFactory.getNext(previousParameterStrings);
		}
		return previousParameterStrings.toArray();
	}
}