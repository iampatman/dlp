/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Random;
/**
 *
 * @author PC
 */
public class FileCollector {
    private long n;
    private String Path;
    private int detectionLag;
    private long lmax; //Gia tri file dai nhat tinh theo bytes
    private List<File> fileList;
    private int[] N;
    public List<Integer> checkingPoint;
    private long nFingerPrint;
    private long count=0;
    private float falsepositive=0;
    private BloomFilter<String>[] bloomFilters;
    public int getNumberOfFiles(){
        return fileList.size();
    }
    public File getFile(int Index){
        return fileList.get(Index);
    }
    private void TakeList(List<File> filelist, List<File> folderlist, File dir){
        File[] list;
        list = dir.listFiles();
        for (File filetemp: list){
            if (filetemp.isDirectory()){
                folderlist.add(filetemp);
                TakeList(filelist,folderlist,filetemp);                
            }
            else {
                filelist.add(filetemp);
                if (filetemp.length()>lmax){
                    lmax = filetemp.length();
                }
            }                
        }
    }
    FileCollector(File file,int Delta,float fp){        
        //this.Path  = path; //The Path to location containing files.
        this.detectionLag = Delta; //Expected Detection lag 
        this.falsepositive = fp;
        this.lmax = 0;
        List<File> folderList = new ArrayList<File>();
        fileList = new ArrayList<File>();        
        checkingPoint = new ArrayList<Integer>();
        File f = file;
        fileList.clear();
        TakeList(fileList, folderList, f);       
        
    }    
    public void Implement(){
        try{
            long starttime,endtime,time;
            starttime = new Date().getTime();
            Sort();
            endtime = new Date().getTime();
            time = endtime-starttime;
            System.out.println("Sort completed");
            System.out.println("Time: " + 1.0*time/1000+ " seconds");
                        
            starttime = new Date().getTime();
            InitializeN();
            endtime = new Date().getTime();
            time = endtime-starttime;
            System.out.println("Initialized N completed");
            System.out.println("Time: " + 1.0*time/1000+" seconds");            
            
            //in();
            starttime = new Date().getTime();
            calculateCheckingPoints();
            endtime = new Date().getTime();
            time = endtime-starttime;
            System.out.println("Calculated all the checking points ");
            System.out.println("Time: " + 1.0*time/1000 + " seconds");
            //this.nFingerPrint = Integer.MAX_VALUE-1;
            
        } catch (Exception e){
            System.out.println(e.toString());
        }
    }
    private void Sort(){
        //Sap xep list file theo dung luong tang dan; 
        File files[] = new File[fileList.size()];
        files = fileList.toArray(files);
        try{
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare( File a, File b ) {
            // do your comparison here returning -1 if a is before b, 0 if same, 1 if a is after b
                if (a.length() < b.length()) {
                    return -1;
                }
                else if (a.length()>b.length()) {
                    return 1;
                }
                return 0;
            }
        } );
        } catch (Exception e){
            System.out.println(e.toString());
        }
        int numberoffile = fileList.size();
        fileList.clear();
        for (int i=0;i<numberoffile;i++){
            fileList.add(files[i]);
        }               
        
    }
    private void InitializeN(){
        int count = 0;
        N = new int[(int)lmax+1];
        int j,i;
        i=fileList.size()-1; //vi tri trong ds file
        j=(int)lmax; // vi tri trong mang N
        while (j>=0){
           // System.out.println(j + " " + count + " " + i);
            while ((i>=0)&&(fileList.get(i).length() == j)){
                count++;
                i--;    
                //System.out.println(count+ " "+ i);
            }       
            N[j] = count;
            j--;                
        }        
    }          
    public long getNumberOfFingerprint(){
        return nFingerPrint;
    }
    public void setNumberOfFingerprint(long value){
        this.nFingerPrint = value;
    }
    public int CalcB(int t,double p,boolean forAllocation){
        int rs=0;
        if (forAllocation==false){
            rs = (int)(-1.0*N[t]/Math.log10(p));        
        }
        else {            
            //rs = (int)(N[t]*Math.log(1.0f/p)/Math.pow(Math.log(2.0), 2.0));
            rs = (int)(N[t]*Math.log10(1.0f/p)/Math.pow(Math.log10(2.0), 2.0));
        }
        return rs;        
    }
    public void calculateCheckingPoints(){
        int f[],p[],b[];
        int delta = detectionLag,LMAX;
        int gMin,MinValue,temp;
        LMAX = (int)lmax;
        f = new int[LMAX+1];
        p = new int[LMAX+1];
        for (int i=0;i<=lmax;i++){
            f[i] = 0;
            p[i] = 0;
        }
        f[LMAX-delta] = 0;
        p[LMAX-delta] = 0;
        for (int t=LMAX-delta-1;t>=0;t--){
            gMin = delta-1;
            MinValue = Integer.MAX_VALUE;
            for (int g=delta-1;g>=1;g--){
                    int calcb = CalcB(t+g,1.0*g/delta,false);
                    temp = calcb + f[t+g];
                    //if (calcb>0) System.out.println("With g = " + g +" Calc B = " + calcb + " N[t+g] = "+N[t+g]);      
                    if (temp < MinValue&&temp>0&&calcb>0){
                        gMin = g;
                        MinValue = temp;  
                        //System.out.println(t + " " + g + " " + temp);
                        //if (temp==0) break;
                    }                                               
            }             
            f[t] = MinValue;
            p[t] = t+gMin;
            //System.out.println(t + " " + f[t] + " " + gMin);
        }
        //Truy vet
        int j = 1,next;        
        //  checkingPoint.clear();    
        checkingPoint.add(0);
        checkingPoint.add(p[1]);         
        next = p[checkingPoint.get(1)];
        while (next>0&&j<lmax){            
            j++;
            checkingPoint.add(next);
            next = p[checkingPoint.get(j)];
        }
        
        
        nFingerPrint = 0;
        for(int i=0;i<checkingPoint.size();i++){
            //System.out.print(checkingPoint.get(i)+ "th ");    
            if (i>1){
                int g = checkingPoint.get(i) - checkingPoint.get(i-1);
                if (g>0){
                    nFingerPrint += CalcB(checkingPoint.get(i),1.0*g/delta,false);
                }
              //System.out.println(" g = " + g + " b = " + CalcB(checkingPoint.get(i),1.0*g/delta,false) + " N= " + N[checkingPoint.get(i)]);
            }
        }         
        System.out.println("The Number of checking points is " + checkingPoint.size() + " with "+nFingerPrint/8/1024 + " Kbytes of memory");
    }    
    public int getNumberOfCheckingPoints(){
        return this.checkingPoint.size();
    }
    private long createSeed(File f){
        return f.length();
    }
    public int TestFileExisting(File f,Main.BloomFilter<String> bloomFilter[],int changingPos, String hashname){
        try{
            FileInputStream fi = new FileInputStream(f);
            boolean finish = false;
            Random rand = new Random(0);
            for (int j=1;j<checkingPoint.size();j++){
                byte fileContent[] = null;
                int len;                    
                if (j==1) {                     
                    len = checkingPoint.get(j)+1;
                    if (len > fi.available()){
                        len = fi.available();
                        finish = true;
                    }                        
                }
                else {                       
                    len = checkingPoint.get(j) - checkingPoint.get(j-1);
                    if (fi.available() < len){
                        len = fi.available();
                        finish = true;
                    }
                    if (checkingPoint.get(j)>=changingPos&&checkingPoint.get(j-1)<=changingPos){                       
                       // System.out.println("Distance between 2 checking points is " + len);
                    }
                        
                }                 
                fileContent = new byte[len];
                if (fileContent.length==0){
                        continue;
                    }                
                fi.read(fileContent, 0, len);                    
                Fingerprint fp = new Fingerprint(fileContent,createSeed(f));                                   
                long rs = fp.CalcFingerprint(hashname);
                long pos = Math.abs(rs%bloomFilter[j-1].getExpectedNumberOfElements());
                fileContent[fileContent.length-1]=(byte)rand.nextInt();
                fp = new Fingerprint(fileContent, createSeed(f));
                rs = fp.CalcFingerprint(hashname);
                long pos1 = Math.abs(rs%bloomFilter[j-1].getExpectedNumberOfElements());
                if (bloomFilter[j-1].getBit(pos)==false||bloomFilter[j-1].getBit(pos1)==false) {
                    //System.out.println("Detected at checking point: " + j);
                    //System.out.println("Detected at byte: " + checkingPoint.get(j));                   
                    int actualDetectionLag = checkingPoint.get(j) - changingPos;
                    return actualDetectionLag;                    
                }                
                if (finish) {
                    break;
                }
            }
        } catch (Exception e){            
            System.out.println(e.toString());
        }
        return -1;
    }
    public void createFingerprint(Main.BloomFilter<String> bloomFilter[], String hashname,Runtime runtime, main prev){
        try{
            count = 0;
            long counttotal = 0;            
            prev.UpdateBar(0);
            for (int i=0;i<fileList.size();i++){
                //cập nhật thanh trạng thái
                int percent = i*100/fileList.size();
                if (percent%3==0&&percent!=0){
                    prev.UpdateBar(percent);
                }
                FileInputStream fi = new FileInputStream(fileList.get(i));
                boolean finish = false;
                Random rand = new Random(0);
                for (int j=1;j<checkingPoint.size();j++){                    
                    byte fileContent[] = null;
                    int len;                    
                    if (j==1) {                     
                        len = checkingPoint.get(j)+1;
                        if (len > fi.available()){
                            len = fi.available();
                            finish = true;
                        }                        
                    }
                    else {                       
                        len = checkingPoint.get(j) - checkingPoint.get(j-1);
                        if (fi.available() < len){
                            len = fi.available();
                            finish = true;
                        }
                    }                 
                    fileContent = new byte[len];
                    fi.read(fileContent, 0, len);         
                    if (fileContent.length==0){
                        continue;
                    }
                    //hash1
                    Fingerprint fp = new Fingerprint(fileContent, createSeed(fileList.get(i)));                    
                    long rs = fp.CalcFingerprint(hashname);
                    long pos = Math.abs(rs%bloomFilter[j-1].getExpectedNumberOfElements());
                    counttotal++;               
                    if (bloomFilter[j-1].getBit(pos)==true){
                        count++;
                    }
                    bloomFilter[j-1].setBit(pos, true);    
                    fileContent[fileContent.length-1] = (byte)rand.nextInt();
                    //hash 2
                    fp = new Fingerprint(fileContent, createSeed(fileList.get(i)));
                    rs = fp.CalcFingerprint(hashname);
                    pos = Math.abs(rs%bloomFilter[j-1].getExpectedNumberOfElements());
                    counttotal++;
                    //count++;                    
                    if (bloomFilter[j-1].getBit(pos)==true){
                        //System.out.println("Duplicate bit in Bloom Filter at index of "+pos);
                        count++;
                    }
                    bloomFilter[j-1].setBit(pos, true);    
                    if (finish) {
                        break;
                    }
                }
                
                fi.close();                
                //System.out.println("Calculated all the fingerprints for file: " + i + " " + fileList.get(i).getName());
            }
            prev.UpdateBar(100);
            
            //System.out.println("Number of Duplicating bit is: "+count);
            //System.out.println("Total = " + counttotal);
        } catch (Exception e){
            System.out.println(e.toString());
        }
    }
    
}
