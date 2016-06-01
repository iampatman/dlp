/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import javax.swing.JFileChooser;

/**
 *
 * @author PC
 */
public class main extends javax.swing.JFrame {
    /**
     * Creates new form main
     */    

    File selectedRootFile = null;
    static Random rand = new Random(0);
    public main() {
        initComponents();
    }     
    private void setLabelTime(String Hashname, double time){
        switch (Hashname){
            case "MD5": tgMD5.setText(String.valueOf(time) + 's');
                    break;
            case "SHA1": tgSHA.setText(String.valueOf(time) + 's');
                    break;
            case "Murmur3": tgMM3.setText(String.valueOf(time)+ 's');
                    break;
            case "FNVHash": tgFNV.setText(String.valueOf(time)+ 's');
                    break;
            case "JenkinsHash": tgJKH.setText(String.valueOf(time)+ 's');
                    break;
            case "CityHash": tgCT.setText(String.valueOf(time)+ 's');
                    break;                                
        };
        update(this.getGraphics());        
    }
    private void setLabelNoFile(String Hashname, int no){
        switch (Hashname){
            case "MD5": nlMD5.setText(String.valueOf(no));
                    break;
            case "SHA1": nlSHA.setText(String.valueOf(no));
                    break;
            case "Murmur3": nlMM3.setText(String.valueOf(no));
                    break;
            case "FNVHash": nlFNV.setText(String.valueOf(no));
                    break;
            case "JenkinsHash": nlJKH.setText(String.valueOf(no));
                    break;
            case "CityHash": nlCT.setText(String.valueOf(no));
                    break;                                
        };
        update(this.getGraphics());
    }
    public void UpdateBar(int progressvalue){        
        if (progressvalue!=jProgressBar.getValue()){
            jProgressBar.setValue(progressvalue);            
            jProgressBar.update(jProgressBar.getGraphics());        
        }                
    }
    public void Implement() {      
            // khởi tạo
            jProgressBar.setMinimum(0);
            jProgressBar.setMaximum(100);
            //Doc file vao Collector                          
            float falsePositive=0.001f;
            List<String> HashName = new ArrayList<>();
            HashName.add("SHA1"); 
            if (chkMD5.isSelected()==true){
                HashName.add("MD5");                
            }
            if (chkSHA1.isSelected()==true){
                HashName.add("SHA1");                
            }
            if (chkMM3.isSelected()==true){
                HashName.add("Murmur3");                
            }
            if (chkFNV.isSelected()==true){
                HashName.add("FNVHash");                
            }
            if (chkCT.isSelected()==true){
                HashName.add("CityHash");                
            }
            if (chkJKH.isSelected()==true){
                HashName.add("JenkinsHash");                
            }                       
            
            FileCollector fileCollector = new FileCollector(selectedRootFile, 1000,0.01f);
            fileCollector.Implement();      
            //int detectionlag=1000;
            for (int i=0;i<HashName.size();i++){                          
                //Khoi tao bloom filter
                rand = new Random(1);
                System.out.println(HashName.get(i));
                BloomFilter bloomFilter[];
                bloomFilter = new BloomFilter[fileCollector.getNumberOfCheckingPoints()];
                Runtime runtime = Runtime.getRuntime();     
                //System.out.println("Free/Total Memory = " + runtime.freeMemory()/(1024L * 1024L) + "/" + runtime.totalMemory()/(1024L * 1024L));                
                for (int j=1;j<fileCollector.checkingPoint.size();j++){
                    int g = fileCollector.checkingPoint.get(j) - fileCollector.checkingPoint.get(j-1);
                    int bloomfilterSize = fileCollector.CalcB(fileCollector.checkingPoint.get(j),falsePositive,true);
                    bloomFilter[j-1] = new BloomFilter<String>((double)1,bloomfilterSize, 1);                                        
                    //System.out.println("BF: " +j+ " size = " + bloomFilter[j-1].getExpectedNumberOfElements() );
                }
                //System.out.println("Free/Total Memory = " + runtime.freeMemory()/(1024L * 1024L) + "/" + runtime.totalMemory()/(1024L * 1024L));
                System.out.println("BloomFilter Initialized");          
                //Tao fingerprint va dua vao bloom filter
                long starttime,endtime,time;
                starttime = new Date().getTime();
                String hashname;
                hashname = HashName.get(i);
                fileCollector.createFingerprint(bloomFilter,hashname,runtime,this);
                endtime = new Date().getTime();
                time = endtime-starttime;
                System.out.println("Creating all the fingerprints completed");            
                System.out.println("Time: " + 1.0*time/1000+" seconds");        
                this.setLabelTime(hashname, 1.0*time/1000);                                    
               // System.out.println("Number of added elements is "+ bloomFilter.count());
                //Qua trinh tao ra cac file test va test
                /*
                for (int j=0;j<bloomFilter.length;j++){
                    System.out.print("BF " + (j+1) +"th: ");
                    System.out.println(bloomFilter[j].count()+ "/" +bloomFilter[j].getExpectedNumberOfElements());
                    
                
                */               
                float totalDetectionLag = new Float(0);
                int numberoftests=1000;
                int filesExistCount = 0;
                for (int j=0;j<numberoftests;j++){
                    //System.out.println("Test: "+ (i+1) + " ");
                    int rs = Testing(fileCollector, bloomFilter,hashname);
                    if (rs>0) {
                        filesExistCount++;
                        totalDetectionLag += rs;
                    }
                } 
                filesExistCount = numberoftests - filesExistCount;            
                System.out.println("Number of Files leakaging is " + filesExistCount);
                this.setLabelNoFile(hashname, filesExistCount);
               //System.out.println("Total Actual Detection Lag "+totalDetectionLag);
                System.out.println("Average Detection Lag "+totalDetectionLag/numberoftests);
                System.out.println("===================================");        
            }
        }
    public int Testing(FileCollector fileCollector, BloomFilter<String> bloomFilter[], String hashname){
        //Bien random dung de random file va bad byte
        int randomNumber = rand.nextInt(fileCollector.getNumberOfFiles());
        if (randomNumber<0){
                System.out.println("Random number for selecting file is negative");
                randomNumber = Math.abs(randomNumber);
            }        
        File fileTest = fileCollector.getFile(randomNumber);
        TestFile tf = new TestFile(fileTest);
        int actualDetectionLag = 0;
        if (tf.ChangeBadByte(rand)==false){
           System.out.println("Bad-byte has been changed unsucessfully");
           return 0;           
        }
        fileTest = new File("TestFile");
        //System.out.println("Changing location is "+ tf.getChangingPosition());
        actualDetectionLag = fileCollector.TestFileExisting(fileTest, bloomFilter,tf.getChangingPosition(),hashname);
        if (actualDetectionLag==-1){
            //System.out.println("File exists");
            fileTest.delete();
            return -1;
        } else {
            //System.out.println("File doesnt exist");                                
            //System.out.println("Actual Detection Lag: " + actualDetectionLag + "bytes");            
            fileTest.delete();            
            return actualDetectionLag;
        }                        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tgMM3 = new javax.swing.JLabel();
        tgSHA = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        nlJKH = new javax.swing.JLabel();
        chkCT = new javax.swing.JCheckBox();
        nlCT = new javax.swing.JLabel();
        chkFNV = new javax.swing.JCheckBox();
        nlFNV = new javax.swing.JLabel();
        tgMD5 = new javax.swing.JLabel();
        nlMM3 = new javax.swing.JLabel();
        chkJKH = new javax.swing.JCheckBox();
        txtPath = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        chkMM3 = new javax.swing.JCheckBox();
        chkSHA1 = new javax.swing.JCheckBox();
        chkMD5 = new javax.swing.JCheckBox();
        jProgressBar = new javax.swing.JProgressBar();
        tgCT = new javax.swing.JLabel();
        tgJKH = new javax.swing.JLabel();
        nlMD5 = new javax.swing.JLabel();
        nlSHA = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        tgFNV = new javax.swing.JLabel();
        btnRUN = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Demo Luận Văn An Trung - Hoài Trung");
        setResizable(false);

        tgMM3.setText("0");

        tgSHA.setText("0");

        jLabel15.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel15.setText("Tên Hàm băm");

        nlJKH.setText("0");

        chkCT.setText("City Hash");

        nlCT.setText("0");

        chkFNV.setText("FNV Hash");

        nlFNV.setText("0");

        tgMD5.setText("0");

        nlMM3.setText("0");

        chkJKH.setText("Jenkins Hash");

        txtPath.setEditable(false);
        txtPath.setText("Đường dẫn");

        jButton1.setText("Chọn Thư Mục");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        chkMM3.setText("Murmur3");
        chkMM3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkMM3ActionPerformed(evt);
            }
        });

        chkSHA1.setText("SHA1");
        chkSHA1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkSHA1ActionPerformed(evt);
            }
        });

        chkMD5.setText("MD5");

        jProgressBar.setStringPainted(true);

        tgCT.setText("0");

        tgJKH.setText("0");

        nlMD5.setText("0");

        nlSHA.setText("0");

        jLabel16.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel16.setText("Thời gian thực hiện");

        jLabel17.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel17.setText("Số lượng tập tin rò rỉ");

        tgFNV.setText("0");

        btnRUN.setText("Run");
        btnRUN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRUNActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(txtPath)
                                .addGap(18, 18, 18)
                                .addComponent(jButton1)))
                        .addGap(21, 21, 21))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(chkCT, javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(chkJKH)
                                    .addGap(100, 100, 100)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(chkFNV)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGap(10, 10, 10)
                                                .addComponent(jLabel15))
                                            .addComponent(chkMD5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGap(18, 18, 18)
                                                .addComponent(jLabel16))
                                            .addGroup(layout.createSequentialGroup()
                                                .addGap(24, 24, 24)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addComponent(tgMD5, javax.swing.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
                                                    .addComponent(tgSHA, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(tgMM3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(tgFNV, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(tgCT, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(tgJKH, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(chkSHA1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(chkMM3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addGap(48, 48, 48)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(nlCT, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(nlFNV, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(nlMM3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(nlSHA, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel17, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(nlMD5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(nlJKH, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap(13, Short.MAX_VALUE))))
            .addGroup(layout.createSequentialGroup()
                .addGap(152, 152, 152)
                .addComponent(btnRUN, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(txtPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(jLabel16)
                    .addComponent(jLabel17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkMD5)
                    .addComponent(tgMD5)
                    .addComponent(nlMD5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkSHA1)
                    .addComponent(tgSHA)
                    .addComponent(nlSHA))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkMM3)
                    .addComponent(tgMM3)
                    .addComponent(nlMM3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkFNV)
                    .addComponent(tgFNV)
                    .addComponent(nlFNV))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkCT)
                    .addComponent(tgCT)
                    .addComponent(nlCT))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkJKH)
                    .addComponent(tgJKH)
                    .addComponent(nlJKH))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnRUN)
                .addContainerGap(23, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        JFileChooser filechooser = new JFileChooser();
        filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnval = filechooser.showOpenDialog(this);
        if (returnval == JFileChooser.APPROVE_OPTION){
            selectedRootFile = filechooser.getSelectedFile();
            this.txtPath.setText(selectedRootFile.getPath());
        }      

    }//GEN-LAST:event_jButton1ActionPerformed

    private void chkMM3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkMM3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_chkMM3ActionPerformed

    private void chkSHA1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkSHA1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_chkSHA1ActionPerformed

    private void btnRUNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRUNActionPerformed
        // TODO add your handling code here:
        this.Implement();
    }//GEN-LAST:event_btnRUNActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new main().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnRUN;
    private javax.swing.JCheckBox chkCT;
    private javax.swing.JCheckBox chkFNV;
    private javax.swing.JCheckBox chkJKH;
    private javax.swing.JCheckBox chkMD5;
    private javax.swing.JCheckBox chkMM3;
    private javax.swing.JCheckBox chkSHA1;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JProgressBar jProgressBar;
    private javax.swing.JLabel nlCT;
    private javax.swing.JLabel nlFNV;
    private javax.swing.JLabel nlJKH;
    private javax.swing.JLabel nlMD5;
    private javax.swing.JLabel nlMM3;
    private javax.swing.JLabel nlSHA;
    private javax.swing.JLabel tgCT;
    private javax.swing.JLabel tgFNV;
    private javax.swing.JLabel tgJKH;
    private javax.swing.JLabel tgMD5;
    private javax.swing.JLabel tgMM3;
    private javax.swing.JLabel tgSHA;
    private javax.swing.JTextField txtPath;
    // End of variables declaration//GEN-END:variables
}
