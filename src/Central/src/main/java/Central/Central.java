package Central;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.*;
import org.json.JSONObject;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Central implements MqttCallback {
  static private HttpServer HttpServer = new HttpServer();
//  static private UDPServer udp;
  public static Printing.DataSender.Processor processor;
  public static DataSenderHandler handler;
  private static String SOLARIP = System.getenv("SOLARIP");
  private static String CONIP = System.getenv("CONIP");
  private static String WINDIP = System.getenv("WINDIP");
  private static String NUCIP = System.getenv("NUCIP");
  private static MqttClient mqttClient;
  private static boolean running = true;
  private static int packageSize = 5 ;
  private static int PackageNumber = 1;
  public static Runnable serverCentral;
  private static ArrayList<String> history ;
  private static ArrayList<String> SecondCentralHistory ;
  private static ArrayList<String> ThirdCentralHistory ;


  public Central(){
    this.history = new ArrayList<String>();
    this.SecondCentralHistory = new ArrayList<String>();
    this.ThirdCentralHistory = new ArrayList<String>();
  }

  public void process (JSONObject receivedMessage) throws Exception {
    handler = new DataSenderHandler(this.history, this.SecondCentralHistory ,this.ThirdCentralHistory );
    handler.addToHistory(receivedMessage.toString(2));
    handler.fromJson(receivedMessage);
    HttpServer.fromJson(receivedMessage);
  }


    public static void main(String args[]) throws IOException, MqttException, ParseException {
      mqttClient = new MqttClient("tcp://broker.hivemq.com:1883", MqttClient.generateClientId());
      String Failtime = getFailTime();
      String RestartTime = getRestartTime();

      System.out.println("Expected Server crashes at:" + getFailTime());
      System.out.println("Expected Server restarts at:" + getRestartTime());

      ServerFailStimulation(Failtime);
      ServerRestartStimulation(RestartTime);

      if(SOLARIP == null) SOLARIP = "127.0.0.1";
      if(CONIP == null) CONIP = "127.0.0.1";
      if(WINDIP == null) WINDIP = "127.0.0.1";
      if(NUCIP == null) NUCIP = "127.0.0.1";
      Central central = new Central();
      try {
        Thread.sleep(2000);
        handler = new DataSenderHandler(history,SecondCentralHistory,ThirdCentralHistory);
        processor = new Printing.DataSender.Processor(handler);

        Runnable startThrift = new Runnable() {

          public void run() {
            startThrift(processor);
          }
        };
        new Thread(startThrift).start();
        Thread httpServer = new Thread(HttpServer);
        httpServer.start();
        central.connectMqtt();
        serverCentral = new Runnable() {
          @Override
          public void run() {
            while (running){
              try {
                Thread.sleep(4000);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              try {
                connectSecondCentral();
              } catch (TException e) {
                e.printStackTrace();
              }
              try {
                Thread.sleep(4000);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              try {
                connectThirdCentral();
              } catch (TException e) {
                e.printStackTrace();
              }
            }
          }

        };
        new Thread(serverCentral).start();

      }
      catch (Exception e) {
        System.err.println("Server Connection error : " + e.getMessage());
      }

  //    central.controllingSolar();
  //    central.controllingCompany();
  //    central.controllingNuke();
  //    central.controllingWind();
    }
  public static void stopServer(){
    running = false;
  }
  public static void start() throws Exception {
    running = true;
    new Thread(serverCentral).start();
  }

  public static void ServerRestartStimulation(String RestartTime) throws ParseException {
    Timer t=new Timer();
    t.schedule(new TimerTask() {
      public void run() {
        try {
          start();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(RestartTime));
  }

  public static void ServerFailStimulation(String FailedTime) throws ParseException {
    Timer t1=new Timer();
    t1.schedule(new TimerTask() {
      public void run() {
        try {
          stopServer();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(FailedTime));
  }
  //Thrift Methods
  public static void startThrift(Printing.DataSender.Processor processor) {
    try {
      TServerTransport serverTransport = new TServerSocket(9090);
//      TServer server = new TSimpleServer(new Args(serverTransport).processor(processor));

      // Use this for a multithreaded server
       TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

      System.out.println("Starting the Thrift Service on port 9090...");
      server.serve();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  public void controllingSolar(){
    try {
      TTransport transport;
      transport = new TSocket(SOLARIP, 4567);
      try {
        transport.open();
        TProtocol protocol = new TBinaryProtocol(transport);
        SolarControl.SolarControl.Client client = new SolarControl.SolarControl.Client(protocol);
        System.out.println("Turn off the SOLAR");
        System.out.println(client.StartSolarControl(true));

        TimeUnit.SECONDS.sleep(4);
        System.out.println("Turn on the SOLAR");
        System.out.println(client.StartSolarControl(false));

        TimeUnit.SECONDS.sleep(4);
        System.out.println("Turn off the SOLAR");
        System.out.println(client.StartSolarControl(true));

      } finally {
      }
    }catch (Exception e) {
      System.err.println("Server Connection error : " + e.getMessage());
    }
  }
  public void controllingConsumer() {
    try {
      TTransport transport;
      transport = new TSocket(CONIP, 1234);
      try {
        transport.open();
        TProtocol protocol = new TBinaryProtocol(transport);
        ConsumerControl.ConsumerControl.Client client = new ConsumerControl.ConsumerControl.Client(protocol);
        System.out.println("Turn off the Consumer");
        System.out.println(client.StartConsumerControl(true));

        TimeUnit.SECONDS.sleep(4);
        System.out.println("Turn on the Consumer");
        System.out.println(client.StartConsumerControl(false));

        TimeUnit.SECONDS.sleep(4);
        System.out.println("Turn off the Consumer");
        System.out.println(client.StartConsumerControl(true));

      } finally {
      }
    }catch (Exception e) {
      System.err.println("Server Connection error : " + e.getMessage());
    }
  }
  public void controllingWind() {
    try {
      TTransport transport;
      transport = new TSocket(WINDIP, 3456);
      try {
        transport.open();
        TProtocol protocol = new TBinaryProtocol(transport);
        WindControl.WindControl.Client client = new WindControl.WindControl.Client(protocol);
        System.out.println("Turn off the Wind");
        System.out.println(client.StartWindControl(true));

        TimeUnit.SECONDS.sleep(4);
        System.out.println("Turn on the Wind");
        System.out.println(client.StartWindControl(false));

        TimeUnit.SECONDS.sleep(4);
        System.out.println("Turn off the Wind");
        System.out.println(client.StartWindControl(true));

      } finally {
      }
    }catch (Exception e) {
      System.err.println("Server Connection error : " + e.getMessage());
    }
  }
  public void controllingNuke() {
    try {
      TTransport transport;
      transport = new TSocket(NUCIP, 2345);
      try {
        transport.open();
        TProtocol protocol = new TBinaryProtocol(transport);
        NuclearControl.NuclearControl.Client client = new NuclearControl.NuclearControl.Client(protocol);
        System.out.println("Turn off the Nuclear");
        System.out.println(client.StartNuclearControl(true));

        TimeUnit.SECONDS.sleep(4);
        System.out.println("Turn on the Nuclear");
        System.out.println(client.StartNuclearControl(false));

        TimeUnit.SECONDS.sleep(4);
        System.out.println("Turn off the Nuclear");
        System.out.println(client.StartNuclearControl(true));

      } finally {
      }
    }catch (Exception e) {
      System.err.println("Server Connection error : " + e.getMessage());
    }
  }
  //Mosquitto
  public void connectMqtt() throws MqttException {
    MqttConnectOptions options = new MqttConnectOptions();
    options.setAutomaticReconnect(true);
    options.setCleanSession(true);
    options.setConnectionTimeout(10);
    mqttClient.setCallback(this);
    mqttClient.connect(options);
    mqttClient.subscribe("Sensor/Solarpower");
    mqttClient.subscribe("Sensor/Nuclearpower");
    mqttClient.subscribe("Sensor/Windpower");
    mqttClient.subscribe("Sensor/Consumer");
    System.out.println("\nSubscribing to Broker on tcp://broker.hivemq.com:1883 ...\n");
  }


  @Override
  public void connectionLost(Throwable cause) {
    System.out.println("Central Server -> Connection to MQTT broker lost!");
  }

  @Override
  public void messageArrived(String topic, MqttMessage message) throws Exception {
    if(running){
      String sensorDataJson = new String(message.getPayload());
//    System.out.println("estimated time " + System.nanoTime());
      System.out.println("NEW MQTT:" + sensorDataJson);
      JSONObject jsonObject = new JSONObject(sensorDataJson);
      process(jsonObject);
    }else{
      System.out.println("Central Is Offline");
    }

  }
  public static String getFailTime() {
    return LocalDateTime.now().plusMinutes(1)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }
  public static String getRestartTime() {
    return LocalDateTime.now().plusMinutes(2)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }


  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {

  }

  public static void connectSecondCentral() throws TException {
    String SecondCentral = System.getenv("SECONDCENTRAL");
    if(SecondCentral == null) SecondCentral = "localhost";
    String SecHistory;
    TTransport transport;
    transport = new TSocket(SecondCentral, 9091);
    transport.open();
    TProtocol protocol = new TBinaryProtocol(transport);
    System.out.println("Data From Second Central");
    System.out.println("++++++++++++++++++++++++");
    Printing.DataSender.Client client = new Printing.DataSender.Client(protocol);
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
  public static void connectThirdCentral() throws TException {
    String ThirdCentral = System.getenv("THIRDCENTRAL");
    if(ThirdCentral == null) ThirdCentral = "localhost";
    String ThirdHistory;
    TTransport transport;
    transport = new TSocket(ThirdCentral, 9092);
    transport.open();
    TProtocol protocol = new TBinaryProtocol(transport);
    System.out.println("Data From Third Central");
    System.out.println("++++++++++++++++++++++++");
    Printing.DataSender.Client client = new Printing.DataSender.Client(protocol);
    ThirdHistory = client.historyToString();
    if(!ThirdHistory.equals("No Sensor Data available")){
      List<String> HistoryList =  Arrays.asList(ThirdHistory.split("}"));
      Paginate(HistoryList);
      handler.ThirdCentralHistory.clear();
      handler.ThirdCentralHistory.add(ThirdHistory);

    } else{
      System.out.println("No Sensor Data Available");
    }

    transport.close();
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
