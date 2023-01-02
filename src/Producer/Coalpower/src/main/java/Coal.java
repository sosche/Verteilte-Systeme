import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

public class Coal {
    private static final String sensorName = "CoalPower";
    private static final String sensorType = "Generator";
    private static int Power;
    public static boolean running = true;
    public static Runnable server;
    protected static MqttClient mqttClient;
    public static void main(String[] args) throws Exception {
        mqttClient = new MqttClient("tcp://broker.hivemq.com:1883", MqttClient.generateClientId());
        connectMqtt();
        /*
        Start the component
         */
        server = new Runnable() {
            @Override
            public void run() {
                System.out.println("Coal started");
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
                        mqttClient.publish("Sensor2/" + sensorName , mqttMessage);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Message sent with payload: " +jsonMessage.toString() );
                }
            }
        };
        new Thread(server).start();
    }
    public static void connectMqtt() throws MqttException {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        mqttClient.connect(options);
    }
    private static void getDataUpdate() {
        int rand = ThreadLocalRandom.current().nextInt(140000,200000);
        Power = rand;
    }
}