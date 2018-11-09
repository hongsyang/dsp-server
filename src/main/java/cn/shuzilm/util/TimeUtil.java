package cn.shuzilm.util;

public class TimeUtil {

	public static int weekDayToNum(String weekDay) {
		switch (weekDay) {
		case "Monday":
			return 0;
		case "Tuesday":
			return 1;
		case "Wednesday":
			return 2;
		case "Thursday":
			return 3;
		case "Friday":
			return 4;
		case "Saturday":
			return 5;
		case "Sunday":
			return 6;
		}
		return -1;
	}
}
