//package Central;
//
//import org.json.JSONObject;
//
//import java.io.IOException;
//import java.net.DatagramPacket;
//import java.net.DatagramSocket;
//import java.net.InetAddress;
//import java.net.SocketException;
//import java.util.Arrays;
//
//
//public class UDPServer implements Runnable {
//    Central c = new Central();
//    private DatagramSocket socket;
//    private boolean running ;
//    private byte[] buf = new byte[256];
//    private String sensorType, sensorName, sensorPower;
//
//    public UDPServer() throws SocketException {
//        socket = new DatagramSocket(6543);
//    }
//    @Override
//    public void run() {
//        running = true;
//        System.out.println("Started the UDP socket server at port 6543 with buffer size 256");
//        while (running) {
//            DatagramPacket packet
//                    = new DatagramPacket(buf, buf.length);
//            try {
//                socket.receive(packet);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            try {
//                sendDataToHttp(packet);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            printPacketData(packet);
//
//        }
//    }
//
//    private void sendDataToHttp (DatagramPacket udpPacket) throws Exception {
//        // Get IP address and port.
//        InetAddress address = udpPacket.getAddress();
//        int port = udpPacket.getPort();
//        // Get packet length.
//        int length = udpPacket.getLength();
//        String receivedPayload = new String(udpPacket.getData(),0,length);
//        // converts payload to JSON file
//        JSONObject jsonObj = new JSONObject(receivedPayload);
//        jsonObj.put("Address", address);
//        jsonObj.put("Port", port);
//        c.process(jsonObj);
//
//    }
//
//    public void printPacketData(DatagramPacket udpPacket)  {
//        // Get IP address and port.
//        InetAddress address = udpPacket.getAddress();
//        int port = udpPacket.getPort();
//        // Get packet length.
//        int length = udpPacket.getLength();
//
//        String adrStr = address.toString();
//        // Get the payload from the buffer. Mind the buffer size and the packet length!
//        //byte[] playload = Arrays.copyOfRange(udpPacket.getData(), 0, length);
//        String receivedPayload = new String(udpPacket.getData(),0,length);
//        JSONObject jsonObj = new JSONObject(receivedPayload);
//        sensorName = jsonObj.get("SensorName").toString();
//        sensorType = jsonObj.get("SensorType").toString();
//        sensorPower = jsonObj.get("Power").toString();
//        // Print the packet information.
//        System.out.println("Received a packet: IP:Port: " + address + ":" + port +
//                ", length: " + length + ", Sensor Type: " + sensorType + ", Sensor Name: " + sensorName + ", Sensor Power: " + sensorPower);
//    }
//
//    /**
//     * save Data to File
//     */
//    public void writeToFile() {
//    }
//}