package contest.winter2017;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

class ParameterString implements Comparable<ParameterString> {
	private ParameterFactory parameterFactory;
	private ParameterString parent;
	private Output output;
	private List<Parameter> possibleParameters = null;
	private List<String> parameters;
	private HashSet<Parameter> usedParameters;
	private List<ParameterString> fertileChildren = null;
	private List<ParameterString> allChildren = null;
	private int uniqueLines;
	private int childTests = 0;
	private double mean;
	private boolean isExpanded;
	private Boolean isTestable = null;
	private boolean isTested;

	public ParameterString(ParameterFactory parameterFactory, HashSet<Parameter> usedParameter,
			List<String> parameters) {
		this.parameterFactory = parameterFactory;
		this.usedParameters = usedParameter;
		if (usedParameter == null) {
			this.usedParameters = new HashSet<Parameter>();
		}
		this.parameters = parameters;
		if (this.parameters == null) {
			this.parameters = new ArrayList<String>();
		}
	}

	public ParameterString(ParameterFactory parameterFactory, List<String> parameters) {
		this(parameterFactory, null, parameters);
	}

	public ParameterString(ParameterFactory parameterFactory) {
		this(parameterFactory, null, null);
	}

	public void expand() {
		if (this.fertileChildren == null) {
			this.possibleParameters = this.parameterFactory.getNext(parameters, usedParameters);
		}
		if (this.possibleParameters.size() == 0) {
			return;
		}
		Collections.sort(this.possibleParameters, new BranchFactorComparator());

		Parameter brancher = this.possibleParameters.get(0);
		@SuppressWarnings("unchecked")
		HashSet<Parameter> usedParameters = (HashSet<Parameter>) this.usedParameters.clone();
		usedParameters.add(brancher);

		if (brancher.isEnumeration()) {
			List<String> enumVals = brancher.getEnumerationValues();
			for (int i = 0; i < enumVals.size(); i++) {
				boolean numberFound = false;
				Class type = null;
				List<Object> formatVals = new LinkedList<Object>();
				String toFormat = enumVals.get(i);

				List<Class> formatTypes = Parameter.getFormatVariables(toFormat);
				for (Class<?> c : formatTypes) {
					if (Number.class.isAssignableFrom(c) && !numberFound) {
						numberFound = true;
						type = c;
						// could instead keep track of insertion point
						formatVals.add("<<LAST_NUMBER_TO_FORMAT>>");
					} else {
						if (Number.class.isAssignableFrom(c)) {
							formatVals.add(1);
						} else {
							formatVals.add("\"The lazy dog jumped over the quick brown fox.\"");
						}
					}
				}

				toFormat = Parameter.getFormattedParameter(toFormat, formatVals);

				if (toFormat.contains("<<LAST_NUMBER_TO_FORMAT>>")) {
					Number min = null;
					Number max = null;
					if (brancher.getMin() instanceof List) {
						min = ((List<Number>) brancher.getMin()).get(i);
					}
					if (brancher.getMax() instanceof List) {
						max = ((List<Number>) brancher.getMax()).get(i);
					}

					String dummyValue = "";
					if ((min == null || min.longValue() <= 1) && (max == null || max.longValue() <= 1)) {
						dummyValue = "1";
					} else if (min == null && max != null) {
						dummyValue = "" + (max.longValue() - 1);
					} else {
						dummyValue = "" + (min.longValue() + 1);
					}

					toFormat.replace("<<LAST_NUMBER_TO_FORMAT>>", "1");
				}

				List<String> copy = new ArrayList<String>();
				Collections.copy(this.parameters, copy);
				copy.add(toFormat);
				ParameterString newString = new ParameterString(this.parameterFactory, usedParameters, copy);
				this.fertileChildren.add(newString);
				this.allChildren.add(newString);
			}
		} else {
			if (brancher.isFormatted()) {
				String toFormat = brancher.getFormat();
				List<Class> formatTypes = Parameter.getFormatVariables(toFormat);
				List<Object> formatVals = new ArrayList<Object>();
				boolean numberFound = false;

				if (formatTypes.get(0) != null) {
					formatVals.add("<<LAST_THING_TO_FORMAT>>");
					if (Number.class.isAssignableFrom(formatTypes.get(0))) {
						numberFound = true;
					}
				}

				for (int i = 1; i < formatTypes.size(); i++) {
					Class<?> c = formatTypes.get(i);

					if (Number.class.isAssignableFrom(c) && !numberFound) {
						numberFound = true;
						// format
						formatVals.add("<<LAST_NUMBER_TO_FORMAT>>");
					} else {
						if (Number.class.isAssignableFrom(c)) {
							formatVals.add(1);
						} else {
							formatVals.add("\"The lazy dog jumped over the quick brown fox.\"");
						}
					}
				}

				Number min = (Number) brancher.getMin();
				Number max = (Number) brancher.getMax();

				if (toFormat.contains("<<LAST_NUMBER_TO_FORMAT>>")) {
					String dummyValue = "";
					if ((min == null || min.longValue() <= 1) && (max == null || max.longValue() <= 1)) {
						dummyValue = "1";
					} else if (min == null && max != null) {
						dummyValue = "" + (max.longValue() - 1);
					} else {
						dummyValue = "" + (min.longValue() + 1);
					}

					toFormat.replace("<<LAST_NUMBER_TO_FORMAT>>", "1");
				}

				if (!formatTypes.isEmpty()) {
					if (Number.class.isAssignableFrom(formatTypes.get(0))) {
						Set<Number> branches = new HashSet<Number>();

					} else {

					}
				}
			} else {
				// not very hard is it
			}
			/*
			 * replace_all_but_first_replaceme_with_dummy_values();
			 * 
			 * if (first_replaceme_a_number()) { for option in
			 * getNumOptions(getMin, getMax); formatted =
			 * getformatted(dummyValedThing, List(option)); children.add(new
			 * ParameterString(formatted)); } else { for (option in
			 * dummyStringVals()) { formated = getFormated(dummyValedThing,
			 * List(option)); children.add(new ParamterString(formatted)); } }
			 */
		}
	}

