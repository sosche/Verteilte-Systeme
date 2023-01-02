import org.apache.thrift.TProcessor;
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
import java.time.ZoneId;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Solarpower {
    private static final String sensorName = "Solarpower";
    private static final String sensorType = "Generator";
    private static int Power;
    public static boolean running = true;
    public static Solarpower sl ;
    public static SolarControlHandler handler;
    public static SolarControl.SolarControl.Processor processor;
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
//                UDPClient udpClient = null;
//                try {
//                    udpClient = new UDPClient();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                System.out.println("Solar started");
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
    /*
    Initialize the RPC Thrift server
     */
    public static void RPCinit() throws InterruptedException {
        sl = new Solarpower();
        Thread.sleep(3000);
        try {
            handler = new SolarControlHandler(sl);
            processor = new SolarControl.SolarControl.Processor(handler);

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


    public void stop() throws InterruptedException {
        running = false;
    }
    public void start() throws Exception {
        running = true;
        new Thread(server).start();
    }
    private static int checkTime() {
        Date dateInput = new Date();
        int Hour = dateInput.toInstant().atZone(ZoneId.systemDefault()).getHour();
        return Hour;
    }
    private static void getDataUpdate() {
        int Time = checkTime();
        if(Time > 22 || Time < 5 ) {
            Power = 0;
        }else {
            int rand = ThreadLocalRandom.current().nextInt(1000, 5000);
            Power = rand;
        }
    }

    //Thrift Methods
    public static void startThrift(SolarControl.SolarControl.Processor processor) {
        try {
            TServerTransport serverTransport = new TServerSocket(4567);
            TServer server = new TSimpleServer(new TServer.Args(serverTransport).processor(processor));

            // Use this for a multithreaded server
            // TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

            System.out.println("Starting the Thrift Service on port 4567...");
            server.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void connectMqtt() throws MqttException {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        mqttClient.connect(options);
    }
}