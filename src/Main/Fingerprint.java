/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;
import HashFunctions.*;
import com.google.common.hash.HashFunction;
import com.google.common.hash.*;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.zip.CRC32;

/**
 *
 * @author PC
 */

public class Fingerprint {
    
    private byte[] input;
    private long result;
    private long seed;
    Fingerprint(byte[] in,long seed){
        this.input = in;
        this.seed = seed;
    }
    public long CalcFingerprint(String hashname){   
        
        //CRC crc = new CRC(32, 0x04C11DB7);
       // CRC32 crc1 = new CRC32();
//        crc1.update(input);
        HashFunction hf;
        switch (hashname){
            case "Murmur3":
                hf = com.google.common.hash.Hashing.murmur3_128();              
                return (long)hf.hashBytes(this.input).asLong();
            case "SHA1":
                hf = com.google.common.hash.Hashing.sha1();
                return (long)hf.hashBytes(this.input).asLong();                
            case "MD5":
                hf = com.google.common.hash.Hashing.md5();
                return (long)hf.hashBytes(this.input).asLong();
            case "CRC128":
                //Crc128 crc128 = new Crc128();                
                return CRC128(input);
            case "FNVHash":
                return FNVHash.hash64(input);                
            case "JenkinsHash":
                return JenkinsHash.hash64(input, 0);         
            case "CityHash":                  
                long[] rs = CityHashFunctions.cityHash128(input, 0, input.length);                                              
                return Math.abs(rs[0]);               
        }
        return 0;
     }
    public long getResult(){
        return result;
    }    
    private int MyCRC32(byte[] a,int start,int end){
        byte[] temp;
        temp = Arrays.copyOfRange(a, start, end);              
        CRC32 crc32 = new CRC32();
        crc32.update(temp);        
        return (int)crc32.getValue();
    }      
    public long CRC128(byte[] input){
        int length = input.length;
        int segment;        
        //Random rand = new Random(seed);
        try{
            int part1,part2,part3,part4;

            if (length%4!=0){            
                int ipad = 4 - (length%4);            
                input = Arrays.copyOf(input, length+ipad);
            }    
            length = input.length;
            segment=length/4;          
            part1 = MyCRC32(input,0,segment-1);
            part2 = MyCRC32(input,segment,2*segment-1);
            part3 = MyCRC32(input,2*segment,3*segment-1);
            part4 = MyCRC32(input,3*segment,length-1);
            String seedValueStr = String.valueOf(this.seed);
            for (int i=0;i<10;i++){
                seedValueStr += String.valueOf(this.seed);
            }
            BigInteger seedValue = new BigInteger(seedValueStr);

            String str = Integer.toHexString(part1)+Integer.toHexString(part2)
                                +Integer.toHexString(part3)+Integer.toHexString(part4);
            BigInteger bigint = new BigInteger(str, 16);              
            bigint = bigint.xor(seedValue);
            return Math.abs(bigint.longValue());
        } catch (Exception e){
            System.out.println(e.toString());
        }
        return 0;
    }
}
