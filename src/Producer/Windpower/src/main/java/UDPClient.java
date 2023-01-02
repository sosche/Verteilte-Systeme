//import org.json.JSONObject;
//
//import java.io.IOException;
//import java.net.*;
//import java.util.HashMap;
//import java.util.Random;
//
//public class UDPClient {
//    protected int port;
//    private InetAddress address;
//    protected DatagramPacket dp;
//    protected DatagramSocket ds;
//    protected Random rand;
//    protected byte[] buffer;
//    protected int id;
//    private String sensorName, sensorType, sensorPower;
//
//    public UDPClient() throws Exception{
//        String server = System.getenv("DESTINATION");
//        if(server == null) server = "localhost";
//        try {
//            address = InetAddress.getByName(server);
//        } catch (UnknownHostException e) {
//            System.out.println("Can not parse the destination host address.\\n{}" + e.getMessage());
//            System.exit(1);
//        }
//
//    }
//
//    public void sendMsg(JSONObject msg) {
//
//        // Create the UDP datagram socket.
//        try (DatagramSocket udpSocket = new DatagramSocket()) {
//            System.out.println("Started the UDP socket that connects to " + address.getHostAddress());
//
//            // Convert the message into a byte-array.
//            byte[] buf = msg.toString().getBytes();
//            // Create a new UDP packet with the byte-array as payload.
//            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 6543);
//
//            // Send the data.
//            udpSocket.send(packet);
//            sensorName = msg.get("SensorName").toString();
//            sensorType = msg.get("SensorType").toString();
//            sensorPower = msg.get("Power").toString();
//            System.out.println("Message sent with payload:  " + "Sensor Type: " + sensorType + ", Sensor Name: " + sensorName + ", Sensor Power: " + sensorPower
//                    + ", Address: " + address + ", Port: 6543");
//        } catch (SocketException e) {
//            System.out.println("Could not start the UDP socket server.\n{}" +  e.getLocalizedMessage());
//        } catch (IOException e) {
//            System.out.println("Could not send data.\n{}" + e);
//        }
//    }
//
//}