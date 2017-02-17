package contest.winter2017;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Set;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Class to represent permission info. This class maps a permission to the
 * allowances and risks associated with programs which use that permission.
 * 
 * @author ICT-2
 */
public class PermissionInfo {
	// constants
	public static final String LOAD_ERROR = "Could not load any permission information.";
	public static final String NO_DOCUMENTATION = "No documentation for that permission.";
	public static final String COPYRIGHT_NOTICE = "Copyright © 1993, 2017, Oracle and/or its affiliates.  All rights reserved.  Used by permission.";

	// information on initialization status
	private static boolean isInitialized = false;
	private static boolean initFailed = false;

	/**
	 * Map of permission allowances.
	 */
	private static HashMap<String, String> permissionAllowances = new HashMap<String, String>();

	/**
	 * Map of permission risks.
	 */
	private static HashMap<String, String> permissionRisks = new HashMap<String, String>();

	/**
	 * Returns the allowance associated with a given permission.
	 * 
	 * @param permission
	 *            - permission to get allowances for
	 * @return the allowance associated with the permission, contents of
	 *         LOAD_ERROR if info could not load, or contents of
	 *         NO_DOCUMENTATION if info for the permission does not exist
	 */
	public static String getAllowance(String permission) {
		if (!isInitialized) {
			init();
		}
		if (PermissionInfo.initFailed) {
			return PermissionInfo.LOAD_ERROR;
		} else if (PermissionInfo.permissionAllowances.get(permission) == null) {
			return PermissionInfo.NO_DOCUMENTATION;
		} else {
			return PermissionInfo.permissionAllowances.get(permission);
		}
	}

	/**
	 * Returns the risk associated with a given permission.
	 * 
	 * @param permission
	 *            - permission to get risks for
	 * @return the risk associated with the permission, contents of LOAD_ERROR
	 *         if info could not load, or contents of NO_DOCUMENTATION if info
	 *         for the permission does not exist
	 */
	public static String getRisk(String permission) {
		if (!isInitialized) {
			init();
		}
		if (PermissionInfo.initFailed) {
			return PermissionInfo.LOAD_ERROR;
		} else if (PermissionInfo.permissionAllowances.get(permission) == null) {
			return PermissionInfo.NO_DOCUMENTATION;
		} else {
			return PermissionInfo.permissionRisks.get(permission);
		}
	}

	/**
	 * Returns the set of permission names.
	 * 
	 * @return the set of permission names
	 */
	@SuppressWarnings("unchecked")
	public static Set<String> getPermissionNames() {
		if (!isInitialized) {
			init();
		}
		return Collections.unmodifiableSet(permissionAllowances.keySet());
	}

	/**
	 * Initializes the permission information from a text file.
	 */
	private static void init() {
		PermissionInfo.isInitialized = true;
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(PermissionInfo.class.getResourceAsStream("PermissionInfo.txt")))) {
			String next;
			String permission;
			StringBuffer info;
			br.readLine();

			while ((next = br.readLine()) != null) {
				permission = next.split("\\Q.\\E")[0];
				br.readLine();

				info = new StringBuffer();
				while (!(next = br.readLine()).equals("Potential Risks:")) {
					info.append(next);
					info.append('\n');
				}
				PermissionInfo.permissionAllowances.put(permission, info.toString());

				info = new StringBuffer();
				while (!(next = br.readLine()).equals("--NEXT--")) {
					info.append(next);
					info.append('\n');
				}
				PermissionInfo.permissionRisks.put(permission, info.toString());
			}
		} catch (IOException e) {
			PermissionInfo.initFailed = true;
		}
	}

	/**
	 * Private constructor for PermissionInfo. This ensures this class can only
	 * be used in a static context.
	 */
	private PermissionInfo() {
	}
}
