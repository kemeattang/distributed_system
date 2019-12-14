/*--------------------------------------------------------
1. Abasiekeme Attang

2. Java 

3. Instructions
> javac MyWebServer.java

4. Run Instructions
> java MyWebServer
> in browser run on localhost:2540

5. List of files needed for running the program.
 a. MyWebServer.java
 b. http-streams.txt
 c. serverlog.txt
 d. addNums.html
----------------------------------------------------------*/

import java.net.*;
import java.io.*;
import java.util.*;


class Worker extends Thread {  
  Socket sock;
  Worker(Socket s) {
    sock = s;
  }

  public void run() {
    PrintStream out = null;
    BufferedReader in = null;

    try {
      
       
       in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
       out = new PrintStream(sock.getOutputStream());

      String browserrequest = in.readLine();

      if (browserrequest == null || browserrequest.length() == 0) 
          System.out.println("files not found");

      else 
      {



        String fname = browserrequest.substring(4, browserrequest.length() - 9); 

        // implementing text/plain mimes type
        
        if (fname.endsWith(".java"))
        {
            System.out.println(fname);
            String contentType = "text/plain";
            BrowserOutput(fname, contentType, out);

          }
        
        else if (fname.endsWith(".txt"))
        {
            System.out.println(fname);
            String contentType = "text/plain";
            BrowserOutput(fname, contentType, out);

          }
        // implementing text/html mime types

        else if (fname.endsWith(".htm") || fname.endsWith(".html")   )
        {
            System.out.println(fname);
            String contentType = "text/html";  
            BrowserOutput(fname, contentType, out);
        
        }
          

        
        else if (fname.contains("cgi"))
        {
          System.out.println(fname);
          String contentType = "text/html";
          getAddNums(fname, contentType, out);
      
        }
        else if (fname.endsWith("/"))
        {
          System.out.println(fname);
          String contentType = "text/html";
          Header(fname, contentType, out);
        }

        else if (!fname.endsWith("/"))
        {
          System.out.println(fname);
          String contentType = "text/plain";
          BrowserOutput(fname, contentType, out);
        }
      }
  
    System.out.flush();
    sock.close();

    } catch(IOException x){
      System.out.println(x);
    }
  } 

   


   


  public void BrowserOutput (String fname, String contentType, PrintStream out ){
    final int DEFAULT_BUFFER_SIZE = 10024 * 4;                                             
    File file1 = new File(fname);              

    if (!fname.equals("/") && !file1.isFile()) {

      try {
        InputStream input = new FileInputStream(fname.substring(1, fname.length()));
        
        out.print("HTTP/1.1 200 OK");  
        out.print("Content-Length: " + 47);
        out.print("Content-type: "+ contentType + "\r\n\r\n");

        byte[] data = new byte[DEFAULT_BUFFER_SIZE];
        int Bytebuffer = input.read(data);
        out.write(data, 0, Bytebuffer);                                                             
        out.flush();                                                                                
        input.close();  

      } catch (IOException x){
        System.out.println(x);
      }
    }
  }

  public void Header(String fname, String contentType, PrintStream out){
    int ds = 0;
    out.print("HTTP/1.1 200 OK");     
    out.print("Content-Length: " + 47); 
    out.print("Content-type: " + contentType + "\r\n\r\n");

  
    File file2 = new File( "./"+ fname + "/");
    File [] file_string = file2.listFiles( ); 

    if (file_string != null)
    {
      for ( int i = 0 ; i < file_string.length ; i ++ ) 
      {
        if (file_string[i].isDirectory()) 
        {
          out.print("<a href=\"" + file_string[i].getName()  + "/\">/" + file_string[i].getName() + "/</a><br>");
        } 
        else if (file_string[i].isFile()) 
        {
          out.print("<a href=\"" + file_string[i].getName() + "\">" + file_string[i].getName() + "</a> (" + file_string[i].length() + ")<br>");

        }

      }
    }
  }


class getAddNums {

  int sum;
  String number1;

  String name;
  
  String number2;
  

}
  public void getAddNums (String fname, String contentType, PrintStream out)
  {
     
    String f = fname.substring(22, fname.length());
    String [] fs = f.split("[=&]");
    
    String number2 = fs[5];
    
    String number1 = fs[3];

    String name = fs[1]; 
    
     
    int sum = (Integer.valueOf(number1)) + (Integer.valueOf(number2));    

    
    String result  = "Dear "+ name +", the sum of "+ number1 +" and "+ number2 +" is "+ sum;
  
    out.print("HTTP/1.1 200 OK");
    out.print("Content-Length: " + 47);
    out.print("Content-type: "+ contentType + "\r\n\r\n");  
    out.print("<p>" + result + "</p>");

  } 

}
public class MyWebServer {

    public static boolean controlswitch = true;
  
    public static void main(String a[]) throws IOException {
      int q_len = 6;   
      int port = 2540; 
      Socket sock;
  
      ServerSocket servsock = new ServerSocket(port, q_len);  

      System.out.println("Abasiekeme Attang's WebServer starting up, listening at port "+ port +".\n");
      while (controlswitch) { 
        sock = servsock.accept();
        new Worker(sock).start(); 

      }
      if (controlswitch = false) { 
      servsock.close();
      }
    }
  
  }

  