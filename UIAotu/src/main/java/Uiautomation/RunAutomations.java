package Uiautomation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by HL on 2018/9/5.
 */

public class RunAutomations {
    public static void main(String args[]){
        test test1 = new test();
        try {
            test.testmethod();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 读取Test.properties 配置信息
        String appPackage = test1.appPackage;
        String appActivity = test1.str11;
        String platformName = "Android";
        String platformVersion = test1.str9;
        String appPaths = test.str10;
        String[] connectip = test1.connectip.split(",");
        String[] devicescount = test1.devicescount.split(",");
        String linuxapkpath = test1.linuxapkpath;
        String[] systemPorts = test1.systemPorts.split(",");

        Object[] deviceUdid = GetDevices.getUdid();
        if (deviceUdid == null || deviceUdid.length < 1) {
            System.out.println("无设备连接");
            return;
        }
        System.out.println("devices count：" + deviceUdid.length);

        // 获取设备ID
        Object[] connectPort = (Object[]) AppiumServerManager.startAppiumServer(deviceUdid.length).toArray();
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 获取连接的端口好,判断设备数是否大于端口数 deviceUdid.length 设备数
        if (deviceUdid.length <= 1 && connectip.length > 1) {
            System.out.print("请检查配置文件中设备数和IP是否正确,IP数如果大于1,设备数则必须大于1。");
        } else {
            int lastLength = 0;
            for (int i = 0; i < connectip.length; i++) {
                int total = lastLength + Integer.parseInt(devicescount[i]);
                for (int j = lastLength; j < lastLength + Integer.parseInt(devicescount[i]); j++) {
                    MultiThread executedevices[] = new MultiThread[deviceUdid.length];
                    executedevices[j] = new MultiThread(String.valueOf(deviceUdid[j]), String.valueOf(deviceUdid[j]),
                            String.valueOf(deviceUdid[j]), (int) connectPort[j], appPackage, appActivity, platformName,
                            platformVersion, appPaths, connectip[i], linuxapkpath,systemPorts[j]);
                    executedevices[j].start();
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                lastLength = total;
            }
        }
        // 多设备或只有多端口时，创建多线程进行运行

    }
}
