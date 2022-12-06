package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.StringTokenizer;
import javax.swing.JOptionPane;

public class FileSharing extends javax.swing.JFrame {
    
    private Client client;
    private Socket socket;
    private String client_username;
    private String sender;
    private String receiver;
    private String file;
    private DataInputStream disReader;
    private DataOutputStream dosWriter;
    private StringTokenizer st;
    private int port;
    
    /** Creates new form SendFile **/
    public FileSharing() {
        initComponents();
    }
    
    
    /**
     * Executed when the user prompts Send File, connect to server and prepare for file sharing connection.
     * Starts the send file thread. 
     * @param user
     * @param sender
     * @param receiver
     * @param p
     * @param c
     * @return
    **/
    public boolean startFileSharing(String user, String receiver, String sender, int p, Client c){
        this.sender = sender;
        this.receiver = receiver;
        this.client_username = user;
        this.port = p;
        this.client = c;
        
        setTitle("You are logged in as: " + client_username + " | Send a File");
        txtSendTo.setText(receiver);
        try {
            socket = new Socket(sender, port);
            dosWriter = new DataOutputStream(socket.getOutputStream());
            disReader = new DataInputStream(socket.getInputStream());
            String format = "userFileSocket " + client_username;
            dosWriter.writeUTF(format);
            
            new Thread(new SendFileThread(this)).start();
            return true;
        } 
        catch (IOException e) {
            System.out.println("[SendFileThreadError]" + e.getMessage());
        }
        return false;
    }
    
    
    
    /** Handles send file thread request from server **/
    class SendFileThread implements Runnable{
        private FileSharing filesharing;
        
        public SendFileThread(FileSharing filesharing){
            this.filesharing = filesharing;
        }
        
        private void closeSendFileThread(){
            try {
                socket.close();
            } 
            catch (IOException e) {
                System.out.println("[SendFileThreadClose]: " + e.getMessage());
            }
            dispose();
        }
        
