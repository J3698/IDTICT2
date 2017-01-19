package contest.winter2017;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Class to represent a list of parameters.
 * 
 * This representation of a list of parameters
 * makes creating all possible parameter lists
 * easer, with the help of excludes. Excludes are
 * parameters which are part of the exclusion set,
 * which we specifically do not want to use in the
 * list of parameters.
 * 
 * @author IDTICT2
 */
class ParameterList implements Iterable<Parameter> {
	/**
	 * List of parameters
	 */
	private ArrayList<Parameter> paramList;

	/**
	 * Set of parameters to exclude
	 */
	private HashSet<Parameter> toExclude;

	public ParameterList() {
		this.paramList = new ArrayList<Parameter>();
		this.toExclude = new HashSet<Parameter>();
	}

	/**
	 * Method to add a parameter to the
	 * list of parameters
	 * 
	 * @param parameter to add
	 */
	public void addParameter(Parameter toAdd) {
		this.paramList.add(toAdd);
	}

	/**
	 * Method to add a parameter to the
	 * exclusion set.
	 * 
	 * @param param parameter to exclude
	 */
	public void addExclude(Parameter param) {
		this.toExclude.add(param);
	}

	/**
	 * Method to return a list representation
	 * of the parameters.
	 * 
	 * @return the list of parameters
	 */
	public List<Parameter> getParameters() {
		return this.paramList;
	}

	/**
	 * Method to copy this ParameterList.
	 * Parameters and 
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object clone() {
		ParameterList clone = new ParameterList();
		clone.paramList = (ArrayList<Parameter>) this.paramList.clone();
		clone.toExclude = (HashSet<Parameter>) this.toExclude.clone();
		return null;
	}

	/**
	 * Method to get an iterator over this parameter list
	 * 
	 * @return iterator over the parameters
	 */
	public Iterator<Parameter> iterator() {
		return this.paramList.iterator();
	}

	/**
	 * 
	 */
	public String toString() {
		return this.paramList.toString();
	}

	/**
	 * Method to get this parameter list's size
	 * 
	 * @return the size of this parameter list
	 */
	public int size() {
		return this.paramList.size();
	}
}