
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Consumer {
    private static final String sensorName = "Consumer";
    private static final String sensorType = "Consumer";
    protected Random rand;
    private static int Demand;
    public static boolean running = true;
    public static Consumer con ;
    public static ConsumerControlHandler handler;
    public static ConsumerControl.ConsumerControl.Processor processor;
    public static Runnable server;
    protected static MqttClient mqttClient;
    public static void main(String[] args) throws Exception {
        RPCinit();
        mqttClient = new MqttClient("tcp://broker.hivemq.com:1883", MqttClient.generateClientId());
        connectMqtt();
        server = new Runnable() {
            @Override
            public void run() {
//                UDPClient udpClient = null;
//                try {
//                    udpClient = new UDPClient();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                System.out.println("Consumer started");
                while (running){
                    getDataUpdate();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String DemandText = String.valueOf(Demand);

                    JSONObject jsonMessage = new JSONObject();
                    jsonMessage.put("SensorType", sensorType);
                    jsonMessage.put("SensorName", sensorName);
                    jsonMessage.put("Power", DemandText + " Kw");
                    /*
                     * Convert Jsonobject to string then to mqttMessage
                     * Publish the message to the Broker
                     * */
                    MqttMessage mqttMessage = new MqttMessage(jsonMessage.toString().getBytes());
                    try {
                        mqttClient.publish("Sensor/" + sensorName , mqttMessage);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Message sent with payload: " +jsonMessage.toString() );
//                    udpClient.sendMsg(jsonMessage);
                }
            }
        };
        new Thread(server).start();
    }
    /*
    Initialize the RPC Thrift server
     */
    public static void RPCinit() throws InterruptedException {
        con = new Consumer();
        Thread.sleep(3000);
        try {
            handler = new ConsumerControlHandler(con);
            processor = new ConsumerControl.ConsumerControl.Processor(handler);

            Runnable startThrift = new Runnable() {
                public void run() {
                    startThrift(processor);
                }
            };
            new Thread(startThrift).start();
        }catch (Exception e) {
            System.out.println("Can not start RPC" + e.getMessage());
        }
    }

    private static void getDataUpdate() {
        int rand = ThreadLocalRandom.current().nextInt(10000,15000);
        Demand = rand;
    }
    //Thrift Methods
    public static void startThrift(ConsumerControl.ConsumerControl.Processor processor) {
        try {
            TServerTransport serverTransport = new TServerSocket(1234);
            TServer server = new TSimpleServer(new TServer.Args(serverTransport).processor(processor));

            // Use this for a multithreaded server
            // TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

            System.out.println("Starting the Thrift Service on port 1234...");
            server.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void stop() throws InterruptedException {
        running = false;
    }
    public void start() throws Exception {
        running = true;
        new Thread(server).start();
    }
    public static void connectMqtt() throws MqttException {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        mqttClient.connect(options);
    }
}
