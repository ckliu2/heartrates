package com.common;


import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.*;
import java.text.*;


public class Tools {

	private static HashMap hm = new HashMap();
	static DecimalFormat df = new DecimalFormat("##.000");
	static DecimalFormat df2 = new DecimalFormat("##.00");
	
	public Tools() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static boolean isEmptyString(String s) {
		if (s == null || s.trim().length() == 0)
			return true;
		return false;
	}

	public static java.util.Date convertToDate1(String date) {
		if (date != null && date.length() > 0) {
			java.text.SimpleDateFormat dmj = new java.text.SimpleDateFormat("yyyyMMdd");

			try {
				return dmj.parse(date);
			} catch (java.text.ParseException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public static java.util.Date convertToDate3(String date) {
		if (date != null && date.length() > 0) {
			java.text.SimpleDateFormat dmj = new java.text.SimpleDateFormat("yyyy-MM-dd");
			try {
				return dmj.parse(date);
			} catch (java.text.ParseException e) {
				//e.printStackTrace();
				//System.out.println("convertToDate3 error="+e.toString());
			}
		}
		return null;
	}

	public static java.util.Date convertToDate2(String date) {
		if (date != null && date.length() > 0) {
			java.text.SimpleDateFormat dmj = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
			try {
				return dmj.parse(date);
			} catch (java.text.ParseException e) {
				//System.out.println("convertToDate2 error="+e.toString());
			}
		}
		return null;
	}

	public static java.util.Date convertToDate(String date) {
		if (date != null && date.length() > 0) {
			java.text.SimpleDateFormat dmj = new java.text.SimpleDateFormat("yyyy/MM/dd");

			try {
				return dmj.parse(date);
			} catch (java.text.ParseException e) {
				return null;
			}
		}

		return null;
	}

	public static String todayString() {
		Date date = new Date();
		java.text.SimpleDateFormat dmj = new java.text.SimpleDateFormat("yyyyMMdd");
		String s = dmj.format(date);
		return s;
	}

	public static String dateToString(java.util.Date date) {
		if (date != null) {
			java.text.SimpleDateFormat dmj = new java.text.SimpleDateFormat("yyyy/MM/dd");
			String s = dmj.format(date);
			return s;
		}
		return null;
	}

	public static String dateToString1(java.util.Date date) {
		if (date != null) {
			java.text.SimpleDateFormat dmj = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
			String s = dmj.format(date);
			return s;
		}
		return null;
	}

	public static Timestamp getCurrentTimestamp() {
		return new Timestamp(System.currentTimeMillis());
	}

	public static String formatSimpleDate(java.util.Date d) {
		String s = "";
		if (d != null) {
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy/MM/dd");

			return sdf.format(d);
		}

		return null;
	}

	public static String firstCharToLowerCase(String pString_) {
		String sRetVal = new String();
		if (pString_ == null || pString_.length() == 0) {
			return (sRetVal);
		}
		sRetVal = pString_.substring(0, 1).toLowerCase() + pString_.substring(1, pString_.length());

		return (sRetVal);
	}

	public static String firstCharToUpperCase(String pString_) {
		String sRetVal = new String();
		if (pString_ == null || pString_.length() == 0) {
			return (sRetVal);
		}
		sRetVal = pString_.substring(0, 1).toUpperCase() + pString_.substring(1, pString_.length());

		return (sRetVal);
	}

	public static String getUniqueId() {
		Date ts = new Date();
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmssSSS.");
		String txt = fmt.format(ts);

		if (hm.get(txt) != null) {
			int a = ((Integer) hm.get(txt)).intValue() + 1;
			hm.put(txt, new Integer(a));
			return txt + a;
		} else {
			hm.clear();
			hm.put(txt, new Integer(0));
			return txt + "0";
		}
	}

	public static String getURLDecoder(String value) {
		String zhongguo = "";
		try {
			zhongguo = java.net.URLDecoder.decode(value, "utf-8");
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return zhongguo;
	}



	public static String md5(String m) {
		String pass = "";
		try {
			byte[] LaborBytes = m.getBytes();
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(LaborBytes);
			byte messageDigest[] = algorithm.digest();
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String hex = Integer.toHexString(0xFF & messageDigest[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			pass = hexString + "";
		} catch (Exception e) {
			System.out.println("MDF FAIL()");
		}
		return pass;
	}

	public static Date getToday() {
		Date t = new Date();
		return t;
	}

	public static java.util.Date convertToDateTime(String date) {
		if (date != null && date.length() > 0) { // 2010-11-24 02:44
			java.text.SimpleDateFormat dmj = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			try {
				return dmj.parse(date);
			} catch (java.text.ParseException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public static int getIntervalDays(Date begindate, Date enddate) {
		int day = 0;
		try {
			long millisecond = enddate.getTime() - begindate.getTime();
			day = (int) (millisecond / 24L / 60L / 60L / 1000L);
		} catch (Exception e) {
			System.out.println("getIntervalDays=" + e.toString());
		}
		return day;
	}

	public static String guid() {
		String guid = java.util.UUID.randomUUID().toString();
		return guid;
	}
	
	public static String methodURL(String u)
	{
		String page="";		
		int len=u.length();
		int times=u.lastIndexOf("/");
		page=u.substring(times+1, len-4);
		//System.out.println("len="+len);
		//System.out.println("times="+times);
		//System.out.println("page="+page);
		return page;
	}
	

	public static void main(String[] arg) {
		try {
			   //System.out.println(methodURL("/imageDB/erp/workflowSameProductJSON.jsp"));
			// System.out.println("�̥���2�^�T����=" + quoteUnit("�̥���", "�^�T����", 50));
			System.out.println(getPriceByUnitArea("�̥���", 111,222, 360));
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	public static int getPriceByUnitArea(String unit, double width, double height, double price) {
		int sum = 0;
		if (unit.equals("�~")) {
			int chi = (int) Math.ceil(width * height / 900.0);
			sum = (int) (chi * price);
		}

		if (unit.equals("�̥���")) {
			double d= new BigDecimal(width * height / 10000.0).setScale(2, BigDecimal.ROUND_CEILING).doubleValue();
			sum = (int)Math.ceil(price * d);
		}

		if (unit.equals("�^�T����")) {
			double w = (int) Math.ceil(width / 2.54);
			double h = (int) Math.ceil(height / 2.54);
			// System.out.println("w=" + w+"--h="+h);
			sum = (int) (w * h * price);
		}

		if (unit.equals("��")) {
			sum = (int) price;
		}

		if (unit.equals("��")) {
			sum = (int) price;
		}

		return sum;
	}

	public static double getUnitArea(String unit, double width, double height) {
		double sum = 0;
		if (unit.equals("�~")) {
			int chi = (int) Math.ceil(width * height / 900.0);
			sum = chi;
		}

		if (unit.equals("�̥���")) {
			sum = new BigDecimal(width * height / 10000.0).setScale(2, BigDecimal.ROUND_CEILING).doubleValue();
		}

		if (unit.equals("�^�T����")) {
			double w = (int) Math.ceil(width / 2.54);
			double h = (int) Math.ceil(height / 2.54);
			//System.out.println("w=" + w+"--h="+h);
			sum = (int) (w * h);
		}

		if (unit.equals("��")) {
			sum = 1;
		}

		if (unit.equals("��")) {
			sum = 1;
		}

		return sum;
	}

	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}


}
