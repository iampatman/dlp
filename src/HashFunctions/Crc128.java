/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package HashFunctions;

import java.math.BigInteger;

/**
 *
 * @author Dinh
 */
public class Crc128 {
    
    private long result;
    private int g[] = new int[128]; //mảng chứa generate --- đa thức sinh
    private int k,msb;
    private String s1;
    
    public Crc128() {
    }
        
    public long implement(byte[] input){
        g[0] = 1;
        for(int i = 1; i < 128; i++){
            g[i] = 0;
        }
        
        int d[] = new int[128 + input.length*8];
        for(int i = 0; i < input.length; i++){
            s1 = String.format("%8s", Integer.toBinaryString(input[i] & 0xFF)).replace(' ', '0');
            k = 0;
            for(int j = i*8; j < i*8+8; j++){
                d[j] = Integer.parseInt(s1.substring(k,k+1));
                k++;
            }
        }
       
        for(int i = 0; i < 127; i++)
            d[input.length*8 + i]=0;
        
        int r[] = new int[128+input.length*8];
        
        for(int i=0; i < 128; i++)
            r[i]=d[i];
        
        int z[] = new int[128];
        
        for(int i = 0; i < 128; i++)
            z[i]=0;
        
        for(int i=0; i < input.length*8; i++){ 
            k = 0;
            msb = r[i];
            for(int j = i; j < 128+i; j++){ 
                if(msb == 0) 
                    r[j] = xor(r[j],z[k]);
                else  
                    r[j] = xor(r[j],g[k]);
                k++;
                }
                r[128+i]=d[128+i];
        }
        
        String s = "";
        for(int i = 0; i < input.length*8+128-1; i++){ 
             s += Integer.toString(d[i]);
        }        
        s = s.substring(0, 64);
        BigInteger temp = new BigInteger(s, 2);
        //String str = temp.toString();
        result = temp.longValue();
        
        return result;
    }
    
    public static int xor(int x,int y)
    { 
        if(x==y) 
            return(0);
        else  
            return(1);
    }
    
}
