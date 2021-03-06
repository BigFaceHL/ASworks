package Uiautomation;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TestData {
    int rowNumber, colNumber; //本次测试用例在数据文件中的行号与列号
    String phoneNumber;
    String getData; //返回的获取数据
    String appType = test.str12; //app类型，KJ，ZS与MHD分别对应开卷，追书与漫画岛3个项目
    boolean caseExist;
    Hashtable<String, String> cells = new Hashtable<String, String>(); //存放列名与列号对应关系的哈希表

    IdCardGenerator g = new IdCardGenerator();
    test test1 = new test();

    /**
     * 获取执行用例文件的所在行数
     *
     * @param caseName
     * @param devicesName
     */
    public void setNowNumber(String caseName, String devicesName) {
        String result2 = test1.str3;
        String excelPath = result2 + devicesName + "测试相关数据" + ".xlsx";
//        String excelPath = result2 + "测试相关数据" + ".xlsx";
        String value;
        File file = new File(excelPath);
        int totalRowNumber;
        caseExist = false; //判断用例在数据文件中是否存在对应行
        try {
            InputStream inputStream = new FileInputStream(file);
            Workbook workbook = null;
            try {
                workbook = new XSSFWorkbook(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }//解析xlsx格式

            Sheet sheet = workbook.getSheet("Sheet1");//第一个工作表
            totalRowNumber = sheet.getLastRowNum() - sheet.getFirstRowNum();//sheet1数据的行数

            for (int i = 1; i <= totalRowNumber; i++) {
                Row row = sheet.getRow(i);//获取行对象
                row.getCell(0);
                value = row.getCell(0).getStringCellValue();//测试用例名称
                if (value.trim().equals(caseName.trim())) {
                    rowNumber = i;
                    caseExist = true;
                    break;
                }

            }
            if (caseExist == false) {
                //System.out.println("数据文件中未对该用户设置对应行");
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * excel 读取数据
     *
     * @param dataType 列名
     * @param device   一个设备对应的一个文件
     * @return
     * @throws IOException
     */
    public String getTestData(String dataType, String device) throws IOException {

        try {
            test.testmethod();
            String result2 = test1.str3;
            String excelPath = result2 + device + "测试相关数据" + ".xlsx";
//		String excelPath=result2+"测试相关数据"+".xlsx";
            File file = new File(excelPath);

            InputStream inputStream = new FileInputStream(file);
            Workbook workbook = null;
            try {
                workbook = new XSSFWorkbook(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }//解析xlsx格式

            Sheet sheet = workbook.getSheet("Sheet1");//第一个工作表
            //setColNumber(dataType);//设置要获取的数据所在的列号
            Row row = sheet.getRow(rowNumber);//获取行号
            colNumber = getColNumber(dataType);

            if (row.getCell(colNumber) == null) {
                getData = "";
            } else {
                row.getCell(colNumber).setCellType(Cell.CELL_TYPE_STRING);
                getData = row.getCell(colNumber).getStringCellValue();//
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return getData;
    }

    /**
     * 对比数据
     *
     * @param dataType
     * @param device
     * @return
     * @throws IOException
     */
    public String computAndGetData(String dataType, String device) throws IOException {
        String acountedValue = "";
        int chongzb_kj, jianglib_kj;
        String discountRate = "", totalOriginalCost;
        switch (dataType) {
            case "折扣数量":
                totalOriginalCost = getTestData("章节总原价", device);
                discountRate = getTestData("最终折扣率", device);
                acountedValue = computeDiscountAmount(totalOriginalCost, discountRate);
                break;
            case "最终折扣率":
                String specialUserRate = getTestData("特权用户折扣率", device);
                if (specialUserRate.equals("")) {
                    specialUserRate = "8.0";//如果数据文件中没有配置特权用户的折扣率，默认将其设为8.0折
                }

                String nomalUserRate = getTestData("一般折扣率", device);
                acountedValue = computeDiscountRate(specialUserRate, nomalUserRate, device);
                break;
            case "章节总折后价":

                totalOriginalCost = getTestData("章节总原价", device);
                if (appType.equalsIgnoreCase("KJ")) //对于开卷项目来说，如果为会员账户，且数据文件中的最终折扣率未配置，则将其设为默认的8折，且回写到数据文件中
                {
                    if (getTestData("是否特权用户", device).equals("未开通")) //非会员账号，无折扣
                    {
                        discountRate = "10.0";
                        setTestData("最终折扣率", "无折扣", device);
                    } else if (getTestData("最终折扣率", device).equals("")) {
                        discountRate = "8.0";
                        setTestData("最终折扣率", discountRate, device);
                    } else //如果账户折扣率已经做了配置，取其配置的值
                    {
                        discountRate = getTestData("最终折扣率", device);
                    }
                } else {
                    discountRate = getTestData("最终折扣率", device);
                }

                acountedValue = computeActualCost(totalOriginalCost, discountRate);
                break;
            case "开卷_总书币":

                chongzb_kj = getNonQianfValue(getTestData("开卷_充值书币", device)); //从数据文件中"开卷_充值书币"列取出信息，并去掉千分位
                jianglib_kj = getNonQianfValue(getTestData("开卷_奖励书币", device));
                acountedValue = getQianfValue(chongzb_kj + jianglib_kj); //将充值书币于奖励书币相加，并转换位千分位的形式
                break;

            default:
                break;
        }

        try {
            test.testmethod();
            String result2 = test1.str3;
            String excelPath = result2 + device + "测试相关数据" + ".xlsx";
//		String excelPath=result2+"测试相关数据"+".xlsx";
            File file = new File(excelPath);
            InputStream fis = new FileInputStream(file);
            OPCPackage opcPackage = OPCPackage.open(fis);
            Workbook wb = new XSSFWorkbook(opcPackage);
            Sheet sheet = wb.getSheetAt(0);
            colNumber = getColNumber(dataType);
            sheet.setColumnWidth(colNumber, 15 * 256);//讲第特定列宽度设为10个字符宽度
            Row row = (Row) sheet.getRow(rowNumber);
            Cell cell0 = null;
            cell0 = row.createCell(colNumber);
            cell0.setCellType(Cell.CELL_TYPE_STRING);

            FileOutputStream out = new FileOutputStream(excelPath);
            cell0.setCellValue(acountedValue);
            out.flush();
            wb.write(out);
            out.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return acountedValue;
    }

    /**
     * 计算购买章节时总的折扣数量
     *
     * @param totalOriginalCost
     * @param discountRate
     * @return
     * @throws IOException
     */
    public String computeDiscountAmount(String totalOriginalCost, String discountRate) throws IOException {
        String acountedValue = "";
        double a = (Double.parseDouble(discountRate)) * 0.1; //折扣数量 = 章节总原价*（1-最终折扣率%）
        double b = Double.parseDouble(totalOriginalCost);
        double c = b * (1 - a);

        acountedValue = Long.toString(Math.round(c)); //c计算出来取整时，原来值可能有误，用四舍五入的方式做一下处理
        return acountedValue;
    }

    /**
     * 计算购买章节时最后的折扣率
     *
     * @param specialUserRate
     * @param nomalUserRate
     * @param device
     * @return
     * @throws IOException
     */
    public String computeDiscountRate(String specialUserRate, String nomalUserRate, String device) throws IOException {
        String acountedValue = "", ifSpectialUserText = "", filterdNURate = "";
        float a, b;
        if (appType.equalsIgnoreCase("ZS")) {
            ifSpectialUserText = "到期"; //追书项目下，特权用户的显示信息为“已包月”
        }

        if (getTestData("是否特权用户", device).trim().contains(ifSpectialUserText)) //如果为特权用户，最后的折扣率 = 一般用户折扣率*特权用户折扣率
        {
            a = (float) ((Float.parseFloat(specialUserRate)) * 0.1);
        } else //如果不是特权用户，最后的折扣率 = 一般用户折扣率，即特权用户折扣率为1
        {
            a = 1.0f;
        }

        if (nomalUserRate.equals("无折扣")) {
            b = 1;
        } else {
            filterdNURate = WarpingFunctions.getFiltedText(nomalUserRate, "/折"); //从页面上的信息包含"*折"的字样，需要去掉“折”再进行计算
            b = (float) ((Float.parseFloat(filterdNURate)) * 0.1);
        }
        float c = a * b * 10;
        DecimalFormat df = new DecimalFormat("#.0"); //返回结果只保留一位小数
        df.format(c);
        acountedValue = Float.toString(c);
        return acountedValue;
    }

    /**
     * 计算折扣数量
     *
     * @param totalOrigCost 总价
     * @param discountRate  折扣
     * @return
     * @throws IOException
     */
    public String computeActualCost(String totalOrigCost, String discountRate) throws IOException {
        String acountedValue = "";
        double a = (Double.parseDouble(discountRate)) * 0.1; //折扣数量 = 章节总原价*（1-最终折扣率%）
        double b = Double.parseDouble((totalOrigCost));
        double c = b * a;
        acountedValue = Long.toString(Math.round(c)); //c计算出来取整时，原来值可能有误，用四舍五入的方式做一下处理
        return acountedValue;
    }

    /**
     * 将所有的列号与列名的对应关系，放入一个哈希表，方便后续代码可以通过列名直接找到对应的列号信息
     *
     * @param device 对应设备的文件
     * @throws IOException
     */
    public void getColFromDataFiles(String device) throws IOException {

        try {
            test.testmethod();
            String result2 = test1.str3;
            String excelPath = result2 + device + "测试相关数据" + ".xlsx";
//		String excelPath=result2+"测试相关数据"+".xlsx";
            String colName;
            File file = new File(excelPath);

            InputStream inputStream = new FileInputStream(file);
            Workbook workbook = null;
            try {
                workbook = new XSSFWorkbook(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }//解析xlsx格式

            Sheet sheet = workbook.getSheet("Sheet1");//第一个工作表
            Row row = sheet.getRow(0);//取第一行的数据，即列名信息
            int columnCount = row.getLastCellNum();
            for (int i = 1; i <= columnCount; i++) {
                if (row.getCell(i) == null) {
                    colName = "";
                    continue;
                } else {
                    row.getCell(i).setCellType(Cell.CELL_TYPE_STRING);
                    colName = row.getCell(i).getStringCellValue();
                }
                cells.put(colName.trim(), Integer.toString(i));

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据传入的列名信息，从哈希表中取出对应的列号
     *
     * @param dataType
     * @return
     */
    public int getColNumber(String dataType) {

        if (cells.containsKey(dataType)) {
            colNumber = Integer.parseInt(cells.get(dataType));
        } else {
            System.out.println("哈希表中没有该列名的信息");
        }
        return colNumber;
    }

    /**
     * 获取去除千分位格式后的数值，传入为带千分位格式的字符串，返回为不带千分位格式的int型数值
     *
     * @param origValue
     * @return
     */
    public int getNonQianfValue(String origValue) {
        int returnValue = 0;
        try {
            returnValue = new DecimalFormat().parse(origValue).intValue();
        } catch (ParseException e) {
            System.out.println("没有含千分位信息的信息传入");
            e.printStackTrace();
        }

        return returnValue;
    }

    /**
     * 获取带千分位格式后的数值，传入为不带千分位格式的数值，返回为带千分位格式的字符串
     *
     * @param origValue
     * @return
     */
    public String getQianfValue(int origValue) {
        String returnValue = DecimalFormat.getNumberInstance().format(origValue);
        return returnValue;
    }

    public void SetNewPhone() {
        try {
            test.testmethod();
            String result2 = test1.str3;
            String excelPath = result2 + "测试相关数据" + ".xlsx";
            File file = new File(excelPath);
            InputStream fis = new FileInputStream(file);
            OPCPackage opcPackage = OPCPackage.open(fis);
            Workbook wb = new XSSFWorkbook(opcPackage);
            Sheet sheet = wb.getSheetAt(0);
            sheet.setColumnWidth(1, 15 * 256);//讲第1列宽度设为30个字符宽度
            Row row = (Row) sheet.getRow(rowNumber);
            row.getCell(1).setCellType(Cell.CELL_TYPE_STRING);
            phoneNumber = row.getCell(1).getStringCellValue();//获取原先电话号码

            FileOutputStream out = new FileOutputStream(excelPath);
            Cell cell0 = row.getCell(1);//setCell(colNumber, Cell.CELL_TYPE_STRING);

            Long a = Long.parseLong(phoneNumber);
            Long b = a + 1; //原先电话号码+1，准备下一次用
            cell0.setCellValue(b.toString());
            out.flush();
            wb.write(out);
            out.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * 传入要检查的余额类型，根据app的显示规则，返回要不要在页面上显示该字段信息的判断值
     *
     * @param checkType
     * @param device
     * @return
     */
    boolean getIfDispMHD(String checkType, String device) {
        boolean ifDisplayed = true;
        String ifMhqSupported;
        int amountMHQ, amountDD, zongZhj;

        try {
            ifMhqSupported = getTestData("漫画岛_是否支持漫画券", device);
            amountMHQ = Integer.parseInt(getTestData("漫画岛_漫画券余额", device));
            amountDD = Integer.parseInt(getTestData("漫画岛_岛蛋余额", device));
            zongZhj = Integer.parseInt(getTestData("章节总折后价", device));
            if (checkType.trim().equals("漫画岛_岛蛋余额")) {
                if (ifMhqSupported.equalsIgnoreCase("Y") && (amountMHQ >= zongZhj)) //如果支持漫画券支付，且剩余漫画券余额大于等于总的折后价格，页面上不显示岛蛋余额
                {
                    ifDisplayed = false;
                }
            } else if (checkType.trim().equals("漫画岛_漫画券余额")) {
                if (ifMhqSupported.equalsIgnoreCase("Y") && (amountMHQ < zongZhj)) //如果支持漫画券支付，且剩余漫画券余额小于总的折后价格，页面上不显示漫话犬余额
                {
                    ifDisplayed = false;
                }

            } else {
                System.out.print("未传入校验类型");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ifDisplayed;
    }

    /**
     * 向Excel插入数据
     *
     * @param testDataType 列名字
     * @param readedText   内容
     * @param devices      一个设备对应的一个文件
     * @return
     */
    public String setTestData(String testDataType, String readedText, String devices) {

        try {
            //test.testmethod();
            String result2 = test1.str3;
            String excelPath = result2 + devices + "测试相关数据" + ".xlsx";
//		String excelPath=result2+"测试相关数据"+".xlsx";
            File file = new File(excelPath);
            InputStream fis = new FileInputStream(file);
            OPCPackage opcPackage = OPCPackage.open(fis);
            Workbook wb = new XSSFWorkbook(opcPackage);
            Sheet sheet = wb.getSheetAt(0);
            colNumber = getColNumber(testDataType);
            sheet.setColumnWidth(colNumber, 20 * 256);//讲第特定列宽度设为10个字符宽度
            Row row = (Row) sheet.getRow(rowNumber);
            Cell cell0 = null;

            cell0 = row.createCell(colNumber);
            cell0.setCellType(Cell.CELL_TYPE_STRING);

            //Cell cell0 = row.getCell(colNumber);
            FileOutputStream out = new FileOutputStream(excelPath);
            cell0.setCellValue(readedText);
            out.flush();
            wb.write(out);
            out.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return readedText;
    }

    /**
     * 追书购买章节计算，并将计算结果写入数据文件
     *
     * @param device 一个设备对应的一个文件
     * @return
     */
    public String buyAccoutingZS(String device) {

        String resultMessage = "", ifShudouSupported;
        int origShubiAmount, origShuQuanAmount, origShudouAmount, shifuAmount; //原有的书币余额，书券余额，书豆余额，购买的实付数额
        int newShubiAmount, newShuQuanAmount, newShudouAmount; //购买后端哦书币余额，书券余额，书豆余额
        int currentZhangJieHao, nextZhangJieHao;//本次要购买的起始章节号，下次要购买的其实章节号
        String zhangjieShu; //本次购买的章节数
        try {
            Pattern pNotNum = Pattern.compile("[^0-9]");

            String shubi = getTestData("追书_书币余额", device);
            String shuquan = getTestData("追书_书券余额", device);
            String totalPrice = getTestData("章节总折后价", device);

            Matcher shubiInteger = pNotNum.matcher(shubi);
            Matcher shuquanInteger = pNotNum.matcher(shuquan);
            Matcher totalPriceInteger = pNotNum.matcher(totalPrice);

            origShubiAmount = Integer.parseInt(shubiInteger.replaceAll("").trim());
            origShuQuanAmount = Integer.parseInt(shuquanInteger.replaceAll("").trim());
            shifuAmount = Integer.parseInt(totalPriceInteger.replaceAll(""));

            if (origShuQuanAmount >= shifuAmount) //如果书券金额大于实际购买金额，直接扣减书券
            {
                newShuQuanAmount = origShuQuanAmount - shifuAmount;
                setTestData("追书_书券余额", Integer.toString(newShuQuanAmount), device);
            } else if ((origShuQuanAmount < shifuAmount) && ((origShuQuanAmount + origShubiAmount) >= shifuAmount)) //书券余额<如果实际购买金额<=（书券余额+书币余额），先将书券扣减到0，再扣减书币
            {
                newShuQuanAmount = 0;
                setTestData("追书_书券余额", "0", device);
                newShubiAmount = origShuQuanAmount + origShubiAmount - shifuAmount;
                setTestData("追书_书币余额", Integer.toString(newShubiAmount), device);
            } else {
                resultMessage = "余额不足购买";
            }
            String buyChapter =getTestData("购买章节", device);
            Matcher buyChapterMatcher = pNotNum.matcher(buyChapter);

            //计算数据文件中下一个需要购买的章节号
            currentZhangJieHao = Integer.parseInt(buyChapterMatcher.replaceAll(""));
            zhangjieShu = getTestData("章节数量", device);
            if (zhangjieShu.equals("本章")) {
                nextZhangJieHao = currentZhangJieHao + 1;
            } else {
                nextZhangJieHao = currentZhangJieHao + Integer.parseInt(WarpingFunctions.getFiltedText(zhangjieShu, "后/章"));
            }
            setTestData("购买章节", Integer.toString(nextZhangJieHao), device);

        } catch (Exception e) {
            e.printStackTrace();
            resultMessage = e.getMessage();
        }
        return resultMessage;
    }

    /**
     * 开卷购买章节计算，并将计算结果写入数据文件
     *
     * @param device 一个设备对应的一个文件
     * @return
     */
    public String buyAccoutingKJ(String device) {

        String resultMessage = "", ifJiangliSupported;

        int origChongzAmount, origJiangliAmount, shifuAmount; //原有的充值书币余额，奖励书币余额，购买的实付数额
        int newChongzAmount = 0, newJiangliAmount = 0, newTotalAmount; //购买后的充值书币、奖励书币、总书币余额
        int currentZhangJieHao, nextZhangJieHao;//本次要购买的起始章节号，下次要购买的其实章节号
        String zhangjieShu; //本次购买的章节数
        try {
            ifJiangliSupported = getTestData("开卷_奖励书币是否可用", device);
            origChongzAmount = getNonQianfValue(getTestData("开卷_充值书币", device)); //开卷项目中要对充值书币/奖励书币/总书币的存取进行千分位的处理，后同
            origJiangliAmount = getNonQianfValue(getTestData("开卷_奖励书币", device));
            shifuAmount = Integer.parseInt(getTestData("章节总折后价", device));
            if (ifJiangliSupported.equals("奖励书币余额不可用")) //如果奖励书币不可用，则通过充值书币余额进行扣减
            {
                if (origChongzAmount >= shifuAmount) //如果书券金额大于实际购买金额，直接扣减书券
                {
                    newChongzAmount = origChongzAmount - shifuAmount;
                    setTestData("开卷_充值书币", getQianfValue(newChongzAmount), device);
                    newTotalAmount = newChongzAmount + origJiangliAmount;
                    setTestData("开卷_总书币", getQianfValue(newTotalAmount), device);
                } else {
                    resultMessage = "余额不足购买";
                }
            } else //如果支持奖励书币，则通过奖励书币余额与充值书币书币余额进行扣减
            {
                if (origJiangliAmount >= shifuAmount) {
                    newJiangliAmount = origJiangliAmount - shifuAmount;
                    setTestData("开卷_奖励书币", getQianfValue(newJiangliAmount), device);
                    newTotalAmount = origChongzAmount + newJiangliAmount;
                    setTestData("开卷_总书币", getQianfValue(newTotalAmount), device);
                } else if ((origJiangliAmount + origChongzAmount) >= shifuAmount) {
                    newJiangliAmount = 0;
                    setTestData("开卷_奖励书币", "0", device);
                    newChongzAmount = origJiangliAmount + origChongzAmount - shifuAmount;
                    setTestData("开卷_充值书币", getQianfValue(newChongzAmount), device);
                    newTotalAmount = newChongzAmount + newJiangliAmount;
                    setTestData("开卷_总书币", getQianfValue(newTotalAmount), device);
                } else {
                    resultMessage = "余额不足购买";
                }
            }

            //计算数据文件中下一个需要购买的章节号
            currentZhangJieHao = Integer.parseInt(getTestData("购买章节", device));
            zhangjieShu = getTestData("章节数量", device);
            if (zhangjieShu.equals("单章购买")) {
                nextZhangJieHao = currentZhangJieHao + 1;
            } else {
                nextZhangJieHao = currentZhangJieHao + Integer.parseInt(WarpingFunctions.getFiltedText(zhangjieShu, "购买/章"));
            }
            setTestData("购买章节", Integer.toString(nextZhangJieHao), device);

        } catch (Exception e) {
            resultMessage = e.getMessage();
        }
        return resultMessage;
    }

    /**
     * 漫画岛购买章节计算，并将计算结果写入数据文件
     *
     * @param device 一个设备对应的一个文件
     * @return
     */
    public String buyAccoutingMHD(String device) {

        String resultMessage = "", ifMHQSupported;
        int origDDAmount, origMHQAmount, shifuAmount; //原有的漫画券余额，岛蛋余额，购买的实付数额
        int newDDAmount = 0, newMHQAmount = 0; //购买后的岛蛋、漫画券余额
        int currentZhangJieHao, nextZhangJieHao = 0;//本次要购买的起始章节号，下次要购买的其实章节号
        String zhangjieShu; //本次购买的章节数
        try {
            ifMHQSupported = getTestData("漫画岛_是否支持漫画券", device);
            origDDAmount = Integer.parseInt(getTestData("漫画岛_岛蛋余额", device));
            origMHQAmount = Integer.parseInt(getTestData("漫画岛_漫画券余额", device));
            shifuAmount = Integer.parseInt(getTestData("章节总折后价", device));
            if (ifMHQSupported.trim().equals("N")) //数据文件中指明漫画券不可用，则通过岛蛋余额进行扣减
            {
                if (origDDAmount >= shifuAmount) //如果岛蛋金额大于实际购买金额，直接扣减岛蛋余额
                {
                    newDDAmount = origDDAmount - shifuAmount;
                    setTestData("漫画岛_岛蛋余额", Integer.toString(newDDAmount), device);
                } else {
                    resultMessage = "余额不足购买";
                }
            } else//如果数据文件中不指明漫画券不可用，则默认支持漫画券，则通过漫画券余额与岛蛋余额进行扣减
            {
                if (origMHQAmount >= shifuAmount) {
                    newMHQAmount = origMHQAmount - shifuAmount;
                    setTestData("漫画岛_漫画券余额", Integer.toString(newMHQAmount), device);
                } else if ((origMHQAmount + origDDAmount) >= shifuAmount) {
                    newMHQAmount = 0;
                    setTestData("漫画岛_漫画券余额", "0", device);
                    newDDAmount = origMHQAmount + origDDAmount - shifuAmount;
                    setTestData("漫画岛_岛蛋余额", Integer.toString(newDDAmount), device);
                } else {
                    resultMessage = "余额不足购买";
                }
            }

            //计算数据文件中下一个需要购买的章节号
            currentZhangJieHao = Integer.parseInt(getTestData("购买章节", device));
            zhangjieShu = getTestData("章节数量", device);
            if (zhangjieShu.contains("本话")) {
                nextZhangJieHao = currentZhangJieHao + 1;
            } else {
                nextZhangJieHao = currentZhangJieHao + Integer.parseInt(WarpingFunctions.getFiltedText(zhangjieShu, "后/话"));
            }
            setTestData("购买章节", Integer.toString(nextZhangJieHao), device);

        } catch (Exception e) {
            resultMessage = e.getMessage();
        }
        return resultMessage;
    }
}

