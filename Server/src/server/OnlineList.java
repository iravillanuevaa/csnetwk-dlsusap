package server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class OnlineList implements Runnable {
    
    Server server;
    
    public OnlineList (Server server){
        this.server = server;
    }
    
    /** Get current online users and inform client by writing action userOnline
     *  userOnline is called in ClientThread
     **/
    @Override
    public void run() {
        try {
            while(!Thread.interrupted()){
                String user = "";
                
                for (int x = 0; x < server.clientList.size(); x++){
                    user = user + " " + server.clientList.elementAt(x);
                }
                
                for (int x=0; x < server.socketList.size(); x++){
                    Socket online_thread_soc = (Socket) server.socketList.elementAt(x);
                    DataOutputStream dosWriter = new DataOutputStream(online_thread_soc.getOutputStream());
           
                    if(user.length() > 0){
                        dosWriter.writeUTF("userOnline " + user);
                    }
                }
                
                Thread.sleep(1900);
            }
        }
        catch(InterruptedException e){
            server.appendServerMessage("[InterruptedException]: " + e.getMessage() + ",");
        }
        catch (IOException e) {
            server.appendServerMessage("[IOException]: " + e.getMessage() + ",");
        } 
    }
}
