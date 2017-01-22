package contest.winter2017;

import java.util.List;

public class BlackBoxExplorer {
	private String jarToTestPath;
	private ParameterFactory parameterFactory;
	private boolean bbTestsPsecified;
	private boolean timeGoalSpecified;
	private int bbTests;
	private int timeGoal;

	public BlackBoxExplorer(String jarToTestPath, ParameterFactory parameterFactory,
						boolean bbTestsSpecified, boolean timeGoalSpecified, int bbTests, int timeGoal) {

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
	}

	public BlackBoxExplorer(String jarToTestPath, ParameterFactory parameterFactory) {
		this(jarToTestPath, parameterFactory, false, false, 0, 0);
	}

	public void exploreByFizzing() {
		List<ParameterList> possibleParamLists = this.parameterFactory.possibleParamLists();
		ParameterList parameterList = possibleParamLists.get(0);



		// System.out.println(possibleParamLists);
	}
}





