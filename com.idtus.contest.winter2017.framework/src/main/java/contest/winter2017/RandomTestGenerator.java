package contest.winter2017;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomTestGenerator extends TestGenerator {

	/**
	 * Constructs a random test generator with the given parameter factory and
	 * list of outputs.
	 * 
	 * @param parameterFactory
	 *            - parameter factory for this test generator.
	 * 
	 * @param outputs
	 *            - list of outputs encountered by any tests run outputs is
	 *            updated automatically by the tester
	 */
	public RandomTestGenerator(ParameterFactory parameterFactory, List<Output> outputs) {
		super(parameterFactory, outputs);
		// TODO Auto-generated constructor stub
	}

	@Override
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
		List<Parameter> potentialParameters = getParameterFactory().getNext(previousParameterStrings);
		Parameter potentialParameter;
		Random rng = new Random();
		while (!potentialParameters.isEmpty()) {
			String parameterString = "";
			potentialParameter = potentialParameters.get(rng.nextInt(potentialParameters.size()));

			// if(potentialParameter.isOptional()) //TODO? - your team might
			// want to look at this flag and handle it as well!

			// an enumeration parameter is one that has multiple options
			if (potentialParameter.isEnumeration()) {
				// dumb logic - given a list of options, always use the first
				// one
				List<String> enumerationValues = potentialParameter.getEnumerationValues();
				parameterString = enumerationValues.get(rng.nextInt(enumerationValues.size())) + " ";

				// if the parameter has internal format (eg.
				// "<number>:<number>PM EST")
				if (potentialParameter.isFormatted()) {

					// loop over the areas of the format that must be replaced
					// and choose values
					List<Object> formatVariableValues = new ArrayList<Object>();
					for (Class<?> type : Parameter.getFormatVariables(parameterString)) {
						if (type == Integer.class) {
							// dumb logic - always use 1 for an integer
							formatVariableValues.add(new Integer(randomInt()));
						} else if (type == String.class) {
							// dumb logic - always use 'one' for an integer
							formatVariableValues.add(new String(randomString()));
						}
					}

					// build the formatted parameter string with the chosen
					// values (eg. 1:1PM EST)
					parameterString = Parameter.getFormattedParameter(parameterString, formatVariableValues);
				}
				previousParameterStrings.add(parameterString);
				// if it is not an enumeration parameter, it is either an
				// Integer, Double, or String
			} else {
				if (potentialParameter.getType() == Integer.class) {
					// dumb logic - always use '1' for an Integer
					parameterString = Integer.toString(randomInt()) + " ";
					previousParameterStrings.add(parameterString);
				} else if (potentialParameter.getType() == Double.class) {
					// dumb logic - always use '1.0' for a Double
					parameterString = Double.toString(randomDouble()) + " ";
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
								formatVariableValues.add(randomInt());
							} else if (type == String.class) {
								// dumb logic - always use 'one' for a string
								formatVariableValues.add(randomString());
							}
						}

						// build the formatted parameter string with the chosen
						// values (e.g. 1:1PM EST)
						parameterString = potentialParameter.getFormattedParameter(formatVariableValues);
					} else {
						// dumb logic - always use 'one' for a String
						parameterString = randomString();
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
			potentialParameters = getParameterFactory().getNext(previousParameterStrings);
		}
		return previousParameterStrings.toArray();
	}

	/**
	 * Returns a random int.
	 * 
	 * @return a random int
	 */
	public int randomInt() {
		return new Random().nextInt();
	}

	/**
	 * Returns a random double.
	 * 
	 * @return a random double
	 */
	public double randomDouble() {
		Random rng = new Random();
		return rng.nextInt() * rng.nextDouble();
	}

	/**
	 * Returns a random string.
	 * 
	 * @return a random string
	 */
	public String randomString() {
		Random r = new Random();
		int choice = r.nextInt(5);
		String str = "";
		if (choice == 0) {
			str = "\"}{\\\\s({\t0\"";
		} else if (choice == 1) {
			str = "\"~The d0g jump3d ov3r the cow~\"";
		} else if (choice == 2) {
			str = "\"\"";
		} else {
			for (int i = 0; i < 5; i++) {
				str += "" + (char) (32 + r.nextInt(127 - 32));
			}
		}
		return str;
	}
}