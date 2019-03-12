package Uiautomation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class GetDevices {

	public static Object[] getUdid() {
		List<String> deviceslist = new ArrayList<String>();
		Runtime runtime = Runtime.getRuntime();
		Properties prop = System.getProperties();
		String adbdevicecmd = "adb devices";
		test test1 = new test();
		String[] devicesids = test1.str8.split(",");
		String connectip = test1.connectip;
		String[] connectips = test1.connectip.split(",");
		if (connectip.indexOf("127.0.0.1") != -1 && connectip.indexOf("192.168") != -1) {
			try {
				BufferedReader cmdadbdevices = new BufferedReader(
						new InputStreamReader(runtime.exec(adbdevicecmd).getInputStream()));
				String line = null;
				while ((line = cmdadbdevices.readLine()) != null) {
					if (line.indexOf(String.valueOf("device")) != -1) {
						line = line.toString().split("device")[0].replaceAll(" ", "").replaceAll("	", "");
						deviceslist.add(line);
					}
				}
				deviceslist.remove(0);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			for (int deviceslen = 0; deviceslen < devicesids.length; deviceslen++) {
				deviceslist.add(devicesids[deviceslen]);
			}
		} else if (connectip.indexOf("127.0.0.1") != -1 && connectip.indexOf("192.168") == -1) {
			if (prop.getProperty("os.name") != null && prop.getProperty("os.name").indexOf("Mac") > -1) {
				try {
					BufferedReader cmdadbdevices = new BufferedReader(new InputStreamReader(
							runtime.exec("/volumes/software/work/eclipse/android/sdk/platform-tools/" + adbdevicecmd)
									.getInputStream()));
					String line = null;

					while ((line = cmdadbdevices.readLine()) != null) {
						if (line.indexOf(String.valueOf("device")) != -1) {
							line = line.toString().split("device")[0].replaceAll(" ", "").replaceAll("	", "");
							deviceslist.add(line);

						}
					}
					deviceslist.remove(0);

				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} else {
				try {
					BufferedReader cmdadbdevices = new BufferedReader(
							new InputStreamReader(runtime.exec(adbdevicecmd).getInputStream()));
					String line = null;

					while ((line = cmdadbdevices.readLine()) != null) {
						if (line.indexOf(String.valueOf("device")) != -1) {
							line = line.toString().split("device")[0].replaceAll(" ", "").replaceAll("	", "");
							deviceslist.add(line);
						}
					}
					deviceslist.remove(0);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		} else {
			for (int deviceslen = 0; deviceslen < devicesids.length; deviceslen++) {
				deviceslist.add(devicesids[deviceslen]);
			}
		}

		return deviceslist.toArray();
	}

}
