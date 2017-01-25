package contest.winter2017;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Class to generate tests to be run from the Tester.
 * 
 * @author ICT-2
 */
public class TestGenerator {
	/**
	 * parameter factory to get info about
	 * inputs to jar to test
	 */
	private ParameterFactory paramFactory;

	/**
	 * list of automatically updated outputs from tests run
	 */
	private ArrayList<Output> outputs;

	/**
	 * 
	 * @param outputs - list of outputs encountered by any tests run
	 *                  outputs is updated automatically by the tester
	 */
	public TestGenerator(ParameterFactory paramFactory, ArrayList<Output> outputs) {
		this.paramFactory = paramFactory;
		this.outputs = outputs;
	}

	/**
	 * Method to get the next test to be run. Currently returns
	 * the same test every time for a given jar. 
	 * 
	 * Some algo ideas:
	 * https://www.tutorialspoint.com/software_testing_dictionary/black_box_testing.htm
	 * 
	 * @return an array of type String/StringBuffer which represents parameters to be tested.
	 */
	public CharSequence[] nextTest() {
		return null;
	}
}