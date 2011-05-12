import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import javax.swing.event.*;
import java.lang.Thread;

class TronClient extends JFrame implements ActionListener, KeyListener, MouseListener{
  public static void main(String args[]){new TronClient();}
  //global variables
  private TronServer server;
  
  private Color color;
  
  private javax.swing.Timer soundLoop = new javax.swing.Timer(64800, this);
  private javax.swing.Timer move = new javax.swing.Timer(70, this);
  
  private boolean connected = false;
  private boolean readyToConnect = false;
  private boolean dead = false;
  
  private int port = 2001;
  private int dataPort = 2002;
  private int timeOut = 10000;
  private int score = 0;
  private int width, height;
  private int player;
  private int x, y;
  private int winner = 0;
  
  private String host = "127.0.0.1";
  private String otherPlayers;
  private String myName = "";
  private String[] tColumns = {"Choose a server"};
  private String[][] tableInfo = {{"Looking for games..."}, {""}};
  private String direction = "u";
  
  private Socket myS;
  
  private LinkedList<String> availableGames = new LinkedList<String>();
  private LinkedList<Point> myPoints = new LinkedList<Point>();
  private LinkedList<LinkedList<Point>> allPoints = new LinkedList<LinkedList<Point>>();
  
  private Toolkit toolkit = Toolkit.getDefaultToolkit();
  
  private TronFrame window;
  
  private GameMenu menu;

  private Random random = new Random();
  
  private JTable games = new JTable(20, 1);
  private JTable players;
  
  private JPanel mainPanel = new JPanel();
  private JPanel leftPanel = new JPanel();
  private JPanel rightPanel = new JPanel();
  
  private JButton b = new JButton("Connect");
  private JButton b1 = new JButton("Start Server");
  
  private JTextField name = new JTextField(20);
  
  private JScrollPane playersPane = new JScrollPane();
  private JScrollPane gamesPane = new JScrollPane();
  
  private Random r = new Random();
  
  
  //constructor
  public TronClient(){
    color = new Color(r.nextInt(255), r.nextInt(255),r.nextInt(255));
    allPoints.add(new LinkedList<Point>());
    allPoints.add(new LinkedList<Point>());
    allPoints.add(new LinkedList<Point>());
    allPoints.add(new LinkedList<Point>());
    
    try{
      System.setProperty("apple.laf.useScreenMenuBar", "true");
      System.setProperty ("com.apple.macos.useScreenMenuBar","true");
      System.setProperty("com.apple.mrj.application.apple.menu.about.name", "TRON");
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }catch(Exception e){System.out.println(e);}
    
    
    
    
    new AePlayWave("media/daft_punk.wav").start();
    soundLoop.start();
    
    this.setSize(500,500);
    
    width = this.getWidth();
    height = this.getHeight();  
    window = new TronFrame();
    menu = new GameMenu();
    new Thread(new SearchForGames()).start();
    
    name.setBorder(BorderFactory.createTitledBorder("My name"));
    
    games = new JTable(tableInfo, tColumns);
    gamesPane.add(games); 
    gamesPane.setPreferredSize(new Dimension(200, 100));
    gamesPane.setBorder(BorderFactory.createTitledBorder("Games"));
    
    players = new JTable(20,2);
    playersPane.setBorder(BorderFactory.createTitledBorder("Players"));   
    playersPane.add(players);
    playersPane.setPreferredSize(new Dimension(200, 400));
    
    leftPanel.add(playersPane);
    b.addMouseListener(this);
    b1.addMouseListener(this);
    rightPanel.add(name);
    rightPanel.add(gamesPane);
    rightPanel.add(b);  
    rightPanel.add(b1);
    this.setLayout(new GridLayout(1, 2));
    this.add(leftPanel);
    this.add(rightPanel);
    
    
    
    this.setVisible(true);   
    
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    this.setLocationRelativeTo(null);
    this.setMinimumSize(this.getSize());
    
    this.setFocusable(true);
    this.addKeyListener(this);
    
    
    
  }
  
  /*******************************
    * begin coding of game methods*
    *******************************/
  
  public void goGame(){
    this.add(window);
    menu.setVisible(false);
    this.remove(menu);
    this.setLayout(new GridLayout(1,1));
    
    window.setVisible(false);
    window.setVisible(true);
    window.repaint();
    window.setFocusable(true);
    window.addKeyListener(this);
    
    move.start(); 
    new Thread(new UpdatePoints()).start();
    
  }
  
  //change to menu from info panel
  private void changeToMenu(){
    leftPanel.setVisible(false);
    rightPanel.setVisible(false);
    this.remove(leftPanel);
    this.remove(rightPanel);
    this.setLayout(new GridLayout(1,1));
    this.add(menu);  
    new Thread(new ConnectToServer()).start();
  }
  
 
  
