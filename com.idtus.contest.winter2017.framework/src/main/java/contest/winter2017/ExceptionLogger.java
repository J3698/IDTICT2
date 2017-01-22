package contest.winter2017;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

/**
 * This static class is for saving logs
 * with information pertaining to exceptions.
 * This class is static to prmote low coupling,
 * i.e. diminish interdependence with other
 * classes.
 * 
 * @author ICT-2
 */
class ExceptionLogger {
	private static CommandLine cliArgs;
	private static File jacocoOutputDir;
	private static File currentDir;

	static void init(CommandLine initCliArgs) {
		cliArgs = initCliArgs;
		jacocoOutputDir = new File(cliArgs.getOptionValue(Main.JACOCO_OUTPUT_PATH));
		currentDir = new File(".");
	}

	/**
	 * private static method to ask to save an error log
	 * <p>
	 * This method asks if the user wants to save log
	 * to the jacoco output location. If this is not
	 * possible, it asks if the user wants to save
	 * the log to the current directory.
	 *
	 * @param cliArgs the commandline arguments
	 * @param re runtime exception encountered
	 * @param now date and time of exception
	 */
	static void errorLogDialog(RuntimeException re, LocalDateTime now) {
		System.out.println("An exception has occured.");
		System.out.println("Save error log to jacoco output path? (Y/n) ");
		Scanner in = new Scanner(System.in);
		if (!in.next().equalsIgnoreCase("n")) {
			if (!saveErrorLog(jacocoOutputDir, cliArgs, re, now)) {
				System.out.println("Could not log save to jacoco output. Save to current directory? (Y/n) ");
				if (!in.next().equalsIgnoreCase("n")) {
					if (!saveErrorLog(currentDir, cliArgs, re, now)) {
						System.out.println("Could not save log to current directory.");
					}
				}
			}
		}
	}

	/**
	 * private static method to save error logs
	 * <p>
	 * This method attempts to log an error file. 
	 * A number is appended to error logs so that
	 * previous logs are not overwritten.
	 * 
	 * @param dir directory to save file into
	 * @param cliArgs the commandline arguments
	 * @param re runtime exception encountered
	 * @param now date and time of exception
	 */
	private static boolean saveErrorLog(File dir, CommandLine cliArgs, RuntimeException re, LocalDateTime now) {
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
			Iterator<Option> it = cliArgs.iterator();
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