	public void getBranches() {

	}

	public void updateMean() {
		double total = this.uniqueLines;
		for (ParameterString child : this.allChildren) {
			total += child.childTests * child.mean;
		}
		this.mean = total / this.childTests;

		if (this.parent != null) {
			this.parent.updateMean();
		}
	}

	/**
	 * @return the usedParameters
	 */
	public Set<Parameter> getUsedParameters() {
		return this.usedParameters;
	}

	/**
	 * @return the string
	 */
	public String toString() {
		String out = "";
		for (String param : this.parameters) {
			out += param + " ";
		}
		return out;
	}

	public void addUniqueness(int uniqueLines) {
		this.uniqueLines += uniqueLines;
	}

	public void subUniqueness(int uniqueLines) {
		this.uniqueLines -= uniqueLines;
	}

	/**
	 * @return the children
	 */
	public List<ParameterString> getChildren() {
		if (this.fertileChildren == null) {
			expand();
		}
		return this.fertileChildren;
	}

	/**
	 * @return the parent
	 */
	public ParameterString getParent() {
		return this.parent;
	}

	/**
	 * @return the output
	 */
	public Output getOutput() {
		return this.output;
	}

	/**
	 * @param output
	 *            - the output to set
	 */
	public void setOutput(Output output) {
		this.output = output;
	}

	public boolean isTested() {
		return this.isTested;
	}

	public boolean isTestable() {
		if (this.isTestable != null) {
			return this.isTestable;
		}

		if (this.possibleParameters == null) {
			this.possibleParameters = this.parameterFactory.getNext(parameters, usedParameters);
		}

		for (Parameter param : this.possibleParameters) {
			if (!param.isOptional()) {
				this.isTestable = false;
				return false;
			}
		}

		this.isTestable = true;
		return true;
	}

	public void setTested() {
		if (!this.isTested) {
			this.isTested = true;
			setChildTested();
		}
	}

	public void setChildTested() {
		this.childTests++;
		if (this.parent != null) {
			this.parent.setChildTested();
		}
	}

	public boolean isExpandable() {
		if (this.fertileChildren == null) {
			this.possibleParameters = this.parameterFactory.getNext(parameters, usedParameters);
		}
		return !possibleParameters.isEmpty();
	}

	public boolean isExpanded() {
		return this.isExpanded;
	}

	public ParameterString bestChild() {
		Collections.sort(this.fertileChildren);
		return this.fertileChildren.get(0);
	}

	public void removeFromParent() {
		if (this.parent != null) {
			this.parent.removeChild(this);
		}
	}

	private void removeChild(ParameterString parameterString) {
		this.fertileChildren.remove(parameterString);
	}

	public double upperBound() {
		double frac = 2 * Math.log(parent.childTests) / this.childTests;
		return this.mean + Math.sqrt(frac);
	}

	public int compareTo(ParameterString other) {
		double num = other.upperBound() - this.upperBound();
		return num == 0 ? 0 : num > 0 ? 1 : -1;
	}
}

class BranchFactorComparator implements Comparator<Parameter> {
	private static final int stringBranchFactor = 4;
	private static final int numBranchFactor = 5;

	@Override
	public int compare(Parameter first, Parameter second) {
		// TODO Auto-generated method stub
		return branchFactor(first) - branchFactor(second);
	}

	public int branchFactor(Parameter parameter) {
		if (parameter.isEnumeration()) {
			return parameter.getEnumerationValues().size();
		} else if (parameter.getType() == String.class) {
			return stringBranchFactor;
		} else {
			Number min = (Number) parameter.getMin();
			Number max = (Number) parameter.getMax();
			if (max != null && min != null && min.equals(max)) {
				return 3;
			}
			return 5;
		}
	}
}