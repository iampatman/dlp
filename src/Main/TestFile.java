/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Random;
/**
 *
 * @author PC
 */
public class TestFile {
    private File inputFile;
    private byte[] outputData;
    private int positionChanged; // Vi tri byte bi thay doi = ramdom number
    TestFile(File input){
        this.inputFile = input;                     
    }
    boolean ChangeBadByte(Random rand){
        try{
            FileInputStream fi = new FileInputStream(inputFile);
            //System.out.println("File name:" + inputFile.getName() + " File size: " + inputFile.length() + " bytes");            
            positionChanged = rand.nextInt(fi.available());            
            if (positionChanged<0){
                System.out.println("Random number for selecting bad bytes is negative: " + positionChanged);
                positionChanged = Math.abs(positionChanged);
            }
            outputData = new byte[fi.available()];            
            fi.read(outputData);            
            fi.close();            
            byte temp = outputData[positionChanged];
            if (temp>1){
                temp/=2;
            }
            else {
                    temp+=1;
            }            
            //System.out.println("Original Byte: " + outputData[positionChanged] + " at pos " + positionChanged +" has been changed to " + temp);
            outputData[positionChanged] = temp;
            FileOutputStream fo = new FileOutputStream("TestFile");
            fo.flush();
            fo.write(outputData);
            fo.close();            
            return true;
        } catch (Exception e){
            System.out.println(e.toString());
            return false;
        }
    }
    public int getChangingPosition(){
        return positionChanged;
    }
}
