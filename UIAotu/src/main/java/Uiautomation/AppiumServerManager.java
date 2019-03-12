package Uiautomation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AppiumServerManager {
	/**
	 * star server
	 * 
	 * @param count
	 * @return
	 */

	public static List<Integer> startAppiumServer(int count) {
		List<String> deviceslist = new ArrayList<String>();
		Runtime runtime = Runtime.getRuntime();
		try {
			BufferedReader cmdadbdevices = new BufferedReader(
					new InputStreamReader(runtime.exec("adb devices").getInputStream()));
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
		int[] ports = radomPort(deviceslist.size());
		Properties prop = System.getProperties();
		List<Integer> successPorts = new ArrayList<Integer>();
		test test1 = new test();
		String connectip = test1.connectip;
		String[] devicesports = test1.port.split(",");
		if (connectip.indexOf("127.0.0.1") != -1 && connectip.indexOf("192.168") != -1) {
			for (int i = 0; i < ports.length; i++) {
				int p = excuteCmd(ports[i]);
				if (p != 0) {
					successPorts.add(p);
				}
			}
			for (int devicesportlen = 0; devicesportlen < devicesports.length; devicesportlen++) {
				successPorts.add(Integer.parseInt(devicesports[devicesportlen]));
			}
		} else if (connectip.indexOf("127.0.0.1") != -1 && connectip.indexOf("192.168") == -1) {
			if (prop.getProperty("os.name") != null && prop.getProperty("os.name").indexOf("Mac") > -1) {
				for (int devicesportlen = 0; devicesportlen < devicesports.length; devicesportlen++) {
					successPorts.add(Integer.parseInt(devicesports[devicesportlen]));
				}
			} else {
				if (ports != null) {
					for (int i = 0; i < ports.length; i++) {
						int p = excuteCmd(ports[i]);
						if (p != 0) {
							successPorts.add(p);
						}
					}
				}
			}
		} else {
			for (int devicesportlen = 0; devicesportlen < devicesports.length; devicesportlen++) {
				successPorts.add(Integer.parseInt(devicesports[devicesportlen]));
			}
		}
		return successPorts;
	}

	/**
	 * 执行cmd命令
	 * 
	 * @param port
	 */
	public static int excuteCmd(int port) {
		Runtime runtime = Runtime.getRuntime();
		Properties prop = System.getProperties();
		int defPort = 0;
		int bootstrap =port+1990;
		if (prop.getProperty("os.name") != null && prop.getProperty("os.name").indexOf("Mac") > -1) {
			defPort = 0;
		} else {
			try {
				runtime.exec("cmd.exe /c start cmd.exe /k \"appium -a 127.0.0.1 -p " + port
						+ " -bp "+bootstrap+" --session-override -dc \"{\"\"noReset\"\": \"\"true\"\"}\"\"");
				Thread.sleep(2000);
				defPort = port;
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
				System.out.println("启动Appium失败，请检查本地端口是否存在。");
			}

		}
		return defPort;

	}

	/**
	 * 随机端口号
	 * 
	 * @param count
	 * @return
	 */
	public static int[] radomPort(int count) {
		if (count < 1) {
			System.out.println("count must be >0!!");
			return null;
		}
		int[] randoms = new int[count];
		for (int i = 0; i < count; i++) {
			int num = (int) (Math.random() * 1999) + 4000;
			
			for (int j = 0; j < i; j++) {
				if (num==randoms[j]) {
					num=(int) (Math.random() * 1999) + 4000;
				}
			}

			randoms[i] = num;
		}
		return randoms;
	}

	public static void main(String[] args) {
		AppiumServerManager.startAppiumServer(1);
	}
}
