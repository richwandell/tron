import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.Point;
import java.awt.event.*;


class TronServer implements Runnable{
  //public static void main(String args[]){new TronServer();}
  
  
  private DatagramSocket broadcast; 
  
  private int connections = 0;
  private int gamePort = 2001;
  private int dataPort = 2002;
  
  private String os = System.getProperty("os.name").toLowerCase();
  
  private boolean busy = false;
  
  private Socket myS, dataS;
  
  private LinkedList<LinkedList<Point>> points = new LinkedList<LinkedList<Point>>();
  private LinkedList<String> address = new LinkedList<String>();
  private LinkedList<String> players = new LinkedList<String>();
  private LinkedList<String> scores = new LinkedList<String>();
  
  
  
  
  public void run(){
    points.add(new LinkedList<Point>());
    points.add(new LinkedList<Point>());
    points.add(new LinkedList<Point>());
    points.add(new LinkedList<Point>());
    new Thread(new BroadcastMessage()).start();
    new Thread(new DataSocket()).start();
    
    System.out.println(os);
    
    //loop to accept a connection
    while(true){   
      
      try{   
        
        // create socket
        ServerSocket serversocket = new ServerSocket(gamePort);
        System.out.println("Waiting for player number: "+(connections+1));
        myS = new Socket();
        myS = serversocket.accept();
        
        //itterate connections variable
        connections++;
        
        System.out.println("connection accepted");
        
        
        //read remote address
        InetAddress remoteAddress = myS.getInetAddress();
        byte[] ipAddr = remoteAddress.getAddress();
        String remoteIP = ipAddr[0]+"."+ipAddr[1]+"."+ipAddr[2]+"."+ipAddr[3]; 
        
        
        address.add(remoteIP);
        players.add(remoteIP);
        
        
        scores.add("0");
        
        //start new thread to listen for this clients point updates, sending it the socket pointer and its player number
        new Thread(new SocketThread(myS, connections)).start();
        
        //print any exceptions
      }catch(Exception e){ }
    }
  }//end constructor 
  
  
 
  class DataSocket implements Runnable{
    public void run(){
      while(true){
        try{
          ServerSocket ss = new ServerSocket(dataPort);
          dataS = new Socket();
          dataS = ss.accept();
          
          ObjectOutputStream oos = new ObjectOutputStream(dataS.getOutputStream());
          oos.writeObject(players);
          oos.writeObject(scores);
          
        }catch(Exception e){}
        
      }
    }
  }
  
  
  class BroadcastMessage implements Runnable{
    public void run(){
      while(true){
        try{
          Thread.sleep(5000); 
          broadcast = new DatagramSocket();
          InetAddress addr = InetAddress.getLocalHost();
          
          byte[] buf = addr.getAddress();
          
          InetAddress group = InetAddress.getByName("224.0.0.251");
          DatagramPacket packet = new DatagramPacket(buf, buf.length, group, 4446);
          broadcast.send(packet);
          
        }catch(Exception e){}
      }    
    }
  }
 
  
  class SocketThread implements Runnable{
    private Socket localSocket;
    private int player;
    
    private boolean firstTime = true;
    
    public SocketThread(Socket rs, int c)
    {
      localSocket = rs;
      player = c;
      
    }//constructor
    
    public void run(){           
        try{
          while(true){ 
          ObjectOutputStream oos = new ObjectOutputStream(localSocket.getOutputStream());
          oos.writeObject(points);
          
          
         ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(localSocket.getInputStream()));
         points.set(player-1, (LinkedList<Point>)ois.readObject());
          }
        }catch(Exception e){System.out.println("Error communicating with client: "+e);} 
      
    }//run
  }  
}
  
  

  
