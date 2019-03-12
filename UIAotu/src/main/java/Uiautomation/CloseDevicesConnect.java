package Uiautomation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

public class CloseDevicesConnect {
    public void closeDevicesCmd(int executePort, String executeUdid, String executeDevicename, String uninstallcmd) {
        try {


            Properties prop = System.getProperties();
            if (prop.getProperty("os.name") != null && prop.getProperty("os.name").indexOf("Mac") > -1) { // 判断是否为Mac系统
                String executecmd = "lsof -i -P";
                String line = null;
                String[] executecmdarray = new String[15];
                String[] pidlistarray = new String[15];
                StringBuffer cmdresults = new StringBuffer();
                Runtime runtime = Runtime.getRuntime();
                BufferedReader cmdlines = new BufferedReader(
                        new InputStreamReader(runtime.exec(executecmd).getInputStream()));
                int count = 0;
                while ((line = cmdlines.readLine()) != null) {
                    if (line.indexOf(String.valueOf(executePort)) != -1) {
                        if (line.indexOf("LISTEN") != -1) {
                            cmdresults.append(line);
                            String cmdresult = cmdresults.toString().replaceAll(" ", ""); // 删除cmd命令结果多余的空格
                            executecmdarray[count] = cmdresult;
                            pidlistarray[count] = executecmdarray[0].split("steel")[0].substring(4);
                            count = count + 1;
                        }
                    }
                }
                System.out.print("卸载" + executeUdid + " APP" + "\r\n");
                new BufferedReader(new InputStreamReader(
                        runtime.exec("/volumes/software/work/eclipse/android/sdk/platform-tools/" + uninstallcmd)
                                .getInputStream()));// 卸载APP
                System.out.print("关闭设备：" + executeDevicename + " Appium" + "\r\n");
                new BufferedReader(new InputStreamReader(runtime.exec("kill  " + pidlistarray[0]).getInputStream())); // 关闭Appium的连接端口
            } else {
                String executecmd = "netstat -aon";
                String line = null;
                String[] executecmdarray = new String[15];
                String[] pidlistarray = new String[15];
                StringBuffer cmdresults = new StringBuffer();
                Runtime runtime = Runtime.getRuntime();
                BufferedReader cmdlines = new BufferedReader(
                        new InputStreamReader(runtime.exec(executecmd).getInputStream()));
                int count = 0;
                while ((line = cmdlines.readLine()) != null) {
                    if (line.indexOf(String.valueOf(executePort)) != -1) {
                        cmdresults.append(line);
                        String cmdresult = cmdresults.toString().replaceAll(" ", ""); // 删除cmd命令结果多余的空格
                        executecmdarray[count] = cmdresult;
                        if (executecmdarray[count].split("LISTENING").length < 2) {

                            pidlistarray[count] = executecmdarray[count].split("LISTENING")[0];
                        } else {
                            pidlistarray[count] = executecmdarray[count].split("LISTENING")[1];
                        }
                        count = count + 1;
                    }
                }
                System.out.print("卸载" + executeUdid + " APP" + "\r\n");
//                new BufferedReader(new InputStreamReader(runtime.exec(uninstallcmd).getInputStream()));// 卸载APP
                Thread.sleep(2000);
                String uninstallServer = "adb -s " + executeUdid + " uninstall " + "io.appium.uiautomator2.server";
                new BufferedReader(new InputStreamReader(runtime.exec(uninstallServer).getInputStream()));// 卸载io.appium.uiautomator2.server APP
                Thread.sleep(2000);
                String uninstallServerTest = "adb -s " + executeUdid + " uninstall " + "io.appium.uiautomator2.server.test";
                new BufferedReader(new InputStreamReader(runtime.exec(uninstallServerTest).getInputStream()));// 卸载io.appium.uiautomator2.server.test  APP
                Thread.sleep(2000);

                System.out.print("关闭设备：" + executeDevicename + " Appium" + "\r\n");
                new BufferedReader(
                        new InputStreamReader(runtime.exec("taskkill -f -pid " + pidlistarray[0]).getInputStream())); // 关闭Appium的连接端口
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("关闭Appium失败，Appium连接设备是：" + executeUdid);
        }
    }

}
