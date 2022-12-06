package client;

import java.io.IOException;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.StringTokenizer;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitorInputStream;

public class FileSharingReceiverThread implements Runnable {
    
    protected Client client;
    protected Socket socket;
    protected DataInputStream disReader;
    protected DataOutputStream dosWriter;
    protected DecimalFormat df = new DecimalFormat("##,#00");
    protected StringTokenizer st;
    private final int BUFFER_SIZE = 100;
    
    public FileSharingReceiverThread(Socket socket, Client client){
        this.socket = socket;
        this.client = client;
        
        try {
            dosWriter = new DataOutputStream(socket.getOutputStream());
            disReader = new DataInputStream(socket.getInputStream());
        } 
        catch (IOException e) {
            System.out.println("[FileSharingReceiverThread]: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            // continue listening in the current thread to transfer file from sender to receiver
            while(!Thread.currentThread().isInterrupted()){
                String data = disReader.readUTF();
                st = new StringTokenizer(data);
                String action = st.nextToken();
                
                switch(action){
                    
                    // download sent file 
                    // receiver will choose what directory to save the file, calls getClientDownloadFolder
                    // format: ssendFile file_name file_size receiver sender
                    case "sSendFile":
                        String sender = null;
                            try {
                                String file_name = st.nextToken();
                                int file_size = Integer.parseInt(st.nextToken());
                                sender = st.nextToken(); 
                                client.setChatFrameTitle("Downloading File....");
                                int confirm = JOptionPane.showConfirmDialog(client, "Do you want to rename the file?");
                                String path;
                                
                                if(confirm == 0){
                                    String new_filename = JOptionPane.showInputDialog("Rename file: ");
                                    String ext = file_name.substring(file_name.lastIndexOf("."));
                                    path = client.getClientDownloadFolder() + new_filename + ext; 
                                    
                                }
                                
                                else{
                                     path = client.getClientDownloadFolder() + file_name; 
                                } 
                                
                                // create file output stream and input stream and monitor file progress
                                FileOutputStream fosWriter = new FileOutputStream(path);
                                InputStream input = socket.getInputStream();    
                                ProgressMonitorInputStream pmis = new ProgressMonitorInputStream(client, "Downloading file please wait...", input);
                                
                                // create buffered input stream
                                BufferedInputStream bis = new BufferedInputStream(pmis);
                                
                                // create a temporary file
                                byte[] buffer = new byte[BUFFER_SIZE];
                                int count;
                                
                                while((count = bis.read(buffer)) != -1){
                                    fosWriter.write(buffer, 0, count);
                                }
                                
                                fosWriter.flush();
                                fosWriter.close();
                                client.setChatFrameTitle("You are logged in as: " + client.getClientUsername() + " | De La Salle Usap");
                                JOptionPane.showMessageDialog(null, "File has been downloaded to \n'"+ path +"'");
                            } 
                            catch (IOException e) {
                                
                                // if error, send back an error message to sender
                                DataOutputStream eDos = new DataOutputStream(socket.getOutputStream());
                                eDos.writeUTF("sFileStatus "+ sender + " Connection lost :( File transfer unsuccessful...,");
                                
                                System.out.println( "[FileSharingReceiverThread]" + e.getMessage());
                                client.setChatFrameTitle("You are logged in as: " + client.getClientUsername() + " | De La Salle Usap");
                                JOptionPane.showMessageDialog(client, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
                                socket.close();
                            }
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("[FileSharingReceiverThread]: " +e.getMessage());
        }
    }
}
