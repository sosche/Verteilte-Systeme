package Central;

import Central.Restdata;
import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;


public class HttpServer implements Runnable {
    Central c = new Central();
    public Restdata restdata;
    static public final int S_port = 8080;
    static final String newline = "\r\n";
    private volatile boolean running;

    public HttpServer(){
        restdata = new Restdata();
        running = true;
    }

    public void stopRunning() {
        running = false;
    }

    public void fromJson (JSONObject jsonobj) throws Exception {
        restdata.fromJson(jsonobj);
    }

    public String receive (String type) throws Exception {
        return restdata.giveString(type);
    }


    @Override
    public void run() {
        try {
            System.out.println("HTTP Thread started");
            ServerSocket serverSocket = new ServerSocket(S_port);
            while (running){
                Socket connection = serverSocket.accept();
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    OutputStream out = new BufferedOutputStream(connection.getOutputStream());
                    PrintStream pout = new PrintStream(out);
                    System.out.println("Connected with HTTP client");
                    //read first line of request
                    String request = in.readLine();
                    if (request == null) {
                        continue;
                    }
                    while (request.length() < "GET /sensors/all HTTP/1.1".length()) {
                        System.out.println("Re-enter URI");
                        request = in.readLine();
                    }
                    String type = request.substring("GET /sensors/".length(), request.length() - 9);
                    // we ignore the rest
                    while (true) {
                        String ignore=in.readLine();
                        if (ignore==null || ignore.length()==0)
                            break;
                    }
                    if (!request.startsWith("GET ") || !(request.endsWith(" HTTP/1.0")
                            || request.endsWith(" HTTP/1.1")) || !request.contains("/sensors/") ||
                            !type.matches("Consumer|Windpower|Solarpower|Nuclearpower|History|all")) {
                        // bad request
                        pout.print("HTTP/1.0 400 Bad Request"+newline+newline);
                    } else {
                        String received = receive(type) ;
                    pout.print(
                            "HTTP/1.0 200 OK"+newline+
                                    "Content-Type: text/plain"+newline+
                                    "Date: "+new Date()+newline+
                                    "Content-length: " + received.length() + newline+newline+
                                    received);
                    }
                    pout.flush();
                    System.out.println("Message Sent");

                } catch (Exception e){
                    e.printStackTrace();
                }
            }

        } catch (Throwable tr) {
            System.err.println("could not start Server: " + tr);
        }
    }
}
