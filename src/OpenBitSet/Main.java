/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package OpenBitSet;

import com.google.common.hash.CityHashFunctions;
import java.util.Random;

/**
 *
 * @author PC
 */
public class Main {
    static Random rand = new Random(0);
    public static void main(String args[]){
        String str = "hello";
        byte[] input = str.getBytes();
        long[] rs = CityHashFunctions.cityHash128(input, 0, input.length);
        for (int i=0;i<rs.length;i++){
            System.out.println(rs[i]);
        }
        
    }
}
