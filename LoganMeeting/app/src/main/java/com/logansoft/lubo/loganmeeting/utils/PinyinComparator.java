package com.logansoft.lubo.loganmeeting.utils;

import java.util.Comparator;

public class PinyinComparator implements Comparator<String>{

	public int compare(String o1, String o2) {
		if (o1.equals("@")
				|| o2.equals("#")) {
			return -1;
		} else if (o1.equals("#")
				|| o2.equals("@")) {
			return 1;
		} else {
			return o1.compareTo(o2);
		}
	}
}
