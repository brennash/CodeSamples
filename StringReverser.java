/****************************************************************************
 *
 * @author Shane Brennan
 * @date 1st Sept. 2013                    
 * @verion 1.0
 * 
 * Created under Creative Commons licence (CC BY-SA 3.0)
 * http://creativecommons.org/licenses/by-sa/3.0/
 *                                 
 ****************************************************************************/

public class StringReverser {

	public static String reverse(String str) {
		String heap = str.substring(str.length()-1, str.length());
		String string = str.substring(0, str.length()-1);
		return reverse(string, heap);
	}
	
	public static String reverse(String str, String heap) {
		if(str.length() == 1) {
			return heap.concat(str);
		}
		else {
			String hp = heap.concat(str.substring(str.length()-1, str.length()));
			String string = str.substring(0, str.length()-1);
			return reverse(string, hp);
		}
	}
		
	public static void main(String args[]) {
		String test = new StringReverser().reverse("zalando");
		System.out.println("Result: "+test);
	}
}

