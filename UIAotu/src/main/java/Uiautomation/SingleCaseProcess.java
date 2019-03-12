package Uiautomation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;

/*
 * 获取Excel文件的内容,使用Workbook方式来读取excel，Excel文件的行号和列号都是从0开始的
 */
public class SingleCaseProcess {
    public static boolean a = true;
    boolean stepExec = true;
    public boolean isLoopStep = false;
    public int loopCount = 0;
    public int caseSequence, maxWaitTime = 15;
    String resultMessage = null; // 非检查点的步骤，写入测试报告时，保存错误信息
    String checkResult = null; // 检查点步骤，写入测试报告时，保存检查结果
    String actualValue = null;//检查点步骤，元素取到的实际值
    String expectedValue = null; // 检查点步骤，期待值
    String caseExecResult = "Pass";//用例执行结果
    String phoneNumber, cardNumber, idNumber, readedText, testDataType, totalText;
    String appType; // app项目名称
    int regNewPhoneNumber = 0;
    List<WebElement> bot;
    ExcelOperation excel = new ExcelOperation();
    test test1 = new test();
    TestData TD = new TestData();
    TestGesture TG = new TestGesture();

    String nativeApp = null;
    String webviewApp = null;

    public void setCaseSequence(String caseName, String devicesName) {
        // caseSequence = CSQ;
        TD.setNowNumber(caseName, devicesName);// 设置“测试数据相关.xlsx”中的行序号

    }

