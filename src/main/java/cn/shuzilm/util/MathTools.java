package cn.shuzilm.util;

public class MathTools {
	
	/**
	 * @param x
	 * @param y
	 * @return
	 */
	public static int division(int x, int y){ 
		if(y == 0)
		return x;
		else
		return division(y,x%y);
	}
}
