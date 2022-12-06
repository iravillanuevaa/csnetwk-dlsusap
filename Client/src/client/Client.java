package client;

import java.awt.Color;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.File;

public class Client extends javax.swing.JFrame {
    
    Socket socket;
    DataOutputStream dosWriter;
    String username, host, receiver = "";
    int port;
    public boolean sendFile = false;
    private boolean isOnline = false;
    private String download_folder = "D:\\";
    
    /** Instantiates MainForm and calls initComponents() **/
    public Client() {
        initComponents();
    }
    
    /** Calls userConnect function 
     * @param username
     * @param host
     * @param port
     */
    public void initChatFrame(String username, String host, int port){
        this.username = username;
        this.host = host;
        this.port = port;
        setTitle("You are logged in as: " + username + " | De La Salle Usap");
        clientConnect();
    }
    
    /** Connects user to DLSU Chat room **/
    public void clientConnect(){
        appendReceivedMsg(" Connecting...", "Status", Color.green, Color.green);
        try {
            socket = new Socket(host, port);
            dosWriter = new DataOutputStream(socket.getOutputStream());
            /** Send username **/
            dosWriter.writeUTF("userJoin "+ username);
            appendReceivedMsg(" Connected! You are now online in this chatroom. At the left part of the screen, you can view users who are online.", "Status", Color.green, Color.green);
            appendReceivedMsg(" You can only start sending msgs and files when there is someone online in the chat room with you.", "Note", Color.green, Color.green);
            /* Start Client Thread and set user status to connected */
            new Thread(new ClientThread(socket, this)).start();
            jButton1.setEnabled(true);
            isOnline = true;
        }
        catch(IOException e) {
            isOnline = false;
            JOptionPane.showMessageDialog(this, "Unable to connect to server, please try again later :(", "Connection Failed", JOptionPane.ERROR_MESSAGE);
            appendReceivedMsg("[IOException]: "+ e.getMessage(), "Error", Color.RED, Color.RED);
        }
    }
    
    /** Return current online status of client 
     * @return
     */
    public boolean isConnected(){
        return this.isOnline;
    }
    
    // APPEND in client chat box (appendReceivedMsg, appendSentMsg, appendOnlineUsers)
    
    /** Append message from other user in your chat box
     * @param msg
     * @param header
     * @param headerColor
     * @param contentColor
     */
    public void appendReceivedMsg(String msg, String header, Color headerColor, Color contentColor){
        jTextPane1.setEditable(true);
        getMsgType(header, headerColor);
        getMsgContent(msg, contentColor);
        jTextPane1.setEditable(false);
    }
 
    /** Append your message to your chat box
     * @param msg
     * @param header
     */
    public void appendSentMsg(String msg, String header){
        jTextPane1.setEditable(true);
        getMsgType(header, Color.blue);
        getMsgContent(msg, Color.darkGray);
        jTextPane1.setEditable(false);
    }
      
    /** Append current online users list in your chat to view
     * @param list
     */
    public void appendOnlineUsers(Vector list){
          getOnlineUsers(list);  
    }
    
    // SET setSendFile, setChatFrameTitle 
    
    /**
     * Update attachment
     * @param b
     */
    public void setSendFile(boolean b){
        this.sendFile = b;
    }
    
    /**
     * Set title
     * @param s
    */
    public void setChatFrameTitle(String s){
        setTitle(s);
    }
    
    // GETTERS getClientDownloadFolder, getClientHost, getClientPort, getClientUsername, getMsgType, getMsgContent, getOnlineUsers, getClientDownloadFolder
    
    /**
     * Get host/send
     * @return
    */
    public String getClientHost(){
        return this.host;
    }
   
    /**
     * Get port
     * @return
    */
    public int getClientPort(){
        return this.port;
    }
   
    /**
     * Get username
     * @return
    */
    public String getClientUsername(){
        return this.username;
    }
    