    public void processHandle(File file, AndroidDriver driver, String testCaseName, String executeDevicename) throws IOException {
        try {

            writeHeaderSingleCase(testCaseName, executeDevicename);// 写入单个测试用例执行报告的标题行
            excel.setCaseName(testCaseName);// 给excel变量传入用例名称，方便在写测试报告时将用例名称作为报告名称的一部分
            TD.getColFromDataFiles(executeDevicename); // 将数据文件中的所有列名与列号的对应关系读入哈希表中
            appType = test.str12; // 从配置文件test.properties获取app的类型
            InputStream inputStream = new FileInputStream(file);
            Workbook workbook = null;
            List<String[]> loopList = new ArrayList<>();

            try {
                workbook = new XSSFWorkbook(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            } // 解析xlsx格式
            Sheet sheet = workbook.getSheet("Sheet1");// 第一个工作表
            int rowCount = sheet.getLastRowNum() - sheet.getFirstRowNum();// sheet1数据的行数
            for (int i = 1; i <= rowCount; i++) {
                resultMessage = "";
                checkResult = "";
                actualValue = "";
                expectedValue = "";


                Row row = sheet.getRow(i);// 获取行对象
                if (row == null)
                    return; // 判断Excel是否有多余的空行数
                String[] value = new String[8];
                for (int j = 0; j < 7; j++) {
                    if (row.getCell(j) == null) {
                        value[j] = "";
                        continue;
                    }
                    row.getCell(j).setCellType(Cell.CELL_TYPE_STRING);
                    value[j] = row.getCell(j).getStringCellValue().trim();// 操作类型
                }
                //执行用例
                excuteCase(value, driver, executeDevicename, i);
                //判断是否循环添加至循环队列
                if (isLoopStep && stepExec && !value[0].equals("循环操作")) {
                    value[7] = String.valueOf(i);
                    loopList.add(value);
                }
                if (loopCount > 0 && isLoopStep && value[0].equals("end")) {
                    for (int k = 0; k < loopCount - 1; k++) {
                        for (int j = 0; j < loopList.size(); j++) {
                            String[] newVlue = loopList.get(j);
                            excuteCase(loopList.get(j), driver, executeDevicename, Integer.parseInt(newVlue[7]));
                        }
                    }

                } else {
                    //从队列里开始循环
                    if (isLoopStep && stepExec && value[0].equals("end")) {

                        if (loopList != null && loopList.size() > 0) {
                            boolean iscontinue = true;
                            while (iscontinue) {
                                for (int j = 0; j < loopList.size(); j++) {
                                    String[] newVlue = loopList.get(j);
                                    excuteCase(loopList.get(j), driver, executeDevicename, Integer.parseInt(newVlue[7]));
                                    if (!stepExec) {
                                        iscontinue = false;
                                    }
                                }

                            }
                        }
                    }
                }

                try {
                    Thread.sleep(1200);
                } catch (InterruptedException e) {
                    // e.getMessage();
                    e.printStackTrace();
                }
                if (caseExecResult.equals("fail")) {
                    continue;
                }
            }
            // driver.quit();
            System.out.println("设备" + executeDevicename + ":用例全部结束");
        } catch (Exception e)

        {
            e.printStackTrace();
        }

    }

    public String getCaseExecResult() {
        return caseExecResult;
    }

    public void writeHeaderSingleCase(String caseName, String executeDevicename) throws IOException {
        test1.testmethod();
        String result2 = test1.str2;
        String excelPath = result2 + executeDevicename + "-" + String.valueOf(DateUtil.getYear(excel.date)) + "-" + String.valueOf(DateUtil.getMonth(excel.date)) + "-"
                + String.valueOf(DateUtil.getDay(excel.date)) + "-" + String.valueOf(DateUtil.getHour(excel.date)) + "-" + String.valueOf(DateUtil.getMinute(excel.date)) + caseName + ".xlsx";
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("testdata1");
        FileOutputStream outputStream = new FileOutputStream(excelPath);
        try {
            Row row0 = sheet.createRow(0);
            String[] headers = new String[]{"编号", "检查点", "实际结果", "预期结果", "检查结果", "错误描述"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell_1 = row0.createCell(i, Cell.CELL_TYPE_STRING);
                CellStyle style = excel.getStyle(workbook);
                cell_1.setCellStyle(style);
                cell_1.setCellValue(headers[i]);
                sheet.autoSizeColumn(i);
            }
            workbook.write(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void excuteCase(String[] value, AndroidDriver driver, String executeDevicename, int i) {
        Set<String> contextNames = null;


        try {
            stepExec = WarpingFunctions.getIfCaseExec(driver, value[0], value[3], stepExec, value[2], value[1], value[5]); // 判断该步骤是否需要执行。
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        if ((stepExec == false)) // 如果不需要执行，直接打印该步骤的日志
        {

            resultMessage = "条件不成立该步骤不执行";
            System.out.println("设备" + executeDevicename + ": 用例编号" + (i + 1) + resultMessage + "\r");
            excel.writeResult(value[4], resultMessage, executeDevicename);// 写入单个测试用例单个步骤的执行结果

        } else // 否则清空错误日志，用例步骤顺序执行
        {
            resultMessage = "";

            contextNames = driver.getContextHandles();
            for (String contextName : contextNames) {
                // 用于返回被测app是NATIVE_APP还是WEBVIEW，如果两者都有就是混合型App
                if (contextName.contains("NATIVE")) {
                    nativeApp = contextName;
                }
                if (contextName.contains("WEBVIEW_com.ushaqi.zhuishushenqi")) {
                    // 让appium切换到webview模式以便查找web元素
                    webviewApp = contextName;
                }
            }

            //切换至H5
            if (value[6] != null && !value[6].equals("")) {
                if (value[6].equals("H5") || value[6].equals("h5")) {

                    if (webviewApp != null && !webviewApp.equals("")) {
                        driver.context(contextNames.toArray()[1].toString());
                        System.out.println("切换到webview：" + webviewApp + "driver.getcontext" + driver.getContext());
                        try {
                            Thread.sleep(2000);
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }
            }
            switch (value[0]) {


                case "if_文本包含":
                    excel.writeResult(value[4], resultMessage, executeDevicename);// 写入单个测试用例单个步骤的执行结果
                    break;

                case "end":
                    excel.writeResult(value[4], resultMessage, executeDevicename);// 写入单个测试用例单个步骤的执行结果
                    break;

                case "手势密码_xpath":
                    try {
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        bot = driver.findElements(By.xpath(value[1]));
                        TG.patternLock(value[2], bot, driver);
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);// 写入单个测试用例单个步骤的执行结果
                    break;

                case "滑动屏幕":
                    try {
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        TG.swipeTo(value[1], driver, executeDevicename);
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);// 写入单个测试用例单个步骤的执行结果
                    break;

                //不建议使用的方法
//                        case "滚动查找点击元素":// 只能第一页查找，不能翻页查找元素
//                            try {
//                                WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
//                                driver.scrollTo(value[1]).click();
//                            } catch (Exception e) {
//                                resultMessage = e.getMessage();
//                                caseExecResult = "failure";
//                            }
//                            excel.writeResult(value[4], resultMessage, executeDevicename);// 写入单个测试用例单个步骤的执行结果
//                            break;

                case "向下滑动查找元素_id":// 可以翻页查找元素
                    try {
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        TG.swipeToelement(value[1], driver, value[3], value[5]);
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);// 写入单个测试用例单个步骤的执行结果
                    break;
                case "滑动查找元素_id":// 可以翻页查找元素,可以进行不同方向的滑动，包括上下左右

                    try {
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        TG.swipeToelementAllD(value[1], driver, value[3], bot, value[5]);
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);// 写入单个测试用例单个步骤的执行结果
                    break;
                case "滑动目录并点击_id":// 向上滑动到某元素的位置，并点击该元素
                    try {
                        resultMessage = "";
                        String tmpReadedText = "";
                        String direction = "up";
                        if (appType.equals("ZS") || appType.equals("zs")) // 对于开卷项目，目录页向下滑动;追书与漫画岛则向上滑动
                        {
                            direction = "down";
                        }
                        if (value[3].equals("")) {
                            tmpReadedText = value[5]; // 如果未传入取值类型，需要在value[5]中直接传入要点击的章节，否则，从测试数据文件中读取要点击的章节。
                        } else {
                            tmpReadedText = TD.getTestData(value[3], executeDevicename); // 要点击的内容为，包含"第"+章节号的文本内容
                        }

                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        TG.swipeAndClick(value[1], driver, tmpReadedText, direction);
                        resultMessage = TG.getReturnMessage();
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);// 写入单个测试用例单个步骤的执行结果
                    break;
                case "滑动到底部":
                    try {
                        String str1;
                        String str2;
                        int width = driver.manage().window().getSize().width;
                        int height = driver.manage().window().getSize().height;
                        int tmpCount = 0;
                        do {
                            tmpCount = tmpCount + 1;
                            str1 = driver.getPageSource(); // 滑动前获取pagesource
//                                    driver.swipe(width / 2, height * 9 / 10, width / 2, height / 10, 2000);
                            TouchAction touchAction = new TouchAction(driver);
                            touchAction.press(width / 2, height * 9 / 10).waitAction(Duration.ofMillis(2000)).moveTo(width / 2, height / 10).release().perform();

                            Thread.sleep(2000);
                            str2 = driver.getPageSource();// 滑动后获取pagesource
                        } while (!(str1.equals(str2)) && tmpCount <= 10); // 最大滑动10次
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);// 写入单个测试用例单个步骤的执行结果
                    break;
                case "轻触屏幕中央":
                    try {
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        TG.getReadSet(driver, appType, value[3]);
                        resultMessage = TG.getReturnMessage();
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);// 写入单个测试用例单个步骤的执行结果
                    break;

                case "滑动_seekBar":
                    try {
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        WebElement seekBar = null;
                        if (value[2].equals("")) {
                            if (wait.until(ExpectedConditions.elementToBeClickable(By.id(value[1]))) != null) {

                                seekBar = driver.findElement(By.id(value[1]));
                            }
                        } else {
                            wait.until(ExpectedConditions.elementToBeClickable((WebElement) driver.findElements(By.id(value[1])).get(Integer.parseInt(value[2]))));
                            bot = driver.findElements(By.id(value[1]));
                            seekBar = bot.get(Integer.parseInt(value[2]));
                        }
                        String fromPercent = (value[5] == "") ? "0" : value[5];
                        String toPercent = (value[3] == "") ? "0" : value[3];
                        TG.moveSeekBar(seekBar, driver, Float.parseFloat(fromPercent), Float.parseFloat(toPercent));

                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);
                    break;

                case "点击_AccessibilityId":
                    try {
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        driver.findElementByAccessibilityId(value[1]).click();
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);// 写入单个测试用例单个步骤的执行结果
                    break;

                case "点击_id":
                    try {
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定

                        if (value[2].equals("")) {
                            if (wait.until(ExpectedConditions.elementToBeClickable(By.id(value[1]))) != null) {
                                driver.findElement(By.id(value[1])).click();
                            }

                        } else {
                            wait.until(ExpectedConditions.elementToBeClickable((WebElement) driver.findElements(By.id(value[1])).get(Integer.parseInt(value[2]))));
                            bot = driver.findElements(By.id(value[1]));
                            bot.get(Integer.parseInt(value[2])).click();
                        }

                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);
                    break;

                case "点击_xpath":

                    try {
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        if (value[2].equals("")) {
                            if (wait.until(ExpectedConditions.elementToBeClickable(By.xpath(value[1]))) != null) {

                                driver.findElement(By.xpath(value[1])).click();
                            }
                        } else {
                            wait.until(ExpectedConditions.elementToBeClickable((WebElement) driver.findElements(By.xpath(value[1])).get(Integer.parseInt(value[2]))));
                            bot = driver.findElements(By.xpath(value[1]));
                            bot.get(Integer.parseInt(value[2])).click();
                        }
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);
                    break;

                case "点击_className":
                    try {
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定

                        if (value[2].equals("")) {
                            wait.until(ExpectedConditions.elementToBeClickable(By.className(value[1])));
                            driver.findElement(By.className(value[1])).click();
                        } else {
                            wait.until(ExpectedConditions.elementToBeClickable((WebElement) driver.findElements(By.className(value[1])).get(Integer.parseInt(value[2]))));
                            bot = driver.findElements(By.className(value[1]));
                            bot.get(Integer.parseInt(value[2])).click();
                        }
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);
                    break;

                case "长按_id":
                    try {
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        TouchAction touchAction = new TouchAction(driver);
                        if (value[2].equals("")) {
                            if (wait.until(ExpectedConditions.elementToBeClickable(By.id(value[1]))) != null) {
                                WebElement test = driver.findElement(By.id(value[1]));
                                if (test != null) {
                                    touchAction.longPress(test, Duration.ofMillis(4000));
                                    driver.performTouchAction(touchAction);


                                }
                            }

                        } else {
                            wait.until(ExpectedConditions.elementToBeClickable((WebElement) driver.findElements(By.id(value[1])).get(Integer.parseInt(value[2]))));
                            bot = driver.findElements(By.id(value[1]));
                            WebElement test = bot.get(Integer.parseInt(value[2]));
                            touchAction.longPress(test, Duration.ofMillis(4000)).release().perform();

                        }

                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);
                    break;

                case "长按_xpath":
                    try {
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        TouchAction touchAction = new TouchAction(driver);
                        if (value[2].equals("")) {
                            wait.until(ExpectedConditions.elementToBeClickable(By.xpath(value[1])));
                            WebElement wl = driver.findElement(By.xpath(value[1]));
                            touchAction.longPress(wl, Duration.ofMillis(4000)).release().perform();
                        } else {
                            wait.until(ExpectedConditions.elementToBeClickable((WebElement) driver.findElements(By.xpath(value[1])).get(Integer.parseInt(value[2]))));
                            bot = driver.findElements(By.xpath(value[1]));
                            WebElement wl = bot.get(Integer.parseInt(value[2]));
                            touchAction.longPress(wl, Duration.ofMillis(4000)).release().perform();

                        }

                    } catch (Exception e) {
                        resultMessage = e.getMessage();

                        // investorLogin.invesTag="fail";
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);
                    break;

                case "输入_xpath":
                    try {
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(value[1])));
                        if (value[2].equals("")) {
                            driver.findElement(By.xpath(value[1])).sendKeys(value[3]);
                        } else {
                            bot = driver.findElements(By.xpath(value[1]));
                            bot.get(Integer.parseInt(value[2])).sendKeys(value[3]);
                        }
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);
                    break;

                case "输入_id":
                    try {
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        wait.until(ExpectedConditions.presenceOfElementLocated(By.id(value[1])));
                        if (value[2].equals("")) {
                            driver.findElement(By.id(value[1])).sendKeys(value[3]);
                        } else {
                            bot = driver.findElements(By.id(value[1]));
                            bot.get(Integer.parseInt(value[2])).sendKeys(value[3]);
                        }

                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);
                    break;

                case "输入_classname":
                    try {
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        wait.until(ExpectedConditions.presenceOfElementLocated(By.name(value[1])));
                        driver.findElement(By.name(value[1])).sendKeys(value[3]);
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);
                    break;
                case "输入_文本":
                    try {

                        String tmpReadedText;
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        wait.until(ExpectedConditions.presenceOfElementLocated(By.id(value[1])));
                        tmpReadedText = TD.getTestData(value[3], executeDevicename);// 从测试数据文件中读取要检查的文本，作为期望值。
                        driver.findElement(By.id(value[1])).sendKeys(tmpReadedText);
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);
                    break;

                case "回退":
                    try {
                        driver.pressKeyCode(4);

                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);
                    break;
                case "隐藏键盘":
                    try {
                        driver.hideKeyboard();

                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);
                    break;
                case "读取信息_id":
                    try {
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        wait.until(ExpectedConditions.presenceOfElementLocated(By.id(value[1])));
                        if (value[2].equals("")) {
                            readedText = driver.findElement(By.id(value[1])).getText(); // 要获取的页面元素的文本内容
                        } else {
                            bot = driver.findElements(By.id(value[1]));
                            readedText = bot.get(Integer.parseInt(value[2])).getText();
                        }

                        testDataType = value[3]; // 要获取的文本内容的类型，value[3]中的值应该与“测试相关数据.xlsx"文档中的标题行的信息相匹配;
                        TD.setTestData(testDataType, readedText, executeDevicename);
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);
                    break;
                case "读取信息_xpath":
                    try {
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(value[1])));
                        if (value[2].equals("")) {
                            readedText = driver.findElement(By.xpath(value[1])).getAttribute("name"); // 要获取的页面元素的content-desc里的内容
                        } else {
                            bot = driver.findElements(By.xpath(value[1]));
                            readedText = bot.get(Integer.parseInt(value[2])).getAttribute("name");
                        }

                        testDataType = value[3]; // 要获取的文本内容的类型，value[3]中的值应该与“测试相关数据.xlsx"文档中的标题行的信息相匹配;
                        TD.setTestData(testDataType, readedText, executeDevicename);
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);
                    break;
                case "读取局部信息_id":
                    try {
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        wait.until(ExpectedConditions.presenceOfElementLocated(By.id(value[1])));
                        if (value[2].equals("")) {
                            readedText = driver.findElement(By.id(value[1])).getText(); // 要获取的页面元素的文本内容
                        } else {
                            bot = driver.findElements(By.id(value[1]));
                            readedText = bot.get(Integer.parseInt(value[2])).getText();
                        }

                        testDataType = value[3]; // 要获取的文本内容的类型，value[3]中的值应该与“测试相关数据.xlsx"文档中的标题行的信息相匹配;
                        String filteredText = WarpingFunctions.getFiltedText(readedText, value[5]);
                        TD.setTestData(testDataType, filteredText, executeDevicename);
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);
                    break;
                case "读取局部信息_xpath":
                    try {

                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(value[1])));
                        if (value[2].equals("")) {
                            readedText = driver.findElement(By.xpath(value[1])).getAttribute("name"); // 要获取的页面元素的content-desc里的内容
                        } else {
                            bot = driver.findElements(By.xpath(value[1]));
                            readedText = bot.get(Integer.parseInt(value[2])).getAttribute("name"); // 要获取的页面元素的content-desc里的内容
                        }

                        testDataType = value[3]; // 要获取的文本内容的类型，value[3]中的值应该与“测试相关数据.xlsx"文档中的标题行的信息相匹配;
                        String filteredText = WarpingFunctions.getFiltedText(readedText, value[5]);
                        TD.setTestData(testDataType, filteredText, executeDevicename);
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);
                    break;

                case "漫画岛设置目录顺序_id":
                    try {
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        wait.until(ExpectedConditions.presenceOfElementLocated(By.id(value[1])));
                        readedText = driver.findElement(By.id(value[1])).getText(); // 要获取的页面元素的文本内容
                        if (!(readedText.equals(value[3].trim()))) // 如果当前的目录顺序与传入的期望值不一致，点击“顺序/倒序”按钮
                        {
                            driver.findElement(By.id(value[1])).click();
                        }
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);
                    break;
                case "状态设置_id":
                    try {
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        wait.until(ExpectedConditions.presenceOfElementLocated(By.id(value[1])));
                        readedText = driver.findElement(By.id(value[1])).getText(); // 要获取的页面元素的文本内容
                        if (!(readedText.equals(value[3].trim()))) // 如果传入id对应元素的当前值与传入的期望值不一致，点击id对应按钮
                        {
                            driver.findElement(By.id(value[1])).click();
                        }
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);
                    break;
                case "增加操作":
                    try {
                        int currentNumber;

                        Pattern pattern = Pattern.compile("^[0-9]*$");
                        String dianzan = TD.getTestData(value[3], executeDevicename);
                        Matcher isNum = pattern.matcher(dianzan);

                        if (!isNum.matches()) {
                            currentNumber = 1;
                            System.out.println("不匹配");
                        } else {
                            currentNumber = Integer.parseInt(dianzan) + 1;
                            System.out.println("匹配");
                        }
                        TD.setTestData(value[3], Integer.toString(currentNumber), executeDevicename);

                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);
                    break;

                case "取消操作":
                    try {
                        int currentNumber = 0;

                        Pattern pattern = Pattern.compile("^[0-9]*$");
                        String dianzan = TD.getTestData(value[3], executeDevicename);
                        Matcher isNum = pattern.matcher(dianzan);

                        if (isNum.matches()) {
                            currentNumber = Integer.parseInt(dianzan) - 1;
                            TD.setTestData(value[3], Integer.toString(currentNumber), executeDevicename);
                            System.out.println("匹配");
                        } else {
                            System.out.println("不匹配");
                        }

                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);
                    break;
                case "漫画岛设置收藏已收藏_id":
                    try {
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        wait.until(ExpectedConditions.presenceOfElementLocated(By.id(value[1])));
                        readedText = driver.findElement(By.id(value[1])).getText(); // 要获取的页面元素的文本内容
                        if (!(readedText.equals(value[3].trim()))) // 如果当前的目录顺序与传入的期望值不一致，点击“顺序/倒序”按钮
                        {
                            driver.findElement(By.id(value[1])).click();
                        }
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);
                    break;
                case "带上书架操作退出阅读器_文本":
                    try {
                        Thread.sleep(2000);
                        bot = driver.findElements(By.name(value[3]));
                        if (bot.size() == 1) {
                            driver.findElement(By.name(value[3])).click();
                        }
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);
                    break;
                case "分享之前登录_文本":
                    try {
                        Thread.sleep(2000);
                        bot = driver.findElements(By.name(value[3]));
                        if (bot.size() == 1) {
                            driver.findElement(By.name(value[3])).click();
                        }
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);
                    break;
                case "检查点_xpath":
                    try {
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(value[1])));
                        actualValue = driver.findElement(By.xpath(value[1])).getText();
                        expectedValue = value[5];
                        checkResult = WarpingFunctions.verifyTest(actualValue, expectedValue);
                        if ((checkResult == "" || checkResult == "fail")) {
                            FileUtil.takeTakesScreenshot(driver);
                            resultMessage = FileUtil.filePath;
                            caseExecResult = "fail";
                        }
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        checkResult = "failure";
                        caseExecResult = "failure";
                    }
                    excel.writeCheckResult(value[4], resultMessage, checkResult, actualValue, expectedValue, executeDevicename);
                    break;

                case "检查点_toast":
                    try {
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//android.widget.Toast[1]")));

                        WebElement toastView = driver.findElement(By.xpath("//android.widget.Toast[1]"));

                        actualValue = toastView.getAttribute("name");
                        expectedValue = value[5];
                        checkResult = WarpingFunctions.verifyTest(actualValue, expectedValue);

                        if ((checkResult == "" || checkResult == "fail")) {
                            FileUtil.takeTakesScreenshot(driver);
                            resultMessage = FileUtil.filePath;
                            caseExecResult = "fail";
                        }
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        checkResult = "failure";
                        caseExecResult = "failure";
                    }
                    excel.writeCheckResult(value[4], resultMessage, checkResult, actualValue, expectedValue, executeDevicename);
                    break;

                case "检查点_id":
                    try {
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        wait.until(ExpectedConditions.presenceOfElementLocated(By.id(value[1])));
                        if (value[2].equals("")) {
                            actualValue = driver.findElement(By.id(value[1])).getText();
                        } else {
                            bot = driver.findElements(By.id(value[1]));
                            actualValue = bot.get(Integer.parseInt(value[2])).getText();
                        }

                        expectedValue = value[5];
                        checkResult = WarpingFunctions.verifyTest(actualValue, expectedValue);
                        if ((checkResult == "" || checkResult == "fail")) {
                            FileUtil.takeTakesScreenshot(driver);
                            resultMessage = FileUtil.filePath;
                            caseExecResult = "fail";
                        }
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        checkResult = "failure";
                        caseExecResult = "failure";
                    }
                    excel.writeCheckResult(value[4], resultMessage, checkResult, actualValue, expectedValue, executeDevicename);
                    break;
                case "检查点_文本存在":
                    try {
                        String checkedText;
                        Thread.sleep(2000);
                        String pageSourceString;
                        pageSourceString = driver.getPageSource(); // 获取页面pagesource
                        if (value[3].equals("")) {
                            checkedText = value[5]; // 如果未传入取值类型，需要在value[3]中直接传入要检查的文本，否则，从测试数据文件中读取要检查的文本，作为期望值。
                        } else {
                            checkedText = TD.getTestData(value[3], executeDevicename);
                        }

                        actualValue = checkedText;
                        expectedValue = checkedText; // 默认情况下，假定校验成功，在期望结果与实际结果一样
                        int count = 0;
                        while ((checkResult == "" || checkResult == "fail") && count < 3) {
                            pageSourceString = driver.getPageSource(); // 获取页面pagesource
                            Thread.sleep(1000);
                            checkResult = WarpingFunctions.verifyContainTest(pageSourceString, checkedText, "y");
                            count++;
                        }

                        if ((checkResult == "" || checkResult == "fail")) {
                            FileUtil.takeTakesScreenshot(driver);
                            resultMessage = FileUtil.filePath;
                            caseExecResult = "fail";
                            actualValue = "";
                        }
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        checkResult = "failure";
                        caseExecResult = "failure";
                        actualValue = "";
                    }
                    excel.writeCheckResult(value[4], resultMessage, checkResult, actualValue, expectedValue, executeDevicename);
                    break;
                case "检查点_文本不存在":
                    try {
                        String checkedText;
                        String pageSourceString;
                        pageSourceString = driver.getPageSource(); // 获取页面pagesource
                        if (value[3].equals("")) {
                            checkedText = value[5]; // 如果未传入取值类型，需要在value[3]中直接传入要检查的文本，否则，从测试数据文件中读取要检查的文本，作为期望值。
                        } else {
                            checkedText = TD.getTestData(value[3], executeDevicename);
                        }
                        actualValue = checkedText;
                        expectedValue = checkedText; // 默认情况下，假定校验成功，在期望结果与实际结果一样
                        int count = 0;
                        while ((checkResult == "" || checkResult == "fail") && count < 2) {
                            pageSourceString = driver.getPageSource(); // 获取页面pagesource
                            Thread.sleep(1000);
                            checkResult = WarpingFunctions.verifyContainTest(pageSourceString, checkedText, "n");
                            count++;
                        }
                        if ((checkResult == "" || checkResult == "fail")) {
                            FileUtil.takeTakesScreenshot(driver);
                            resultMessage = FileUtil.filePath;
                            caseExecResult = "fail";
                            actualValue = checkedText;
                            expectedValue = pageSourceString;
                        }
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        checkResult = "failure";
                        caseExecResult = "failure";
                        actualValue = "";
                    }
                    excel.writeCheckResult(value[4], resultMessage, checkResult, actualValue, expectedValue, executeDevicename);
                    break;
                case "检查点_id_字段包含":
                    try {
                        String bigText, smallText;
                        Thread.sleep(2000);
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        if (value[2].equals("")) {
                            bigText = driver.findElement(By.id(value[1])).getText();
                        } else {
                            bot = driver.findElements(By.id(value[1]));
                            bigText = bot.get(Integer.parseInt(value[2])).getText();
                        }
                        if (value[3].equals("")) // value[3]中可以指定从数据文件读取的内容；如果value[3]为空，则要验证被包含的字段，直接从value[5]读取
                        {
                            smallText = value[5];
                        } else {
                            smallText = TD.getTestData(value[3], executeDevicename);
                        }

                        actualValue = smallText;
                        expectedValue = smallText; // 默认情况下，假定校验成功，在期望结果与实际结果一样
                        checkResult = WarpingFunctions.verifyContainTest(bigText, smallText, "y");
                        if ((checkResult == "" || checkResult == "fail")) {
                            FileUtil.takeTakesScreenshot(driver);
                            resultMessage = FileUtil.filePath;
                            caseExecResult = "fail";
                            actualValue = bigText;
                        }
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        checkResult = "failure";
                        caseExecResult = "failure";
                        actualValue = "";
                    }
                    excel.writeCheckResult(value[4], resultMessage, checkResult, actualValue, expectedValue, executeDevicename);
                    break;
                case "检查点_xpath_字段包含":
                    try {
                        String bigText, smallText;
                        Thread.sleep(2000);
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        if (value[2].equals("")) {
                            bigText = driver.findElement(By.xpath(value[1])).getAttribute("name"); // 获取content
                            // desc里的信息
                        } else {
                            bot = driver.findElements(By.xpath(value[1]));
                            bigText = bot.get(Integer.parseInt(value[2])).getAttribute("name");
                        }
                        if (value[3].equals("")) // value[3]中可以指定从数据文件读取的内容；如果value[3]为空，则要验证被包含的字段，直接从value[5]读取
                        {
                            smallText = value[5];
                        } else {
                            smallText = TD.getTestData(value[3], executeDevicename);
                        }

                        actualValue = smallText;
                        expectedValue = smallText; // 默认情况下，假定校验成功，在期望结果与实际结果一样

                        checkResult = WarpingFunctions.verifyContainTest(bigText, smallText, "y");
                        if ((checkResult == "" || checkResult == "fail")) {
                            FileUtil.takeTakesScreenshot(driver);
                            resultMessage = FileUtil.filePath;
                            caseExecResult = "fail";
                            actualValue = bigText;
                        }
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        checkResult = "failure";
                        caseExecResult = "failure";
                        actualValue = "";
                    }
                    excel.writeCheckResult(value[4], resultMessage, checkResult, actualValue, expectedValue, executeDevicename);
                    break;

                case "检查点_id_字段不包含":
                    try {
                        String bigText, smallText;
                        Thread.sleep(2000);
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        if (value[2].equals("")) {
                            bigText = driver.findElement(By.id(value[1])).getText();
                        } else {
                            bot = driver.findElements(By.id(value[1]));
                            bigText = bot.get(Integer.parseInt(value[2])).getText();
                        }

                        if (value[3].equals("")) // value[3]中可以指定从数据文件读取的内容；如果value[3]为空，则要验证被包含的字段，直接从value[5]读取
                        {
                            smallText = value[5];
                        } else {
                            smallText = TD.getTestData(value[3], executeDevicename);
                        }
                        actualValue = "";
                        expectedValue = ""; // 默认情况下，假定校验成功，在期望结果与实际结果一样

                        checkResult = WarpingFunctions.verifyContainTest(bigText, smallText, "n");
                        if ((checkResult == "" || checkResult == "fail")) {
                            FileUtil.takeTakesScreenshot(driver);
                            resultMessage = FileUtil.filePath;
                            caseExecResult = "fail";
                            actualValue = smallText;
                        }
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        checkResult = "failure";
                        caseExecResult = "failure";
                        actualValue = "";
                    }
                    excel.writeCheckResult(value[4], resultMessage, checkResult, actualValue, expectedValue, executeDevicename);
                    break;
                case "检查点_xpath_字段不包含":
                    try {
                        String bigText, smallText;
                        Thread.sleep(2000);
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        if (value[2].equals("")) {
                            bigText = driver.findElement(By.xpath(value[1])).getAttribute("name"); // 获取content
                            // desc里的信息
                        } else {
                            bot = driver.findElements(By.xpath(value[1]));
                            bigText = bot.get(Integer.parseInt(value[2])).getAttribute("name");
                        }
                        if (value[3].equals("")) // value[3]中可以指定从数据文件读取的内容；如果value[3]为空，则要验证被包含的字段，直接从value[5]读取
                        {
                            smallText = value[5];
                        } else {
                            smallText = TD.getTestData(value[3], executeDevicename);
                        }

                        actualValue = "";
                        expectedValue = ""; // 默认情况下，假定校验成功，在期望结果与实际结果一样

                        checkResult = WarpingFunctions.verifyContainTest(bigText, smallText, "n");
                        if ((checkResult == "" || checkResult == "fail")) {
                            FileUtil.takeTakesScreenshot(driver);
                            resultMessage = FileUtil.filePath;
                            caseExecResult = "fail";
                            actualValue = smallText;
                        }
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        checkResult = "failure";
                        caseExecResult = "failure";
                    }
                    excel.writeCheckResult(value[4], resultMessage, checkResult, actualValue, expectedValue, executeDevicename);
                    break;
                case "检查点_id_内容为空":
                    try {
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);
                        WebElement wen;
                        if (value[2].equals("")) {
                            wen = driver.findElement(By.id(value[1]));
                            wen.equals("");
                            checkResult = "pass";
                        } else {
                            bot = driver.findElements(By.id(value[1]));
                            wen = bot.get(Integer.parseInt(value[2]));
                            wen.equals("");
                            checkResult = "pass";
                        }

                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        checkResult = "fail";
                        caseExecResult = "failure";
                    }
                    excel.writeCheckResult(value[4], resultMessage, checkResult, actualValue, expectedValue, executeDevicename);
                    break;
                case "检查点_id_数值":
                    try {
                        String bigText, smallText;
                        Thread.sleep(2000);
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        if (value[2].equals("")) {
                            bigText = driver.findElement(By.id(value[1])).getText();
                        } else {
                            bot = driver.findElements(By.id(value[1]));
                            bigText = bot.get(Integer.parseInt(value[2])).getText();
                        }

                        smallText = TD.computAndGetData(value[3], executeDevicename);
                        actualValue = smallText;
                        expectedValue = smallText; // 默认情况下，假定校验成功，在期望结果与实际结果一样

                        checkResult = WarpingFunctions.verifyContainTest(bigText, smallText, "y");
                        if ((checkResult == "" || checkResult == "fail")) {
                            FileUtil.takeTakesScreenshot(driver);
                            resultMessage = FileUtil.filePath;
                            caseExecResult = "fail";
                            actualValue = "";
                        }
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        checkResult = "failure";
                        caseExecResult = "failure";
                        actualValue = "";
                    }
                    excel.writeCheckResult(value[4], resultMessage, checkResult, actualValue, expectedValue, executeDevicename);
                    break;
                case "检查点_id_比较":
                    try {
                        int comment1, comment2, totalString;
                        Thread.sleep(2000);
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        comment1 = Integer.parseInt(TD.getTestData(value[1], executeDevicename));// 获取评论数前的值
                        comment2 = Integer.parseInt(TD.getTestData(value[3], executeDevicename));// 获取评论数后的值
                        totalString = comment1 + 1;

                        int actualValue = totalString;
                        int expectedValue = totalString; // 默认情况下，假定校验成功，在期望结果与实际结果一样
                        checkResult = WarpingFunctions.comment(comment2, totalString);
                        if ((checkResult == "" || checkResult == "fail")) {
                            FileUtil.takeTakesScreenshot(driver);
                            resultMessage = FileUtil.filePath;
                            caseExecResult = "fail";
                            actualValue = totalString;

                        }
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        checkResult = "failure";
                        caseExecResult = "failure";
                        actualValue = "";
                    }
                    excel.writeCheckResult(value[4], resultMessage, checkResult, actualValue, expectedValue, executeDevicename);
                    break;
                case "检查点_id_漫画岛蛋券余额":
                    try {
                        String bigText, smallText;
                        boolean ifDisplayed;
                        Thread.sleep(2000);
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        bigText = driver.findElement(By.id(value[1])).getText();
                        smallText = TD.getTestData(value[3], executeDevicename);
                        ifDisplayed = TD.getIfDispMHD(value[3], executeDevicename); // 漫画岛的漫画券与岛蛋余额在购买页面不一定显示，需要先判断是否需要显示
                        if (ifDisplayed == true) {
                            actualValue = smallText;
                            expectedValue = smallText; // 默认情况下，假定校验成功，在期望结果与实际结果一样
                            checkResult = WarpingFunctions.verifyContainTest(bigText, smallText, "y");
                            if (checkResult == "fail") {
                                FileUtil.takeTakesScreenshot(driver);
                                resultMessage = FileUtil.filePath;
                                caseExecResult = "fail";
                                actualValue = "";
                            }
                        } else // 如果不需要显示，直接返回正确的结论
                        {
                            actualValue = "不需要显示";
                            expectedValue = "不需要显示";
                            checkResult = "Pass";
                        }
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        checkResult = "failure";
                        caseExecResult = "failure";
                        actualValue = "";
                    }
                    excel.writeCheckResult(value[4], resultMessage, checkResult, actualValue, expectedValue, executeDevicename);
                    break;

                case "检查点_xpath_数值":
                    try {
                        String bigText, smallText;
                        Thread.sleep(2000);
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        if (value[2].equals("")) {
                            bigText = driver.findElement(By.xpath(value[1])).getAttribute("name"); // 获取content
                            // desc里的值
                        } else {
                            bot = driver.findElements(By.xpath(value[1]));
                            bigText = bot.get(Integer.parseInt(value[2])).getAttribute("name");
                        }

                        smallText = TD.computAndGetData(value[3], executeDevicename);
                        actualValue = smallText;
                        expectedValue = smallText; // 默认情况下，假定校验成功，在期望结果与实际结果一样

                        checkResult = WarpingFunctions.verifyContainTest(bigText, smallText, "y");
                        if ((checkResult == "" || checkResult == "fail")) {
                            FileUtil.takeTakesScreenshot(driver);
                            resultMessage = FileUtil.filePath;
                            caseExecResult = "fail";
                            actualValue = "";
                        }
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        checkResult = "failure";
                        caseExecResult = "failure";
                        actualValue = "";
                    }
                    excel.writeCheckResult(value[4], resultMessage, checkResult, actualValue, expectedValue, executeDevicename);
                    break;
                case "等待下载完成":
                    try {
                        int waitItem = 0;
                        String pageSourceString;
                        pageSourceString = driver.getPageSource(); // 获取页面pagesource
                        checkResult = "fail";
                        while (waitItem < 24 && checkResult == "fail") {
                            Thread.sleep(5000);
                            checkResult = WarpingFunctions.verifyContainTest(pageSourceString, value[3], "y");
                            waitItem = waitItem + 1;
                            pageSourceString = driver.getPageSource(); // 再次获取页面pagesource
                        }

                        if (checkResult == "fail") //
                        {
                            resultMessage = "下载时间超长";
                        }
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);
                    break;
                case "完成章节购买":
                    try {
                        if (appType.equals("ZS") || appType.equals("zs")) {
                            resultMessage = TD.buyAccoutingZS(executeDevicename);
                        } else if (appType.equals("KJ") || appType.equals("kj")) {
                            resultMessage = TD.buyAccoutingKJ(executeDevicename);
                        } else if (appType.equalsIgnoreCase("MHD")) {
                            resultMessage = TD.buyAccoutingMHD(executeDevicename);
                        } else {
                            System.out.print("未设定app类型");
                        }

                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }

                    excel.writeResult(value[4], resultMessage, executeDevicename);
                    break;

                case "检查点_class":
                    try {
                        WebDriverWait wait = new WebDriverWait(driver, maxWaitTime);// 最多等待时间由maxWaitTime指定
                        wait.until(ExpectedConditions.presenceOfElementLocated(By.className(value[1])));
                        actualValue = driver.findElement(By.className(value[1])).getText();
                        expectedValue = value[5];
                        checkResult = WarpingFunctions.verifyTest(actualValue, expectedValue);
                        if (checkResult == "fail") {
                            FileUtil.takeTakesScreenshot(driver);
                            resultMessage = FileUtil.filePath;
                            caseExecResult = "fail";
                        }
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        checkResult = "failure";
                        caseExecResult = "failure";
                    }
                    excel.writeCheckResult(value[4], resultMessage, checkResult, actualValue, expectedValue, executeDevicename);
                    break;
                case "对比_文本":
                    try {
                        String except, real;
                        String strcontrast = value[3];
                        String[] strs = strcontrast.split("，");
                        except = TD.getTestData(strs[0], executeDevicename);
                        real = TD.getTestData(strs[1], executeDevicename);
                        if (!except.equals(real)) {
                            resultMessage = "对比内容不一致";
                        }

                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);// 写入单个测试用例单个步骤的执行结果
                    break;
                case "等待时间":
                    try {
                        Thread.sleep(Integer.parseInt(value[1]));
                    } catch (Exception e) {
                        resultMessage = e.getMessage();
                        caseExecResult = "failure";
                    }
                    excel.writeResult(value[4], resultMessage, executeDevicename);
                    break;
                case "循环操作":
                    isLoopStep = true;
                    if (value[3] != null && !value[3].equals("")) {
                        loopCount = Integer.parseInt(value[3]);
                    }
                    resultMessage = "";
                    excel.writeResult(value[4], resultMessage, executeDevicename);
                    break;

                case "循环结束":
                    isLoopStep = false;
                    resultMessage = "";
                    excel.writeResult(value[4], resultMessage, executeDevicename);
                default:
                    excel.writeResult(value[0], resultMessage, executeDevicename);

            }


            if (resultMessage != null) {
                if (resultMessage.length() == 0) {
                    System.out.println("设备" + executeDevicename + ": 用例编号" + (i + 1) + "成功跑通"  + "\r");
                } else {
                    System.out.println("设备" + executeDevicename + ": 用例编号" + (i + 1) + "跑失败" + "用例：" + value[1] + "\r");
                    caseExecResult = "fail";
                }
            }
            //                    切换至原生
            if (value[6] != null && !value[6].equals("")) {
                if (value[6].equals("H5") || value[6].equals("h5")) {
                    driver.context("NATIVE_APP");
                    System.out.println("切换到nativeApp：" + nativeApp + "driver.getcontext" + driver.getContext());
                }
            }

        }
    }
}