  /**************************************************
    *begin coding event handlers for tronclient class*
    **************************************************/
  
  
  
  public void mouseMoved(MouseEvent e){}
  public void mouseEntered(MouseEvent e){}
  public void mouseExited(MouseEvent e){}
  public void mousePressed(MouseEvent e){}
  public void mouseReleased(MouseEvent e){}
  public void mouseClicked(MouseEvent e){
    
    if(e.getSource() == b){
    changeToMenu();
    myName = name.getText();
    readyToConnect = true; 
    }
    
    else if(e.getSource() == b1){
     b.setEnabled(false);
     b1.setEnabled(false);
     new Thread(new TronServer()).start();
    }
    
    
    
  }
  public void keyTyped(KeyEvent e){}
  public void keyPressed(KeyEvent e){
    
    if(e.getKeyCode() == e.VK_LEFT){if(direction.equals("r")){}else{direction = "l";}}
    else if(e.getKeyCode() == e.VK_RIGHT){if(direction.equals("l")){}else{direction = "r";}}
    else if(e.getKeyCode() == e.VK_UP){if(direction.equals("d")){}else{direction = "u";}}
    else if(e.getKeyCode() == e.VK_DOWN){if(direction.equals("u")){}else{direction = "d";}}
    
  }
  public void keyReleased(KeyEvent e){}
  
  
  
  
  
  public void actionPerformed(ActionEvent e){
    if(e.getSource()==soundLoop){new AePlayWave("media/daft_punk.wav").start();}
    else if(e.getSource()==move){
      
      
      if(direction.equals("u")){y--;}
      else if(direction.equals("d")){y++;}
      else if(direction.equals("r")){x++;}
      else{x--;}
      
      
      window.repaint();
      
      myPoints.add(new Point(x, y));
      
      
      if(x >= 100){x=0;}
      else if(x<=0){x=100;}
      else if(y>=100){y=0;}
      else if(y<=0){y=100;}
      
      //collision detection
      
      for(int a = 0; a<4; a++){
      if(allPoints.get(a).contains(new Point(x, y))){winner = a; dead = true;}
      }

      
      
    }
  }
  
  /*****************************************
   *begin coding of the game splash screen *
   *****************************************/
  
  public class GameMenu extends JPanel implements ActionListener{
    int t = 0;
    javax.swing.Timer time = new javax.swing.Timer(1000, this);
    public void actionPerformed(ActionEvent e){
      t++;
      
    }
    
    Image tron1 = toolkit.getImage("media/tron1.jpg");
    Image tron2 = toolkit.getImage("media/tron2.jpg");
    Image loading = toolkit.getImage("media/loading.gif");
    GameMenu(){
      time.start();
      
    }
    
    public void paintComponent(Graphics g){
      super.paintComponent(g); 
      if(t<10){g.drawImage(tron1, 0,0, width, height, this);}
      else if(t<20){g.drawImage(tron2, 0,0, width, height, this);}
      else{t=0;}
      g.drawImage(loading, (width/2)-50, (height/2)-30, this);
      
    }
  }
  
  /**************************************
    * begin coding of the tron game panel*
    **************************************/
  
  public class TronFrame extends JPanel{
    Image cycle_up = toolkit.getImage("media/cycle_up.gif");
    Image cycle_right = toolkit.getImage("media/cycle_right.gif");
    Image cycle_down = toolkit.getImage("media/cycle_down.gif");
    Image cycle_left = toolkit.getImage("media/cycle_left.gif");
    public void paintComponent(Graphics g){
      super.paintComponent(g); 
      this.setBackground(new Color(0,100,200));
      
      for(int a = 0; a<50; a++){
       g.drawLine((width/50)*a, 0,  (width/50)*a, 500);
       g.drawLine(0, (height/50)*a, 500, (height/50)*a);
      }
      
      
      for(int a = 0; a<4; a++){
        for(int b = 0; b<allPoints.get(a).size(); b++){         
        g.fill3DRect((width/100)*allPoints.get(a).get(b).x, (height/100)*allPoints.get(a).get(b).y, (width/100), (height/100), true);
        }
      }
      
      g.setColor(color);
      for(int a = 0; a<myPoints.size(); a++){         
        g.fill3DRect((width/100)*myPoints.get(a).x, (height/100)*myPoints.get(a).y, (width/100), (height/100), true);
        }
      
      if(direction.equals("u")){g.drawImage(cycle_up, (width/100)*myPoints.getLast().x, (height/100)*myPoints.getLast().y, (width/100), (height/100), this);}
      else if(direction.equals("r")){g.drawImage(cycle_right, (width/100)*myPoints.getLast().x, (height/100)*myPoints.getLast().y, (width/100), (height/100), this);}
      else if(direction.equals("d")){g.drawImage(cycle_down, (width/100)*myPoints.getLast().x, (height/100)*myPoints.getLast().y, (width/100), (height/100), this);}
      else{g.drawImage(cycle_left, (width/100)*myPoints.getLast().x, (height/100)*myPoints.getLast().y, (width/100), (height/100), this);}
    }
  }
  
