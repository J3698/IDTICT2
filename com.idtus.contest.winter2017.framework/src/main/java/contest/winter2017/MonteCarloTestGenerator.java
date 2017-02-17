package contest.winter2017;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;

/**
 * Test generator which used the UCB1 Monte-Carlo tree-search algorithm to find
 * promising tests. This algorithm iteratively builds a tree of possible
 * parameter lists. When faced with a branch, UCB1 weighs the score of a child
 * node with how many times it or its children have been tested. Nodes which
 * introduce unique lines of code are valued, but so are neglected branches.
 * <p>
 * Improvements to this algorithm include anything from changing how nodes are
 * scored, to using a different technique such as Thompson sampling.
 * 
 * @author ICT-2
 */
public class MonteCarloTestGenerator extends TestGenerator {
	/**
	 * List of classes encountered by tests run from this test generator.
	 */
	private HashMap<String, ClassCounter> classes = new HashMap<String, ClassCounter>();

	/**
	 * The root parameter string of the search tree.
	 */
	private ParameterString root;

	/**
	 * The test generator to fall back on if this generator fails.
	 */
	private RandomTestGenerator fallBack;

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
		this.fallBack = new RandomTestGenerator(parameterFactory, outputs);
		this.root = new ParameterString(parameterFactory);
	}

	/**
	 * Gets the next test to be run.
	 * <p>
	 * Uses a modified Monte-Carlo tree-search algorithm to generate promising
	 * tests.
	 * 
	 * When a test is run, the associated output object is added to the end of
	 * this class's outputs list.
	 * 
	 * @return an array of objects which represent parameters to be tested.
	 */
	@Override
	public Object[] nextTest() {
		try {
			// update tree with last test
			updateOutputs();

			// get next test from the tree if there is one
			ParameterString curr = this.root;
			boolean hasTest = true;
			while (hasTest) {
				if (curr.isTestable() && !curr.isTested()) {
					curr.setTested();
					return curr.toString().split(" ");
				} else if (curr.isExpandable()) {
					if (!curr.isExpanded()) {
						curr.expand();
					}
					curr = curr.bestChild();
				} else {
					curr.removeFromParent();
					curr = curr.getParent();
				}
				if (curr == null) {
					hasTest = false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// fall-back test if monte carlo generation fails
		System.out.println("ERROR: Monte-Carlo test generation failed, generating random test.");
		return this.fallBack.nextTest();
	}

	/**
	 * Updates how many unique lines of code nodes in the tree encounter.
	 * 
	 * @param parameter
	 * @param icc
	 */
	public void updateClassUniquenesses(ParameterString parameter, IClassCoverage icc) {
		// add the class to our map if it hasn't been visited
		if (this.classes.get(icc.getSignature()) == null) {
			int first = icc.getFirstLine();
			int last = icc.getLastLine();
			this.classes.put(icc.getSignature(), new ClassCounter(first, last));
		}

		// delegate updating the uniquenesses to the class counter
		ClassCounter cc = classes.get(icc.getSignature());
		for (Range r : getRanges(parameter, cc, icc)) {
			List<Range> subRanges = getSubRanges(parameter, cc, r);
			cc.updateStartRange(subRanges.get(0));
			cc.updateEndRange(subRanges.get(subRanges.size() - 1));
			for (int i = 1; i < subRanges.size() - 1; i++) {
				cc.updateMiddleRange(subRanges.get(i));
			}
		}
	}

	/**
	 * Returns the ranges of code for a class covered by the given parameter
	 * string's test.
	 * 
	 * @param parameter
	 *            - parameter to track coverage for
	 * @param cc
	 *            - class counter to track lines for
	 * @param icc
	 *            - i class coverage containing coverage information
	 * @return list of ranges covered by the given parameter string's test
	 */
	private List<Range> getRanges(ParameterString parameter, ClassCounter cc, IClassCoverage icc) {
		List<Range> ranges = new ArrayList<Range>();
		int lastLine = cc.getLastLine();
		int start = cc.getFirstLine();
		while (start <= lastLine) {
			if (icc.getLine(start).getStatus() == ICounter.PARTLY_COVERED) {
				int end = start;
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

	/**
	 * Returns sub ranges for the given range for the given class.
	 * <p>
	 * A test may run a span of code ranging from 0-50 lines. However of those
	 * 50 lines, the first 25 could be unique to another test, the next ten
	 * could be completely new, and the lest 15 could be non unique. This method
	 * would split those 50 lines into their constituent groups, or sub-ranges.
	 * 
	 * @param parameter
	 *            - parameter string whose output is being analyzed
	 * @param cc
	 *            - class counter with ranges
	 * @param r
	 *            - range to get sub ranges for
	 * @return sub ranges of the given range
	 */
	private List<Range> getSubRanges(ParameterString parameter, ClassCounter cc, Range r) {
		// get points of change in the given range
		LinkedList<Integer> changes = new LinkedList<Integer>();
		int curr = r.getStart();
		changes.addLast(curr);
		while (cc.rangeOfNextEnd(curr).getEnd() < r.getEnd()) {
			curr = cc.rangeOfNextEnd(curr).getEnd();
			changes.add(curr);
			curr++;
			if (curr < r.getEnd()) {
				changes.add(curr);
			}
		}
		changes.add(curr);

		// turn change points into sub ranges
		List<Range> subRanges = new ArrayList<Range>(changes.size() / 2);
		while (!changes.isEmpty()) {
			int first = changes.removeFirst();
			int second = changes.removeFirst();
			subRanges.add(new Range(parameter, first, second));
		}
		return subRanges;
	}
}

/**
 * Class to keep track of what tests have touched which lines of code in a given
 * class.
 * 
 * @author ICT-2
 */
class ClassCounter {
	/**
	 * Ranges of code touched by different tests
	 */
	TreeSet<Range> ranges = new TreeSet<Range>();

	/**
	 * Constructs a class counter with the given initial range of lines.
	 * 
	 * @param first
	 *            - first valid line of this class
	 * @param last
	 *            - last valid line of this class
	 */
	public ClassCounter(int first, int last) {
		this.ranges.add(new Range(null, first, last));
	}

	/**
	 * UPdates a range given a new visitor.
	 * 
	 * @param r
	 *            - range to update the visitor of
	 */
	public void updateMiddleRange(Range r) {
		if (!rangeOfNextEnd(r.getEnd()).equals(r)) {
			// dead spot, do nothing
		} else {
			// make dead spot, or make unique
			Range toPlace = rangeOfNextEnd(r.getEnd());
			if (toPlace.getVisitor() == null) {
				toPlace.setVisitor(r.getVisitor());
				r.getVisitor().addUniqueness(r.size());
			} else {
				toPlace.getVisitor().subUniqueness(r.size());
				ranges.remove(toPlace);
			}
		}
	}

	/**
	 * Updates a range given a new visitor.
	 * <p>
	 * A start range may not fully overlap a range which is already contained in
	 * the class counter. Thus updating a start range may require splitting an
	 * existing range in the class counter in two.
	 * 
	 * @param r
	 *            - range to update the visitor of
	 */
	public void updateStartRange(Range r) {
		if (rangeOfNextEnd(r.getStart()).getEnd() > r.getEnd()) {
			return;
		}

		if (r.getStart() != rangeOfNextEnd(r.getStart()).getStart()) {
			Range toSplit = rangeOfNextEnd(r.getStart());
			List<Range> split = toSplit.split(r.getStart());
			ranges.remove(toSplit);
			addRange(split.get(0));
			if (split.get(1).getVisitor() == null) {
				split.get(1).setVisitor(r.getVisitor());
				r.getVisitor().addUniqueness(r.size());
				addRange(split.get(1));
			} else {
				split.get(1).getVisitor().subUniqueness(r.size());
			}
		} else {
			Range toChange = rangeOfNextEnd(r.getStart());
			if (toChange.getVisitor() == null) {
				toChange.setVisitor(r.getVisitor());
				r.getVisitor().addUniqueness(r.size());
			} else {
				toChange.getVisitor().subUniqueness(r.size());
				ranges.remove(toChange);
			}
		}
	}

	/**
	 * Updates a range given a new visitor.
	 * <p>
	 * An end range may not fully overlap a range which is already contained in
	 * the class counter. Thus updating an end range may require splitting an
	 * existing range in the class counter in two.
	 * 
	 * @param r
	 *            - range to update the visitor of
	 */
	public void updateEndRange(Range r) {
		if (rangeOfNextEnd(r.getStart()).getEnd() != r.getEnd()) {
			return;
		}

		if (r.getEnd() != rangeOfNextEnd(r.getStart()).getEnd()) {
			Range toSplit = rangeOfNextEnd(r.getStart());
			List<Range> split = toSplit.split(r.getEnd() + 1);
			ranges.remove(toSplit);
			addRange(split.get(1));
			if (split.get(0).getVisitor() == null) {
				split.get(0).setVisitor(r.getVisitor());
				r.getVisitor().addUniqueness(r.size());
				addRange(split.get(0));
			} else {
				split.get(0).getVisitor().subUniqueness(r.size());
			}
		} else {
			Range toChange = rangeOfNextEnd(r.getStart());
			if (toChange.getVisitor() == null) {
				toChange.setVisitor(r.getVisitor());
				r.getVisitor().addUniqueness(r.size());
			} else {
				toChange.getVisitor().subUniqueness(r.size());
				ranges.remove(toChange);
			}
		}
	}

	/**
	 * Adds a range to this class counter.
	 * 
	 * @param range
	 *            - range to add to this class counter.
	 */
	public void addRange(Range range) {
		this.ranges.add(range);
	}

	public Range rangeOfNextEnd(int line) {
		// no ends further down
		if (this.ranges.last().getEnd() < line) {
			return null;
		}

		Range lineRange = new Range(null, line, line);

		// get next end from higher ranges
		Range lower = this.ranges.lower(lineRange);
		if (lower != null) {
			if (lower.getEnd() >= line) {
				return lower;
			} else {
				return this.ranges.higher(lower);
			}
		}

		// get next end from lower ranges
		Range higher = this.ranges.higher(lineRange);
		if (higher != null) {
			Range equal = this.ranges.higher(higher);
			if (equal != null && equal.getStart() == line) {
				return equal;
			}
			return higher;
		}

		// return the only valid range
		return this.ranges.first();
	}

	/**
	 * Returns the ranges of this class counter.
	 * 
	 * @return the ranges of this class counter
	 */
	public TreeSet<Range> getRanges() {
		return this.ranges;
	}

	/**
	 * Returns the first line of this class counter.
	 * 
	 * @return the first line of this class counter
	 */
	public int getFirstLine() {
		return this.ranges.first().getStart();
	}

	/**
	 * Returns the last line of this class counter.
	 * 
	 * @return the last line of this class counter
	 */
	public int getLastLine() {
		return this.ranges.last().getEnd();
	}
}

/**
 * This class represents a range of code in a class.
 * <p>
 * Ranges are sorted by their start points.
 * 
 * @author ICT-2
 */
class Range implements Comparable<Range> {
	/**
	 * The parameter string which covered this range of code.
	 */
	private ParameterString visitor;

	/**
	 * The first line of code covered by this range of code.
	 */
	private int start;

	/**
	 * The last line of code covered by this range of code.
	 */
	private int end;

	/**
	 * Constructs a range with the given visitor parameter string, start line,
	 * and end line.
	 * 
	 * @param visitor
	 *            - parameter string which covered this range of code.
	 * @param start
	 *            - the first line of code covered by this range of code.
	 * @param end
	 *            - the last line of code covered by this range of code
	 */
	public Range(ParameterString visitor, int start, int end) {
		this.visitor = visitor;
		this.start = start;
		this.end = end;
	}

	/**
	 * Returns this range split at a given line in list form.
	 * <p>
	 * The split line is included in the second range returned.
	 * 
	 * @param line
	 *            - line to split this range at
	 * @return a list with the resulting ranges
	 */
	public List<Range> split(int line) {
		Range first = new Range(this.visitor, start, line - 1);
		Range second = new Range(this.visitor, line, end);
		ArrayList<Range> split = new ArrayList<Range>(2);
		split.add(first);
		split.add(second);
		return split;
	}

	/**
	 * Compares this range to another range.
	 * <p>
	 * Equivalent to comparing the starts of the two ranges.
	 * 
	 * @return an int whose sign represents the ordering of the two ranges
	 */
	@Override
	public int compareTo(Range other) {
		return this.start - other.start;
	}

	/**
	 * Returns whether this range is equal to another object.
	 * 
	 * @param other
	 *            - object to compare
	 * @return true if the object is a range with the same start, otherwise
	 *         false
	 */
	@Override
	public boolean equals(Object other) {
		if (other instanceof Range) {
			return this.start == ((Range) other).start;
		}
		return false;
	}

	/**
	 * Returns the visitor of this range.
	 * 
	 * @return the visitor of this range
	 */
	public ParameterString getVisitor() {
		return this.visitor;
	}

	/**
	 * Sets the visitor of this range.
	 * 
	 * @param visitor
	 *            - the visitor of this range
	 */
	public void setVisitor(ParameterString visitor) {
		this.visitor = visitor;
	}

	/**
	 * Returns the start line of this range.
	 * 
	 * @return the start line of this range
	 */
	public int getStart() {
		return this.start;
	}

	/**
	 * Returns the end line of this range.
	 * 
	 * @return the end line of this range
	 */
	public int getEnd() {
		return this.end;
	}

	/**
	 * Returns the size of this range.
	 * 
	 * @return the size of this range
	 */
	public int size() {
		return this.end - this.start + 1;
	}
}