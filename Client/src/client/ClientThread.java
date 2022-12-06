package client;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JOptionPane;


public class ClientThread implements Runnable{
    
    Client client;
    Socket socket;
    DataInputStream disReader;
    DataOutputStream dosWriter;
    StringTokenizer st;
    protected DecimalFormat df = new DecimalFormat("##,#00");
    
    public ClientThread(Socket socket, Client client){
        this.client = client;
        this.socket = socket;
        try {
            disReader = new DataInputStream(socket.getInputStream());
        } 
        catch (IOException e) {
            client.appendReceivedMsg("[IOException]: "+ e.getMessage(), "Error", Color.RED, Color.RED);
        }
    }

    @Override
    public void run() {
        try {
            while(!Thread.currentThread().isInterrupted()){
                String data = disReader.readUTF();
                st = new StringTokenizer(data);
                String action = st.nextToken();
                
                switch(action){
                    // get user text logs/activities of user and save to a text file
                    // calls saveLogFile of client class
                    // format: text_logs
                    case "userGetChatLogs":
                        String text_logs = "";
                        while(st.hasMoreTokens()){
                            text_logs = text_logs + " " + st.nextToken();
                        }
                        client.saveLogFile(text_logs);
                        break;
                    
                    // get msg from sender and append it to the receiver's chat screen
                    // format: sender 
                    case "userGetChat":
                        String msg = "";
                        String sender = st.nextToken();
                        
                        while(st.hasMoreTokens()){
                            msg = msg + " " + st.nextToken();
                        }
                        
                        client.appendReceivedMsg(msg, sender, Color.red, Color.darkGray);
                        break;
                    
                    // get current list of online user/s
                    // format: user
                    case "userOnline":
                        Vector online = new Vector();
                        while(st.hasMoreTokens()){
                            String list = st.nextToken();
                            
                            if(!list.equalsIgnoreCase(client.username)){
                                online.add(list);
                            }
                            
                        }
                        client.appendOnlineUsers(online);
                        break;
                    
                    // informs the client receiver in the file connection that there is a file to be sent
                    // receiver can accept to get the file or reject
                    // receiver can also select what directory to save the file
                    // format: sender_file receiver_file filename
                    case "userGetFile": 
                        String sender_file = st.nextToken();
                        String receiver_file = st.nextToken();
                        String filename = st.nextToken();
                        int confirm = JOptionPane.showConfirmDialog(client, sender_file + " wants to send you a file." + "\nFilename: " + filename + "\nAccept?");
   
                        // if receiver accepted the file, then establish file sharing socket and inform sender to send file 
                        // ask receiver what directory to download file by calling openFolder in client class
                        if(confirm == 0){ 
                            client.openFolder();
                            try {
                                dosWriter = new DataOutputStream(socket.getOutputStream());
                                String format = "sFileAccept " + sender_file + " " + receiver_file + " accepted the file!,";
                                dosWriter.writeUTF(format);
                              
                                Socket sender_socket = new Socket(client.getClientHost(), client.getClientPort());
                                DataOutputStream fdos = new DataOutputStream(sender_socket.getOutputStream());
                                fdos.writeUTF("userFileSocket "+ client.getClientUsername());
                                new Thread(new FileSharingReceiverThread(sender_socket, client)).start();
                            } 
                            catch (IOException e) {
                                System.out.println("[userGetFileError]: " + e.getMessage());
                            }
                        } 
                        
                        // if client receiver rejected the file, then send back result to sender
                        else { 
                            try {
                                dosWriter = new DataOutputStream(socket.getOutputStream());
                                String format = "sFileError " + sender_file + " Client didn't accept your file or there was a file sharing connection issue.,";
                                dosWriter.writeUTF(format);
                            } 
                            catch (IOException e) {
                                System.out.println("[userGetFile]: " + e.getMessage());
                            }
                        }                       
                        break;   
                        
                    default: 
                        client.appendReceivedMsg("[Error]: Unknown message "+ action, "Error", Color.RED, Color.RED);
                    break;
                }
            }
        } 
        catch(IOException e){
            client.appendReceivedMsg(" Server is now offline, please try chatting again later!", "Error", Color.RED, Color.RED);
        }
    }
}
