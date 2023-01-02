

import Printing.DataSender;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.*;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ThirdCentral implements MqttCallback {
    public static DataSender.Processor processor;
    public static DataSenderHandler handler;
    private static MqttClient mqttClient;
    private static boolean running = true;
    private static ArrayList<String> history ;
    private static ArrayList<String> CentralHistory ;
    private static ArrayList<String> SecondCentralHistory ;
    private static int packageSize = 5 ;
    private static int PackageNumber = 1;
    public ThirdCentral(){
        this.CentralHistory = new ArrayList<String>();
        this.SecondCentralHistory = new ArrayList<String>();
        this.history = new ArrayList<String>();
    }
    public void process (JSONObject receivedMessage) throws Exception {
        handler = new DataSenderHandler(this.history,this.CentralHistory,this.SecondCentralHistory);
        handler.addToHistory(receivedMessage.toString(2));
        handler.fromJson(receivedMessage);
    }

    public static void main(String[] args) throws Exception {
        mqttClient = new MqttClient("tcp://broker.hivemq.com:1883", MqttClient.generateClientId());
        ThirdCentral thirdCentral = new ThirdCentral();

        //Start Thrift server
        try {
            Thread.sleep(2000);
            handler = new DataSenderHandler(history,CentralHistory,SecondCentralHistory);
            processor = new Printing.DataSender.Processor(handler);
            Runnable startThrift = new Runnable() {
                public void run() {
                    startThrift(processor);
                }
            };
            new Thread(startThrift).start();
        }catch (Exception e) {
            System.err.println("Server Connection error : " + e.getMessage());
        }
        thirdCentral.connectMqtt();
        while (running){
            Thread.sleep(4000);
            connectToCentral();
            Thread.sleep(4000);
            connectSecondCentral();
        }
    }

    public static void connectToCentral() throws TException {
        String server_central = System.getenv("DESTINATION");
        if(server_central == null) server_central = "localhost";
        String CentralHistory;
        TTransport transport;
        transport = new TSocket(server_central, 9090);
        transport.open();
        TProtocol protocol = new TBinaryProtocol(transport);
        Printing.DataSender.Client client = new Printing.DataSender.Client(protocol);
        System.out.println("Data From Central");
        System.out.println("++++++++++++++++++++++++");
        CentralHistory = client.historyToString();
        if(!CentralHistory.equals("No Sensor Data available")) {
            List<String> HistoryList =  Arrays.asList(CentralHistory.split("}"));
            Paginate(HistoryList);
            handler.CentralHistory.clear();
            handler.CentralHistory.add(CentralHistory);
        }
        else{
            System.out.println("No Sensor Data Available");
        }
        transport.close();
    }

    public static void connectSecondCentral() throws TException {
        String SecondCentral = System.getenv("SECONDCENTRAL");
        if(SecondCentral == null) SecondCentral = "localhost";
        String SecHistory;
        TTransport transport;
        transport = new TSocket(SecondCentral, 9091);
        transport.open();
        TProtocol protocol = new TBinaryProtocol(transport);
        Printing.DataSender.Client client = new Printing.DataSender.Client(protocol);
        System.out.println("Data From Second Central");
        System.out.println("++++++++++++++++++++++++");
        SecHistory = client.historyToString();
        if(!SecHistory.equals("No Sensor Data available")) {
            List<String> HistoryList =  Arrays.asList(SecHistory.split("}"));
            Paginate(HistoryList);
            handler.SecondCentralHistory.clear();
            handler.SecondCentralHistory.add(SecHistory);
        }
        else{
            System.out.println("No Sensor Data Available");
        }
        transport.close();
    }
    public static void startThrift(DataSender.Processor processor) {
        try {
            TServerTransport serverTransport = new TServerSocket(9092);
//            TServer server = new TSimpleServer(new TServer.Args(serverTransport).processor(processor));

            // Use this for a multithreaded server
            TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

            System.out.println("Starting the Thrift Service on port 9092...");
            server.serve();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void connectMqtt() throws MqttException {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        mqttClient.setCallback(this);
        mqttClient.connect(options);
        mqttClient.subscribe("Sensor3/HouseHold");
        System.out.println("\nSubscribing to Broker on tcp://broker.hivemq.com:1883 ...\n");
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("Central Server -> Connection to MQTT broker lost!");

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String sensorDataJson = new String(message.getPayload());
        System.out.println("NEW MQTT:" + sensorDataJson);
        JSONObject jsonObject = new JSONObject(sensorDataJson);
        process(jsonObject);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
    public static void Paginate(List<String> HistoryList) {
        int fromIndex = (PackageNumber-1) * packageSize;
        int TotalPage = HistoryList.size() / packageSize ; // Calculate total Page
        System.out.println( "Total History Number :" +
                + HistoryList.size() + "\n");
        System.out.println("Total Page :" + TotalPage);
        if( HistoryList.size() < packageSize ){
            System.out.println( "++++++++++++++++ History +++++++++++++++++ "+ "\n"
                    + HistoryList.subList(0 , HistoryList.size())  + "\n");
            return;
        }
        if((fromIndex > HistoryList.size())){
            System.out.println("invalid page number or Page Site");
            return;
        }

        System.out.println( "++++++++++++++++ History +++++++++++++++++ "+ "\n" + "Current Page: " + PackageNumber + "\n"
                + HistoryList.subList(fromIndex, ((PackageNumber-1) * packageSize) + packageSize) +  "\n");
    }
}