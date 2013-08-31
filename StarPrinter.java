
    static int cross(int h) {
        
        int leading = 0; 
        int gap = (h-2);

        //Print the top part
        for(int i=0; i<(h/2); i++) {
            
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
            System.out.print("*\n")
        }
        
        //Print middle star
        for(int i=0; i<leading; i++)
        {
            System.out.print(" ");
        }
        System.out.print("*\n");
        leading--;
        gap = 1;
        
        //Print the bottom part
        for(int i=0; i<(h/2); i++) {
            
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
            System.out.print("*\n")
        }
    }


