package Uiautomation;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.appium.java_client.android.AndroidDriver;

/**
 * Created by HL on 2018/9/5.
 */

public class MultiThread implements  Runnable {
    private AndroidDriver driver;
    private Thread t;
    private String threadName;
    private String executeDevicename;
    private String executeUdid;
    private int executePort;
    private String appPackages;
    private String appActivitys;
    private String platformNames;
    private String platformVersions;
    private String appPath;
    private String excuteSystem;
    private String connectIp;
    private String linuxApkPath;
    private String systemPort;

    MultiThread(String name, String deviceName, String udid, int port, String appPackage, String appActivity,
                 String platformName, String platformVersion, String appPaths, String connectip, String linuxapkpath,String systemPort) {
        threadName = name;
        executeDevicename = deviceName;
        executeUdid = udid;
        executePort = port;
        appPackages = appPackage;
        appPath = appPaths;
        appActivitys = appActivity;
        platformNames = platformName;
        platformVersions = platformVersion;
        connectIp = connectip;
        linuxApkPath = linuxapkpath;
        this.systemPort=systemPort;
        // appium 需要的参数进行重新赋值
        System.out.print("设备：" + executeDevicename + ",设备UDID：" + executeUdid + ",设备端口号：" + executePort + ",ip地址是:"
                + connectIp + "\r\n");
    }

    public void run() {
        List<WebElement> bot;
        String actRes;
        String uninstallcmd = "adb -s " + executeUdid + " uninstall " + appPackages;
        boolean acceptNextAlert = true;
        StringBuffer verificationErrors = new StringBuffer();
        int totalCaseNumbers;
        List<Object[]> records = new ArrayList<Object[]>();
        String testCaseResult = "";
        test configInfo = new test();
        try {
            test.testmethod();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        File allTest, singleTest;
        String result3 = configInfo.str3;// 读取环境配置信息：test.preperites
        String result1 = configInfo.str1;
        allTest = new File(result3 + "测试用例集.xlsx");
        if (!allTest.exists()) {
            System.out.println("测试用例集文件不存在");
            return;
        }
        AllCaseProcess allCP = new AllCaseProcess();
        try {
            allCP.writeallHeader(executeDevicename);// 生成总体测试报告的标题行
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        try {
            records = allCP.getallTests(allTest);// 读取所有测试用例，返回所有用例的数据信息
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        CloseDevicesConnect closeConnect = new CloseDevicesConnect(); // 调用APP关闭命令,执行APP关闭
        totalCaseNumbers = records.size();
        DesiredCapabilities capabilities = new DesiredCapabilities();
        try {
            String connectlink;
            if (connectIp.indexOf("192.168") != -1) {
                connectlink = connectIp + ":" + executePort + "/wd/hub";
                appPath = linuxApkPath;

            } else {
                connectlink = "http://127.0.0.1:" + executePort + "/wd/hub";
            }

            // 配置连接的设备信息(uiautomator)
            capabilities.setCapability("deviceName", executeDevicename);
            capabilities.setCapability("udid", executeUdid);
            capabilities.setCapability("platformName", platformNames);
            capabilities.setCapability("platformVersion", platformVersions);
            capabilities.setCapability("app", appPath);
            capabilities.setCapability("appPackage", appPackages);
            capabilities.setCapability("appActivity", appActivitys);
//            capabilities.setCapability("unicodeKeyboard", "true");
//            capabilities.setCapability("resetKeyboard", "true");//个别手机因这两个配置会跑不起来
            capabilities.setCapability("noReset", "true");
            capabilities.setCapability("noSign", "true");
            capabilities.setCapability("chromedriverExecutableDir", configInfo.str6);
            //重新创建chromeDriver
            capabilities.setCapability("recreateChromeDriverSessions","true");
            //运行时权限（自动接受所有的权限）
            capabilities.setCapability("autoGrantPermissions", "true");
            // 配置连接的设备信息(uiautomator2)
            capabilities.setCapability("automationName", "UiAutomator2");
            capabilities.setCapability("systemPort", systemPort);


            driver = new AndroidDriver(new URL(connectlink), capabilities);
            System.out.println("设备："+executeDevicename+"sessionId"+ driver.getSessionId());
            driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

            for (int i = 0; i < totalCaseNumbers; i++) {
                Object[] value = records.get(i);
                String testCaseName = (String) value[1];// 获取测试用例配置文件中的测试用例
                String whetherExec = (String) value[2];
                whetherExec.trim();
                if (whetherExec.equals("y") || whetherExec.equals("Y")) {
                    singleTest = new File(result1 + testCaseName + ".xlsx"); // 要不要执行该测试用例
                    if (!singleTest.exists()) {
                        System.out.println("文件" + testCaseName + "不存在" + singleTest.getAbsolutePath());
                        continue;
                    }
                    SingleCaseProcess sCP = new SingleCaseProcess();
                    System.out.println("设备:" + executeDevicename + "执行测试用例: " + testCaseName);
                    sCP.setCaseSequence(testCaseName,executeDevicename); // 根据case名称，设置数据文件中对应的行号
                    try {
                        sCP.processHandle(singleTest, driver, testCaseName, executeDevicename);
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                    String execResult = sCP.getCaseExecResult();
                    allCP.writeAllResult(execResult, i);

                }
            }
            closeConnect.closeDevicesCmd(executePort, executeUdid, executeDevicename, uninstallcmd);

        } catch (Exception e) {
            System.out.println(
                    "连接设备出错，请检查端口号和连接的设备udid,应该连接的设备是：" + executeUdid + ",端口号：" + executePort + ",ip地址是:" + connectIp);
            closeConnect.closeDevicesCmd(executePort, executeUdid, executeDevicename, uninstallcmd);

        }
    }

    public void start() {
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }
}
