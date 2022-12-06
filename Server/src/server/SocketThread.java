package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.StringTokenizer;

public class SocketThread implements Runnable{
    
    Server server;
    Socket socket;
    DataInputStream disReader;
    StringTokenizer st;
    String client, fs_username;
    
    private final int BUFFER_SIZE = 100;
    
    public SocketThread(Socket socket, Server main){
        this.server = main;
        this.socket = socket;
        
        try {
            disReader = new DataInputStream(socket.getInputStream());
        } 
        catch (IOException e) {
            main.appendServerMessage("[SocketThreadIOException]: " + e.getMessage());
        }
    }
    
    /** Get the client socket in client socket list
     * Establish a file sharing connection to send file **/
    private void createFileSharingConnection(String receiver, String sender, String filename){
        try {
            server.appendServerMessage("[FileSharing_Status]: Creating file sharing connection for " + sender + " to " + receiver  + "...,");
            Socket s = server.getClientList(receiver);
            
            // if client exists
            if(s != null){ 
                DataOutputStream dosWriter = new DataOutputStream(s.getOutputStream());
                String format = "userGetFile " + sender + " " + receiver + " " + filename;
                dosWriter.writeUTF(format);
                server.appendServerMessage("[FileSharing_Status]: File sharing connection established!,");
                server.appendServerMessage("[FileSharing_Status]: " + sender + " is now sending " + filename + " to " + receiver + "...," );
            }
            
            // if client doesn't exist, send back to sender that receiver was not found.
            else{
                server.appendServerMessage("[FileSharing_Status]: Client " + "'" + receiver + "' was not found");
                DataOutputStream dosWriter = new DataOutputStream(socket.getOutputStream());
                dosWriter.writeUTF("sFileError " + "Client '" + receiver + "' was not found in the list, check the online list and try again!");
            }
        } 
        catch (IOException e) {
            server.appendServerMessage("[FileSharing_Status]: "+ e.getLocalizedMessage());
        }
    }
    
