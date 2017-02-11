package contest.winter2017;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;

public class MonteCarloTestGenerator extends TestGenerator {
	private HashMap<String, ClassCounter> classes = new HashMap<String, ClassCounter>();
	private ParameterString lastParameterString = null;
	private int outputSize;

	/**
	 * Constructs a Monte-Carlo test generator with the given parameter factory
	 * and list of outputs.
	 * 
	 * @param parameterFactory
	 *            - parameter factory for this test generator.
	 * 
	 * @param outputs
	 *            - list of outputs encountered by any tests run outputs is
	 *            updated automatically by the tester
	 */
	public MonteCarloTestGenerator(ParameterFactory parameterFactory, List<Output> outputs) {
		super(parameterFactory, outputs);
		outputSize = getOutputs().size();
	}

	/**
	 * Gets the next test to be run.
	 * <p>
	 * Uses a modified Monte-Carlo tree search to generate promising tests.
	 * 
	 * When a test is run, the associated Output object is added to the end of
	 * this class's outputs list.
	 * 
	 * @return an array of objects which represent parameters to be tested.
	 */
	@Override
	public Object[] nextTest() {
		// next: if monte-carlo fails for some reason, switch to random fizzing.

		if (getOutputs().size() != this.outputSize) {
			Output lastOutput = getOutputs().get(getOutputs().size() - 1);
			lastParameterString.setOutput(lastOutput);
			updateAllUniquenesses(lastParameterString, lastOutput);
		}

		// TODO Auto-generated method stub
		return null;
	}

	public void updateAllUniquenesses(ParameterString parameter, Output output) {
		Collection<IClassCoverage> toUpdate = output.getCoverageBuilder().getClasses();
		for (IClassCoverage cc : toUpdate) {
			updateClassUniquenesses(parameter, cc);
		}
	}

	public void updateClassUniquenesses(ParameterString parameter, IClassCoverage icc) {
		if (this.classes.get(icc.getSignature()) == null) {
			int first = icc.getFirstLine();
			int last = icc.getLastLine();
			this.classes.put(icc.getSignature(), new ClassCounter(first, last));
		}

		ClassCounter counter = classes.get(icc.getSignature());

		for (Range r : getRanges(parameter, counter, icc)) {
			List<Range> subRanges = getSubRanges(r);
			updateStartRange(subRanges.get(0));
			updateEndRange(subRanges.get(subRanges.size() - 1));
			for (int i = 1; i < subRanges.size() - 2; i++) {
				updateMiddleRange(subRanges.get(i));
			}
		}
	}

	private List<Range> getRanges(ParameterString parameter, ClassCounter cc, IClassCoverage icc) {
		List<Range> ranges = new ArrayList<Range>();
		int lastLine = cc.getLastLine();
		int start = cc.getFirstLine();
		while (start <= lastLine) {
			if (icc.getLine(start).getStatus() == ICounter.PARTLY_COVERED) {
				int end = start;
				// while line covered and not at the end of the class
				while (end != lastLine && icc.getLine(end + 1).getStatus() == ICounter.PARTLY_COVERED) {
					end++;
				}
				ranges.add(new Range(parameter, start, end));
				start = end;
			}
			start++;
		}
		return ranges;
	}

	private List<Range> getSubRanges(ClassCounter cc, Range r) {
		List<Integer> changes = new LinkedList<Integer>();
		int curr = r.getStart();
		while (getEnd(cc, curr) < r.getEnd()) {
			curr = getEnd(cc, curr);
			changes.add(curr);
			curr++;
			if (curr < r.getEnd()) {
				changes.add(curr);
			}
		}
		return null;
	}

	private int nextChange(ClassCounter cc, int line) {
		return -1;
	}

	private int getEnd(ClassCounter cc, int line) {
		return -1;
	}

	private void updateMiddleRange() {
	}

	private void updateStartRange() {
	}

	private void updateEndRange() {
	}
}

class ClassCounter {
	List<Range> ranges = new ArrayList<Range>();

	public ClassCounter(int first, int last) {
		this.ranges.add(new Range(null, first, last));
	}

	public List<Range> getRanges() {
		return this.ranges;
	}

	public int getFirstLine() {
		return this.ranges.get(0).getStart();
	}

	public int getLastLine() {
		return this.ranges.get(this.ranges.size() - 1).getEnd();
	}
}

class Range {
	private ParameterString visitor = null;
	private int start;
	private int end;

	public Range(ParameterString visitor, int start, int end) {
		this.visitor = visitor;
		this.start = start;
		this.end = end;
	}

	public List<Range> split(int line) {
		return null;
	}

	public int getStart() {
		return this.start;
	}

	public int getEnd() {
		return this.end;
	}
}
