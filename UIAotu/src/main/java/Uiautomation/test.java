package Uiautomation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

public class test {
	public static String str5;
	public static String str4;
	public static String str3;
	public static String str2;
	public static String str1;
	public static String str6;
	public static String str7;
	public static String str8;
	public static String str9;
	public static String str10;
	public static String str11;
	public static String str12;
	public static String port;
	public static String appPackage;
	public static String connectip;
	public static String linuxapkpath;
	public static String devicescount;
	public static String systemPorts;


	public static void testmethod() throws FileNotFoundException, IOException {
		Properties pps = new Properties();
		pps.load(new FileInputStream("F:\\autotest\\ASworks\\UIAotu\\Test-zsfree.properties"));
		Enumeration enum1 = pps.propertyNames();// 得到配置文件名
		while (enum1.hasMoreElements()) {
			String strKey = (String) enum1.nextElement();
			String strValue = pps.getProperty(strKey);
			if (strKey.equals("path5")) {
				str5 = strValue;
			}
			if (strKey.equals("path4")) {
				str4 = strValue;
			}
			if (strKey.equals("path3")) {
				str3 = strValue;
			}
			if (strKey.equals("path2")) {
				str2 = strValue;
			}
			if (strKey.equals("path1")) {
				str1 = strValue;
			}
			if (strKey.equals("path6")) {
				str6 = strValue;
			}
			if (strKey.equals("path7")) {
				str7 = strValue;
			}
			if (strKey.equals("path8")) {
				str8 = strValue;
			}
			if (strKey.equals("path9")) {
				str9 = strValue;
			}
			if (strKey.equals("path10")) {
				str10 = strValue;
			}
			if (strKey.equals("path11")) {
				str11 = strValue;
			}
			if (strKey.equals("path12")) {
				str12 = strValue;
			}
			if (strKey.equals("port")) {
				port = strValue;
			}
			if (strKey.equals("appPackage")) {
				appPackage = strValue;
			}
			if (strKey.equals("connectip")) {
				connectip = strValue;
			}
			if (strKey.equals("linuxapkpath")) {
				linuxapkpath = strValue;
			}
			if (strKey.equals("devicescount")) {
				devicescount = strValue;
			}
			if (strKey.equals("systemPort")) {
				systemPorts = strValue;
			}
		}
	}
}
