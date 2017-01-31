package contest.winter2017;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

/**
 * This static class is for saving logs with information pertaining to
 * exceptions. This class is static to prmote low coupling, i.e. diminish
 * interdependence with other classes.
 * 
 * @author ICT-2
 */
class ExceptionLogger {
	/**
	 * CommandLine to get arguments to log.
	 */
	private static CommandLine cliArgs;

	/**
	 * File directory to output error logs into.
	 */
	private static File jacocoOutputDir;

	/**
	 * File directory to output error logs into.
	 */
	private static File currentDir;

	/**
	 * Initializes the ExceptionLog.
	 * 
	 * @param initCliArgs
	 *            - CommandLine to include in reports
	 */
	static void init(CommandLine initCliArgs) {
		cliArgs = initCliArgs;
		jacocoOutputDir = new File(cliArgs.getOptionValue(Main.JACOCO_OUTPUT_PATH));
		currentDir = new File(".");
	}

	/**
	 * Asks to save an error log.
	 * <p>
	 * This method asks if the user wants to save log to the Jacoco output
	 * location. Then, it asks if the user wants to save the log to the current
	 * directory.
	 *
	 * @param re
	 *            - RuntimeException encountered
	 * @param now
	 *            - date and time of exception
	 */
	static void errorLogDialog(RuntimeException re, LocalDateTime now) {
		System.out.println("The tester has encountered an exception.");
		System.out.println("Save error log to jacoco output directory? (Y/n) ");
		Scanner in = new Scanner(System.in);
		if (!in.nextLine().equalsIgnoreCase("n")) {
			if (!saveErrorLog(jacocoOutputDir, re, now)) {
				System.out.println("Could not log save to jacoco output directory.");
			}
		}

		System.out.println("Save error log to current directory? (Y/n) ");
		if (!in.nextLine().equalsIgnoreCase("n")) {
			if (!saveErrorLog(currentDir, re, now)) {
				System.out.println("Could not save log to current directory.");
			}
		}
	}

	/**
	 * Saves the specefied exception to the specified error log file.
	 * <p>
	 * This method attempts to log an error file. A number is appended to error
	 * logs so that previous logs are not overwritten.
	 * 
	 * @param dir
	 *            - directory to save file into
	 * @param re
	 *            - RuntimeEception encountered
	 * @param now
	 *            - date and time of exception
	 * 
	 * @return boolean whether the log was succesfully saved
	 */
	private static boolean saveErrorLog(File dir, RuntimeException re, LocalDateTime now) {
		if (!dir.exists()) {
			return false;
		}

		PrintWriter pw = null;
		File toWrite;
		try {
			// get an available log number
			int num = 0;
			String name = dir.getPath() + "\\error_log_";
			while ((toWrite = new File(name + num + ".txt")).exists()) {
				num++;
			}

			// print date
			pw = new PrintWriter(toWrite);
			pw.println("Recorded at: " + now.toLocalDate() + " " + now.toLocalTime());
			// print arguments
			pw.println("");
			pw.println("\nArguments:");
			Iterator<Option> it = ExceptionLogger.cliArgs.iterator();
			while (it.hasNext()) {
				Option next = it.next();
				pw.println("  " + next.getOpt() + ":");
				for (String value : next.getValues()) {
					pw.println("  - " + value);
				}
			}
			// print stacktrace
			pw.println("");
			pw.println("Trace:");
			re.printStackTrace(pw);
		} catch (Exception e) {
			return false;
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
		System.out.println("File saved succesfully as " + toWrite.getPath() + ".");
		return true;
	}
}