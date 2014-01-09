package util;

import java.util.List;

public abstract class Print {
	
	public static <E> void printList(List<E> list){
		printList(list, "");
	}

	public static <E> void printList(List<E> list, String msg){
		System.out.println(msg);
		for (E e : list){
			System.out.println(e.toString());
		}
	}

}