    /** Get message type (name of client, status)  
     * @param header
     * @param color
    */
    public void getMsgType(String header, Color color){
        int len = jTextPane1.getDocument().getLength();
        jTextPane1.setCaretPosition(len);
        jTextPane1.setCharacterAttributes(MsgFormat.styleMsg(color, "Arial Black", 13), false);
        jTextPane1.replaceSelection(header + ":");
    }
    
    /** Get message content
     * @param msg
     * @param color
    */
    public void getMsgContent(String msg, Color color){
        int len = jTextPane1.getDocument().getLength();
        jTextPane1.setCaretPosition(len);
        jTextPane1.setCharacterAttributes(MsgFormat.styleMsg(color, "SansSerif", 12), false);
        jTextPane1.replaceSelection(msg +"\n");
    }
    
    /**
     * Get Download Folder
     * @return
    */
    public String getClientDownloadFolder(){
        return this.download_folder;
    }
    
    /**
     * Append and show online list
     * If there is no user on the online list, client cannot send a message or file
     * @param list
    */
    public void getOnlineUsers(Vector list){
        try {
            txtpane2.setEditable(true);
            txtpane2.setContentType("text/html");
            StringBuilder sb = new StringBuilder();
            Iterator iterator = list.iterator();
            sb.append("<html><table>");
            while(iterator.hasNext()){
                Object e = iterator.next();
                this.receiver = e.toString();
                sb.append("<tr><td><b>@</b></td><td>").append(e).append("</td></tr>");
            }
            sb.append("</table></body></html>");
            txtpane2.removeAll();
            txtpane2.setText(sb.toString());
            txtpane2.setEditable(false);
            
            if ((this.receiver.equals("")) || (list.isEmpty() == true)){
                jTextField1.setEditable(false);
                jButton1.setEnabled(false);
                sendFileMenu.setEnabled(false);
                
            }
            else{
                jTextField1.setEditable(true);
                jButton1.setEnabled(true);
                sendFileMenu.setEnabled(true);
            }
        } 
        catch (Exception e) {
            System.out.println("[OnlineListError]" + e.getMessage());
        }
    }
   
    /**
     * Open JFileChooser to set download folder of client
     */
    public void openFolder(){
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int open = chooser.showDialog(this, "Select Folder");
        if(open == chooser.APPROVE_OPTION){
            download_folder = chooser.getSelectedFile().toString()+"\\";
        } else {
            download_folder = "D:\\";
        }
    }
    
    /**
     * Open JFileChooser to set download folder of client for chat log text file
     * @param text_log
     */
    public void saveLogFile(String text_log){
        openFolder();
        String path = getClientDownloadFolder();
        System.out.println(text_log);
        path = path.replace("\\", "\\\\");
        String filename = username + "_ChatLogs.txt";
        String file = path + filename;
        System.out.println(file);     
        try{
            File log = new File(file);
            
            if (log.createNewFile()) {
                System.out.println("File created: " + log.getName());
                
            } 
        }
        catch (IOException e){
            System.out.println("An error occurred." + e);
        }
        
        try{
            PrintWriter pw = new PrintWriter(new FileWriter(file));
            text_log = text_log.replaceAll(",", "\r\n");
            pw.println(text_log);
            pw.flush();
            pw.close();
            socket.close();
            setVisible(false);
            /* redirects user to Login Form **/
            new LoginForm().setVisible(true);
        }
        catch(IOException e){
            System.out.println("An error occurred." + e);
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
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtpane2 = new javax.swing.JTextPane();
        jLabel1 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu3 = new javax.swing.JMenu();
        sendFileMenu = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        LogoutMenu = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setBackground(new java.awt.Color(145, 53, 53));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jButton1.setBackground(new java.awt.Color(51, 153, 0));
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Send");
        jButton1.setActionCommand("SendMessage");
        jButton1.setEnabled(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTextPane1.setFont(jTextPane1.getFont());
        jScrollPane1.setViewportView(jTextPane1);

        txtpane2.setFont(new java.awt.Font("Tahoma", 1, 9)); // NOI18N
        txtpane2.setForeground(new java.awt.Color(120, 14, 3));
        txtpane2.setAutoscrolls(false);
        txtpane2.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jScrollPane3.setViewportView(txtpane2);

        jLabel1.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        jLabel1.setText("Online List");
        jLabel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jMenu3.setText("Send File");
        jMenu3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu3ActionPerformed(evt);
            }
        });