        @Override
        public void run() {
            try {
                while(!Thread.currentThread().isInterrupted()){
                    
                    String data = disReader.readUTF();  
                    st = new StringTokenizer(data);
                    String action = st.nextToken();  
                    
                    switch(action){
                        
                         // action executed when there is an error while sending the file 
                        // show error to user by calling JOptionPane
                        // format: sFileError msg
                        case "sFileError":
                            String sFileError_msg = "";
                            while(st.hasMoreTokens()){
                                sFileError_msg = sFileError_msg +" "+ st.nextToken();
                            }                                                                   
                            JOptionPane.showMessageDialog(FileSharing.this, sFileError_msg, "Error" , JOptionPane.ERROR_MESSAGE);
                            filesharing.updateFile(false);
                            filesharing.disableGUI(false);
                            filesharing.updateSendFileBtn("Send File");
                            break;
                        
                        // action executed for the receiver's error response to sending file error status
                        // show error to user by calling JOptionPane
                        // format: sFileStatus msg
                        case "sFileStatus":
                            String sFileStatus_msg = "";
                            
                            while(st.hasMoreTokens()){
                                sFileStatus_msg = sFileStatus_msg + " " + st.nextToken();
                            }
                            
                            filesharing.updateFile(false);
                            JOptionPane.showMessageDialog(FileSharing.this, sFileStatus_msg, "Error", JOptionPane.ERROR_MESSAGE);
                            dispose();
                            break;
                            
                        // action that handles error when receiver encounters error when receiving the file from sender
                        // format: rFileError msg
                        case "rFileError":  
                            String rFileError_msg = "";
                            
                            while(st.hasMoreTokens()){
                                rFileError_msg = rFileError_msg + " " + st.nextToken();
                            }
                            
                            filesharing.updateFile(false);
                            JOptionPane.showMessageDialog(FileSharing.this, rFileError_msg, "Error", JOptionPane.ERROR_MESSAGE);
                            this.closeSendFileThread();
                            break;
                        
                        // action executed when receiver accepts the file being sent by the sender
                        // starts file sharing sender thread so sender can now proceed to sending his/her file
                        case "rFileAccept": 
                            new Thread(new FileSharingSenderThread(socket, file, receiver, client_username, FileSharing.this)).start();
                            break;
                    }
                }
            } 
            catch (IOException e) {
                System.out.println(e.getMessage());
            }
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

        chooser = new javax.swing.JFileChooser();
        txtSendTo = new javax.swing.JTextField();
        btnSendFile = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        txtFile = new javax.swing.JTextField();
        btnBrowse = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);
        setResizable(false);
        setType(java.awt.Window.Type.POPUP);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        txtSendTo.setEditable(false);
        txtSendTo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSendToActionPerformed(evt);
            }
        });

        btnSendFile.setBackground(new java.awt.Color(51, 153, 0));
        btnSendFile.setForeground(new java.awt.Color(255, 255, 255));
        btnSendFile.setText("Send File");
        btnSendFile.setAlignmentY(0.0F);
        btnSendFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendFileActionPerformed(evt);
            }
        });

        jLabel1.setText("Select a File:");

        txtFile.setEditable(false);
        txtFile.setBackground(new java.awt.Color(255, 255, 255));
        txtFile.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        txtFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFileActionPerformed(evt);
            }
        });

        btnBrowse.setBackground(new java.awt.Color(204, 204, 204));
        btnBrowse.setFont(new java.awt.Font("SansSerif", 1, 11)); // NOI18N
        btnBrowse.setText("Browse");
        btnBrowse.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseActionPerformed(evt);
            }
        });

        jLabel2.setText("Send File to:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(txtSendTo)
                            .addComponent(txtFile, javax.swing.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnSendFile, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)
                            .addComponent(btnBrowse, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jLabel1)
                .addGap(1, 1, 1)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFile, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtSendTo, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSendFile, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSendFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendFileActionPerformed
        // TODO add your handling code here:
//        receiver = txtSendTo.getText();
        file = txtFile.getText();
        
        if((file.length() > 0) && (receiver.length() > 0)){
            try {
                txtFile.setText("");
                String fname = getFilename(file);
                String format = "userFileConnection " + client_username + " " + receiver + " " + fname;
                dosWriter.writeUTF(format);
    
                updateSendFileBtn("Sending...");
                btnSendFile.setEnabled(false);
            } 
            catch (IOException e) {
                System.out.println("[FileSharing]" + e.getMessage());
            }
        }
        else{
            JOptionPane.showMessageDialog(this, "Incomplete Form! Please try again.", "Send File Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSendFileActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        client.setSendFile(false);
        try {
            socket.close();
        } 
        catch (IOException e) {
            System.out.println("[SendFileFormClosingError]" + e.getMessage());
        }
    }//GEN-LAST:event_formWindowClosing

    private void btnBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseActionPerformed
        // TODO add your handling code here:
        browseFiles();
    }//GEN-LAST:event_btnBrowseActionPerformed

    private void txtFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFileActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFileActionPerformed

    private void txtSendToActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSendToActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSendToActionPerformed
    
    
    /** Calls the JFileChooser for user to browse and find a file to be sent **/
    public void browseFiles(){
        int open = chooser.showOpenDialog(this);
        if(open == chooser.APPROVE_OPTION){
            txtFile.setText(chooser.getSelectedFile().toString());
        }else{
            txtFile.setText("");
        }
    }
    
     /** This method will disable/enabled GUI file sharing form**/
    public void disableGUI(boolean disable){
        if(disable){ 
            btnSendFile.setEnabled(false);
            btnBrowse.setEnabled(false);
            txtSendTo.setEditable(false);
//            txtFile.setEditable(false);
        } 
        else { 
            btnSendFile.setEnabled(true);
            btnBrowse.setEnabled(true);
//            txtFile.setEditable(true);
        }
    }
   
    /** Executed when user prompts to close send file form **/
    protected void closeFileSharingForm(){
        dispose();
    }
    
    /** Sets form title
     *  @param title
     **/
    public void setFormTitle(String title){
        setTitle(title);
    }
    
    
    /** Gets filename to be shared 
     *  @param path
     *  @return 
     **/
    public String getFilename(String path){
        File file_path = new File(path);
        String fname = file_path.getName();
        return fname.replace(" ", "_");
    }
    
    /** Updates File 
     *  @param b
     **/
    public void updateFile(boolean b){
        client.setSendFile(b);
    }
    
    /** Updates send file button text
     *  @param str
     **/
    public void updateSendFileBtn(String str){
        btnSendFile.setText(str);
    }
      
    /**
     * Creates and displays the form
     * @param args 
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FileSharing().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBrowse;
    private javax.swing.JButton btnSendFile;
    private javax.swing.JFileChooser chooser;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField txtFile;
    private javax.swing.JTextField txtSendTo;
    // End of variables declaration//GEN-END:variables
}
