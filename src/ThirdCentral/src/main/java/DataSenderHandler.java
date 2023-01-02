
import Printing.DataSender;
import org.json.JSONObject;

import java.util.ArrayList;

public class DataSenderHandler implements DataSender.Iface {


    static public String dat;
    ArrayList<String> sensorHistory;
    ArrayList<String> CentralHistory ;
    ArrayList<String> SecondCentralHistory ;

    public DataSenderHandler(ArrayList<String> history,
                             ArrayList<String> CentralHistory,
                             ArrayList<String> SecondCentralHistory){
        this.sensorHistory = history;
        this.CentralHistory = CentralHistory ;
        this.SecondCentralHistory = SecondCentralHistory;

    }

    public void fromJson (JSONObject receivedMessage) throws Exception {
        dat = receivedMessage.toString(2);
    }

    public void addToHistory(String Infor) {
        sensorHistory.add(Infor);
    }

    @Override
    public String historyToString(){
        if (sensorHistory.size() == 0) {
            return "No Sensor Data available";
        } else {
            StringBuilder sb = new StringBuilder();
            for (String jo : sensorHistory) {
                sb.append(jo).append("\n");
            }
            return sb.toString();
        }
    }

    public String ArrayToString(ArrayList<String> List){
        if (List.size() == 0) {
            return "No Sensor Data available";
        } else {
            StringBuilder sb = new StringBuilder();
            for (String jo : List) {
                sb.append(jo).append("\n");
            }
            return sb.toString();
        }
    }
    @Override
    public String printing(String dataa) throws Exception {
        String History = historyToString();
        return History + ";" + ArrayToString(CentralHistory) + ";" + ArrayToString(SecondCentralHistory);
    }
}