    @Override
    public void run() {
        try {
            while(true){
                String data = disReader.readUTF();
                st = new StringTokenizer(data);
                String action = st.nextToken();
                
                switch(action){
                    // return text log activities of user who will be logging out
                    // format: user_exit
                     case "userChatLogs":
                        String user_exit = st.nextToken();
                        System.out.println(user_exit);
                        Socket user_exit_thread = server.getClientList(user_exit);
                        DataOutputStream dosWriterCL = new DataOutputStream(user_exit_thread.getOutputStream());
                        String text_logs = server.getServerMessage();
                        dosWriterCL.writeUTF("userGetChatLogs " + text_logs);
                        break;
                        
                    // set user to client list, set socket to socket list, and append to server msg client status
                    // format: username
                    case "userJoin":
                        String username = st.nextToken();
                        client = username;
                        server.setClientList(username);
                        server.setSocketList(socket);
                        server.appendServerMessage("[Client]: "+ username +" joins the chatroom!,");
                        break;
                    
                    // append chat activity log to server and append sender msg to receiver client
                    // format: from_sender sendTo_receiver msg
                    case "userPostChat":
                        String from_sender = st.nextToken();
                        String sendTo_receiver = st.nextToken();
                        String msg = "";
                        
                        while(st.hasMoreTokens()){
                            msg = msg +" "+ st.nextToken();
                        }
                        
                        Socket thread_soc = server.getClientList(sendTo_receiver);
                        
                        try {
                            DataOutputStream dos = new DataOutputStream(thread_soc.getOutputStream());
                            String content = from_sender + " " + msg;
                            dos.writeUTF("userGetChat " + content);
                            server.appendServerMessage("["+ from_sender +" to "+ sendTo_receiver + "] : " + msg + ",");
                        } 
                        catch (IOException e) {  
                            server.appendServerMessage("[IOException]: Unable to send message to " + sendTo_receiver + ","); 
                        }
                        break;
                   
                    // set filesharing host and file sharing socket using filesharing_username
                    // format: filesharing_username 
                    case "userFileSocket":
                        String filesharing_username = st.nextToken();
                        fs_username = filesharing_username;
                        server.setClientFileSharingUsername(filesharing_username);
                        server.setClientFileSharingSocket(socket);
                        break;
                    
                    // get file sharing socket of receiver and checks if the user exists
                    // if it exists, proceed to sending file
                    // if error occurs while sending file, send a sFileError
                    // after transfer, remove sender and receiver in the file sharing socket
                    // format: ssendFile file_name file_size receiver sender
                    case "sSendFile":
                        String file_name = st.nextToken();
                        String file_size = st.nextToken();
                        String receiver = st.nextToken();
                        String sender = st.nextToken();
                    
                        Socket cSock = server.getClientFileSharingSocket(receiver); 
                        if(cSock != null){
                            try {
                                DataOutputStream dosWriterSF = new DataOutputStream(cSock.getOutputStream());
                                dosWriterSF.writeUTF("sSendFile "+ file_name + " " + file_size + " " + receiver);                             
                                InputStream input = socket.getInputStream();
                                OutputStream sendFile = cSock.getOutputStream();
                                byte[] buffer = new byte[BUFFER_SIZE];
                                int count;
                                
                                while((count = input.read(buffer)) > 0){
                                    sendFile.write(buffer, 0, count);
                                }
                                
                                sendFile.flush();
                                sendFile.close();
                                server.removeClientFileSharing(sender);
                                server.removeClientFileSharing(receiver);
                                server.appendServerMessage("[FileSharing_Status] : File was successfully sent to " + receiver + "!,");
                            } 
                            catch (IOException e) {
                                server.appendServerMessage("[FileSharing_Status] : " + e.getMessage() + ",");
                            }
                        }
                        else{ 
                            server.removeClientFileSharing(receiver);
                            server.appendServerMessage("[FileSharing_Status] : Client '"+ receiver +"' was not found!,");
                            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                            dos.writeUTF("sFileError " + "Client '" + receiver + "' was not found, File Sharing connection will now be terminated...,");
                        }                        
                        break;
                        
                    // send status of file that was sent
                    // if there was an error, write file status action to receiver
                    // format: receiver_name fstatus_msg
                    case "sFileStatus":
                        String receiver_name = st.nextToken(); 
                        String fstatus_msg = ""; 
         
                        while(st.hasMoreTokens()){
                            fstatus_msg = fstatus_msg + " " + st.nextToken();
                        }
                        
                        try {
                            Socket rSock = (Socket) server.getClientFileSharingSocket(receiver_name);
                            DataOutputStream rDos = new DataOutputStream(rSock.getOutputStream());
                            rDos.writeUTF("sFileStatus" + " " + receiver_name + " " + fstatus_msg);
                        } 
                        catch (IOException e) {
                            server.appendServerMessage("[sFileStatus]: "+ e.getMessage() + ",");
                        }
                        break;
                        
                    // creates file sharing connection with sender, receiver, and filename  
                    // format: file_sender file_receiver file_filename
                    case "userFileConnection":                      
                        try {
                            String file_sender = st.nextToken();
                            String file_receiver = st.nextToken();
                            String file_filename = st.nextToken();
                             
                            this.createFileSharingConnection(file_receiver, file_sender, file_filename);
                        } 
                        catch (Exception e) {
                            server.appendServerMessage("[FileSharing_Status]: "+ e.getLocalizedMessage() + ",");
                        }
                        break;
                        
                    // Get the file sharing host socket for connection to send error msg from receiver 
                    // error msg: Client didn't accept your file or there was a file sharing connection issue :(
                    // format: f_receiver ferror_msg
                    case "sFileError": 
                        String f_receiver = st.nextToken();
                        String ferror_msg = "";
                        while(st.hasMoreTokens()){
                            ferror_msg = ferror_msg + " " + st.nextToken();
                        }
                        try {
                            Socket eSock = server.getClientFileSharingSocket(f_receiver); // 
                            DataOutputStream eDos = new DataOutputStream(eSock.getOutputStream());
                            eDos.writeUTF("rFileError "+ ferror_msg);
                            server.appendServerMessage("[FileSharing_Status]: " + ferror_msg + ",");
                        } 
                        catch (IOException e) {
                            server.appendServerMessage("[rFileError]: "+ e.getMessage() + ",");
                        }
                        break;
                        
                    // called when receiver accepts sent file
                    // get the file sharing receiver socket for connection 
                    // Format: sFileAccept fa_sender fa_receiver accept_msg
                    case "sFileAccept": 
                        String fa_sender = st.nextToken();
                        String fa_receiver = st.nextToken();
                        String accept_msg = "";
                        while(st.hasMoreTokens()){
                            accept_msg = accept_msg + " " + st.nextToken();
                        }
                        try {
                            Socket aSock = server.getClientFileSharingSocket(fa_sender); // 
                            DataOutputStream aDos = new DataOutputStream(aSock.getOutputStream());
                            server.appendServerMessage("[FileSharing_Status]: " + fa_receiver + accept_msg + ",");
                            aDos.writeUTF("rFileAccept "+ accept_msg);
                        } 
                        catch (IOException e) {
                            server.appendServerMessage("[rFileError]: "+ e.getMessage() + ",");
                        }
                        break;
                        
                        
                    default: 
                        server.appendServerMessage("[Error]: Unknown message "+ action + ",");
                    break;
                }
            }
        } 
        
         /* Removes client to client list if it log outs or it doesn't exists*/
        catch (IOException e) {
            server.removeClient(client);
            if(fs_username != null){
                server.removeClientFileSharing(fs_username);
            }
        }
    }
    
}
