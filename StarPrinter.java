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

public class Cross {

    static void cross(int n) {
        
        int leading = 0; 
        int gap = (n-2);

        //Print the top part
        for(int i=0; i<(n/2); i++) {
            
            //Print leading whitespace
            for(int j=0; j<leading; j++) {
                System.out.print(" ");
            }
            leading++;
            
            //Print first star
            System.out.print("*");
            
            for(int k=0; k<gap; k++) {
                System.out.print(" ");
            }
            gap -= 2;
            
            //Print trailing star
            System.out.print("*\n");
        }
        
        //Print middle star
        for(int i=0; i<leading; i++) {
            System.out.print(" ");
        }
        System.out.print("*\n");
        leading--;
        gap = 1;
        
        //Print the bottom part
        for(int i=0; i<(n/2); i++) {
            
            //Print leading whitespace
            for(int j=0; j<leading; j++) {
                System.out.print(" ");
            }
            leading--;
            
            //Print first star
            System.out.print("*");
            
            for(int k=0; k<gap; k++) {
                System.out.print(" ");
            }
            gap += 2;
            
            //Print trailing star
            System.out.print("*\n");
        }
    }
    
    public static void main(String[] args) {
        Cross.cross(Integer.parseInt(args[0]));
    }
}