        sendFileMenu.setText("Send File");
        sendFileMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendFileMenuActionPerformed(evt);
            }
        });
        jMenu3.add(sendFileMenu);

        jMenuBar1.add(jMenu3);

        jMenu2.setText("Account");

        LogoutMenu.setText("Logout");
        LogoutMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LogoutMenuActionPerformed(evt);
            }
        });
        jMenu2.add(LogoutMenu);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addComponent(jLabel1)))
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(32, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        try {
            String chat = username + " " + this.receiver  + " " + jTextField1.getText();
            dosWriter.writeUTF("userPostChat " + chat);
            System.out.println(this.receiver);
            appendSentMsg(" " + jTextField1.getText(), username);
            jTextField1.setText("");
        } 
        catch (IOException e) {
            appendReceivedMsg(" Unable to send message due to server is not available at this time :( Please try again later!", "Error", Color.RED, Color.RED);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jMenu3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenu3ActionPerformed

    private void sendFileMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendFileMenuActionPerformed
        // TODO add your handling code here:
        if(!sendFile){
            FileSharing s = new FileSharing();
            if(s.startFileSharing(username, receiver, host, port, this)){
                s.setLocationRelativeTo(null);
                s.setVisible(true);
                sendFile = true;
            } 
            else {
                JOptionPane.showMessageDialog(this, "Unable to establish File Sharing connection :( Please try again later.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_sendFileMenuActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        int confirm = JOptionPane.showConfirmDialog(this, "Close De La Salle Usap?");
        if(confirm == 0){
            int log_save = JOptionPane.showConfirmDialog(null, "Do you want to save your activity log?");
            if(log_save == 0){
               try {
                    dosWriter.writeUTF("userChatLogs " + username);
                } 
                catch (IOException e) {
                    System.out.println("[LogoutError]: " + e.getMessage());
                }
            } 
            else {
                try {
                    socket.close();
                    setVisible(false);
                    /* redirects user to Login Form **/
                    new LoginForm().setVisible(true);
                }
                catch(IOException e) {
                    System.out.println("[LogoutError]: "+ e.getMessage());
                }
            }
        }
    }//GEN-LAST:event_formWindowClosing

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
        try {
            String content = username + " " + this.receiver + " " +evt.getActionCommand();
            dosWriter.writeUTF("userPostChat "+ content);
            appendSentMsg(" " + evt.getActionCommand(), username);
            jTextField1.setText("");
        } 
        catch (IOException e) {
            appendReceivedMsg(" Server is offline... You are unable to send a message :( Please try again later.", "Error", Color.RED, Color.RED);
        }
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void LogoutMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LogoutMenuActionPerformed
        // TODO add your handling code here:
        int confirm = JOptionPane.showConfirmDialog(null, "Log out?");
        if(confirm == 0){
            int log_save = JOptionPane.showConfirmDialog(null, "Do you want to save your activity log?");
                if(log_save == 0){
                    try {
                        dosWriter.writeUTF("userChatLogs " + username);
                    } 
                    catch (IOException e) {
                        System.out.println("[LogoutError]: "+e.getMessage());
                    }
                }
                else{
                    try {
                        socket.close();
                        setVisible(false);
                        /* redirects user to Login Form **/
                        new LoginForm().setVisible(true);
                    }
                    catch(IOException e) {
                        System.out.println("[LogoutError]: "+ e.getMessage());
                    }
                }
        }
       
    }//GEN-LAST:event_LogoutMenuActionPerformed

    /**
     * @param args the command line arguments
     */
    
    public static void main(String args[]) {
     
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Client().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem LogoutMenu;
    private javax.swing.JFileChooser chooser;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JMenuItem sendFileMenu;
    private javax.swing.JTextPane txtpane2;
    // End of variables declaration//GEN-END:variables
}
