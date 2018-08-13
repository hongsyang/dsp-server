package cn.shuzilm.util;

public class TimeUtil {

	public static int weekDayToNum(String weekDay) {
		switch (weekDay) {
		case "星期一":
			return 0;
		case "星期二":
			return 1;
		case "星期三":
			return 2;
		case "星期四":
			return 3;
		case "星期五":
			return 4;
		case "星期六":
			return 5;
		case "星期日":
			return 6;
		}
		return -1;
	}
}
