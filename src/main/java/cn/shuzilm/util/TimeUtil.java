package cn.shuzilm.util;

public class TimeUtil {

	public static int weekDayToNum(String weekDay) {
		switch (weekDay) {
		case "Monday":
		case "星期一":
			return 0;
		case "Tuesday":
		case "星期二":
			return 1;
		case "Wednesday":
		case "星期三":
			return 2;
		case "Thursday":
		case "星期四":
			return 3;
		case "Friday":
		case "星期五":
			return 4;
		case "Saturday":
		case "星期六":
			return 5;
		case "Sunday":
		case "星期日":
			return 6;
		}
		return -1;
	}
	
	public static void main(String[] args) {
		System.out.println(weekDayToNum("Wednesday"));
		System.out.println(weekDayToNum("星期三"));
	}
}
