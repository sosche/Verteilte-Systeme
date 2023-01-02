import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Windpower {
    private static final String sensorName = "Windpower";
    private static final String sensorType = "Generator";
    private static int Power;
    public static boolean running = true;
    public static Windpower wind ;
    public static WindControlHandler handler;
    public static WindControl.WindControl.Processor processor;
    public static Runnable server;
    protected static MqttClient mqttClient;
    public static void main(String[] args) throws Exception {
        RPCinit();
        mqttClient = new MqttClient("tcp://broker.hivemq.com:1883", MqttClient.generateClientId());
        connectMqtt();

        /*
        Start the component
         */
        server = new Runnable() {
            @Override
            public void run() {
////                UDPClient udpClient = null;
//                try {
////                    udpClient = new UDPClient();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                System.out.println("Wind started");
                while (running){
                    getDataUpdate();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String powerText = String.valueOf(Power);
                    JSONObject jsonMessage = new JSONObject();
                    jsonMessage.put("SensorType", sensorType);
                    jsonMessage.put("SensorName", sensorName);
                    jsonMessage.put("Power", powerText + " Kw");
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
    private static boolean simulateWind() {
    int randomWInd = ThreadLocalRandom.current().nextInt(0,100);
    int r = randomWInd % 2;
        if(r == 1 ){
            return true;
        }
        else  return false;
    }
    private static void getDataUpdate() {
        boolean windy = simulateWind();
        if(windy){
            int rand = ThreadLocalRandom.current().nextInt(140000,200000);
            Power = rand;
        }
        else {
            Power = 0;
        }
    }

    public static void RPCinit() throws InterruptedException {
        wind = new Windpower();
        Thread.sleep(3000);
        try {
            handler = new WindControlHandler(wind);
            processor = new WindControl.WindControl.Processor(handler);

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
    //Thrift Methods
    public static void startThrift(WindControl.WindControl.Processor processor) {
        try {
            TServerTransport serverTransport = new TServerSocket(3456);
            TServer server = new TSimpleServer(new TServer.Args(serverTransport).processor(processor));

            // Use this for a multithreaded server
            // TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

            System.out.println("Starting the Thrift Service on port 3456...");
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