  /**********************************
    * begin background process coding*
    **********************************/
  
  //thread used to search for servers that are broadcasting
  public class SearchForGames implements Runnable, ListSelectionListener{
    
    public void run(){
      try{
        byte[] b = new byte[1024];
        DatagramPacket dgram = new DatagramPacket(b, b.length);
        
        MulticastSocket socket = new MulticastSocket(4446); // must bind receive side
        socket.joinGroup(InetAddress.getByName("224.0.0.251"));
        
        while(!connected) {
          
          socket.receive(dgram); // blocks until a datagram is received
          System.out.println("Received " + dgram.getLength() + " bytes from " + dgram.getAddress());
          dgram.setLength(b.length); // must reset length field!
          
          String s = b[0]+"."+b[1]+"."+b[2]+"."+b[3]; ;
          System.out.println(s);
          
          if(!availableGames.contains(s)){
            availableGames.add(s); 
          }
          String[][] g = new String[availableGames.size()][1];
          
          for(int a = 0; a<availableGames.size();a++){
            g[a][0] = availableGames.get(a);           
          }
          
          games = new JTable(g, tColumns);
          games.getSelectionModel().addListSelectionListener(this);
          gamesPane.getViewport().add(games);
          
          
        } 
      }catch(Exception e){System.out.println(e);}
      
    }
    public void valueChanged(ListSelectionEvent e){
      host = (String)games.getValueAt(e.getFirstIndex(), 0);
      
      try{
        
        Socket dataS = new Socket();
        dataS.connect(new InetSocketAddress(host, dataPort), timeOut);
        
        ObjectInputStream ois = new ObjectInputStream(dataS.getInputStream());
        LinkedList<String> playersList = (LinkedList<String>)ois.readObject();
        LinkedList<String> scoresList = (LinkedList<String>)ois.readObject();
        ois.close();
        
        System.out.println(playersList.size());
        
        String[] cols = {"Player", "Score"};
        String[][] ps = new String[playersList.size()][2];
        
        
        for(int a = 0; a<playersList.size(); a++){
          ps[a][0] = playersList.get(a);
          ps[a][1] = scoresList.get(a);
        }
        
        players = new JTable(ps, cols);
        playersPane.getViewport().add(players);
        
      }catch(Exception ex){System.out.println(ex);}
    } 
  }
  
  public class UpdatePoints implements Runnable{
    public void run(){
      while(true){
        try{
          //send my current point to the server
          ObjectOutputStream oos = new ObjectOutputStream(myS.getOutputStream());
          oos.writeObject(!dead ? myPoints : new LinkedList());
          
          if(dead){move.stop(); break;}
          
          //read an array of points from the server
        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(myS.getInputStream()));
        allPoints = (LinkedList<LinkedList<Point>>)ois.readObject();
        allPoints.add(myPoints);  
        }
        catch(Exception ex){System.out.println(ex);} 
      }
    } 
  }
  
  //thread used to send data to the server
  public class ConnectToServer implements Runnable{
    public void run(){while(!connected){if(readyToConnect){connect();}}}
    
    public void connect(){
      try{
        Thread.sleep(3000);
        myS = new Socket();
        
        //connect to server
        myS.connect(new InetSocketAddress(host, port), timeOut);
        connected = true;
        //print connected message
        System.out.println("Connected!");
        
        
        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(myS.getInputStream()));
        allPoints = (LinkedList<LinkedList<Point>>)ois.readObject();
        
        
        int player = 0;
        if(allPoints.get(0).size() == 0){player = 1;}
        else if(allPoints.get(1).size() == 0){player = 2;}
        else if(allPoints.get(2).size() == 0){player = 3;}
        else if(allPoints.get(3).size() == 0){player = 4;} 
        
        
        System.out.println("You are player number: "+player);
        
        //set your position and direction
        switch(player){
          case 1:{x = 50; y = 100; direction="u"; break;}
          case 2:{x = 0; y = 50; direction="r"; break;}
          case 3:{x = 100; y = 50; direction="l"; break;}
          case 4:{x = 50; y = 0; direction="d"; break;}
        }
        
        //add new point        
        myPoints.add(new Point(x, y));
        allPoints.add(myPoints);
        
        //start game
        goGame();
        
      }catch(Exception e){System.out.println(e);}
    }
    
  }
  
  
  
  
}