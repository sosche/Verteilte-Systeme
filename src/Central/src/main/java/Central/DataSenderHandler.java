package Central;

import Printing.DataSender.Iface;
import org.apache.thrift.TException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;


public class DataSenderHandler implements Iface {
    static public String dat;
    ArrayList<String> sensorHistory;
    ArrayList<String> SecondCentralHistory ;
    ArrayList<String> ThirdCentralHistory ;


    public DataSenderHandler(ArrayList<String> history,ArrayList<String> SecondCentralHistory , ArrayList<String> ThirdCentralHistory)
    {
        this.sensorHistory = history;
        this.SecondCentralHistory = SecondCentralHistory;
        this.ThirdCentralHistory = ThirdCentralHistory;
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
//        addToHistory();
        String History = historyToString();
        return History + ";" + ArrayToString(SecondCentralHistory) + ";" + ArrayToString(ThirdCentralHistory);
    }

}
