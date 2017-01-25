package contest.winter2017;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Class to represent permission info. This class maps
 * a permission to the risks associated with programs
 * which use that permssion.
 * 
 * @author ICT-2
 */
public class PermissionInfo {
	private static boolean isInitialized = false;
	private static boolean initFailed = false;
	private static HashMap<String, String> permissionInfo = null;

	/**
	 * Method to get the information assoiated with a given
	 * permission.
	 * <p>
	 * Returns "Could not load permission info" if
	 * 
	 * @param permission - permission to get info for
	 * @return information associated with the permission,
	 *  	"Could not load permission info" if info could not load,
	 *  	or "No documented info for that permission" if info for the permission does not exist
	 */
	public static String getInfo(String permission) {
		if (!isInitialized) {
			init();
		}
		if (PermissionInfo.initFailed) {
			return "Could not load permission info";
		} else if (PermissionInfo.permissionInfo.get(permission) == null) {
			return "No documented info for that permission";
		} else {
			return PermissionInfo.permissionInfo.get(permission);
		}
	}

	/**
	 * Method to initialize the PermissionInfo. Initialization
	 * includes loading permission info from a text file.
	 */
	private static void init() {
		PermissionInfo.isInitialized = true;
		try {
			BufferedReader br = new BufferedReader(new FileReader("PermissionInfo.txt"));
			String next;
			String permission;
			StringBuffer info;
			br.readLine();

			while ((next = br.readLine()) != null) {
				permission = br.readLine();
				info = new StringBuffer();
				while (!(next = br.readLine()).equals("--NEXT--")) {
					info.append(next);
					info.append('\n');
				}
				PermissionInfo.permissionInfo.put(permission, info.toString());
			}
		} catch (IOException e) {
			PermissionInfo.initFailed = true;
		}
	}

	/**
	 * Private Ctr. for PermissionInfo. This ensures
	 * this class can only be used as a static object.
	 */
	private PermissionInfo() {}
}
