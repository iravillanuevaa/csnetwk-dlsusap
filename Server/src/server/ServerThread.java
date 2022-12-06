
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread implements Runnable {
    
    ServerSocket server_socket;
    Server server;
    boolean online = true;
    
    /** When server thread is instantiated, append log activity/server status **/
    public ServerThread(int port, Server server){
        server.appendServerMessage("[Server]: Connecting server in port "+ port + "...,");
        try {
            this.server = server;
            server_socket = new ServerSocket(port);
            server.appendServerMessage("[Server]: Server is now online!" + ",");
        } 
        catch (IOException e) { 
            server.appendServerMessage("[ServerThreadIOException]: "+ e.getMessage() + ","); 
        }
    }

    @Override
    public void run() {
        try {
            // continue listening for connections/users and instantiate socket thread
            while(online){
                Socket socket = server_socket.accept();
                new Thread(new SocketThread(socket, server)).start();
            }
        } 
        catch (IOException e) {
            server.appendServerMessage("[ServerThreadIOException]: "+ e.getMessage() + ",");
        }
    }
    
    public void stop(){
        try {
            server_socket.close();
            online = false;
            System.exit(0);
        } 
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    
}
