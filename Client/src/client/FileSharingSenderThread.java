package client;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.text.DecimalFormat;
import javax.swing.JOptionPane;

public class FileSharingSenderThread implements Runnable {
    
    protected Socket socket;
    protected String file;
    protected String receiver;
    protected String sender;
    protected FileSharing sendFile;
    protected DecimalFormat df = new DecimalFormat("##,#00");
    
    private DataOutputStream dosWriter;
    private final int BUFFER_SIZE = 100;
    
    public FileSharingSenderThread(Socket socket, String file, String receiver, String sender, FileSharing sendFile){
        this.socket = socket;
        this.file = file;
        this.receiver = receiver;
        this.sender = sender;
        this.sendFile = sendFile;
    }

    @Override
    public void run() {
        try {
            sendFile.disableGUI(true);
            dosWriter = new DataOutputStream(socket.getOutputStream());
            
            // send file to server (SocketThread.java) by dosWriter using command sSendFile
            // format: filename filesize receiver sender
            File filename = new File(file);
            int len = (int) filename.length();
            int filesize = (int)Math.ceil(len / BUFFER_SIZE); // get the file size
            String clean_filename = filename.getName();
            dosWriter.writeUTF("sSendFile "+ clean_filename.replace(" ", "_") +" "+ filesize +" "+ receiver +" "+ sender);
         
            // Create an input stream 
            InputStream input = new FileInputStream(filename);
            OutputStream output = socket.getOutputStream();
            
            BufferedInputStream bis = new BufferedInputStream(input);
            byte[] buffer = new byte[BUFFER_SIZE];

            int count = 0;
            while((count = bis.read(buffer)) > 0){
                output.write(buffer, 0, count);
            }
       
            sendFile.setFormTitle("File successfuly sent!");
            sendFile.updateFile(false); 
            JOptionPane.showMessageDialog(sendFile, "File successfully sent!", "Sucess", JOptionPane.INFORMATION_MESSAGE);
            sendFile.closeFileSharingForm();
            
            /* Close input stream */
            output.flush();
            output.close();
        } catch (IOException e) {
            sendFile.updateFile(false); 
        }
    }
}
