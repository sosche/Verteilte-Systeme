package Central;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Restdata {
    volatile private JSONObject sensor1;
    volatile private JSONObject sensor2;
    volatile private JSONObject sensor3;
    volatile private JSONObject sensor4;
    private List <JSONObject> sensorHistory;

    public Restdata() {
        JSONObject obj = new JSONObject();
        obj.put("","");
        sensor1 = obj;
        sensor2 = obj;
        sensor3 = obj;
        sensor4 = obj;
        sensorHistory = new ArrayList<JSONObject>();
    }

    public String giveString (String request) throws Exception{
        switch (request){
            case "Consumer":
                return sensor1.toString(2);
            case "Solarpower":
                return sensor2.toString(2);
            case "Windpower":
                return sensor3.toString(2);
            case "Nuclearpower":
                return sensor4.toString(2);
            case "all":
                return (sensor1.toString(2) + "\n" + sensor2.toString(2) + "\n"
                        + sensor3.toString(2) + "\n" + sensor4.toString(2)     );
            case "History":
                return historyToString();
            default:
                throw new Exception("Wrong Sensors");
        }
    }

    public void fromJson (JSONObject jsonobj) throws Exception{
        sensorHistory.add(jsonobj);
        String sensorName = jsonobj.get("SensorName").toString();
        JSONObject obj = new JSONObject(jsonobj,JSONObject.getNames(jsonobj));
        obj.remove(("Address"));
        obj.remove(("Port"));

        switch (sensorName) {
            case "Consumer":
                this.sensor1 = obj;
                break;
            case "Solarpower":
                this.sensor2 = obj;
                break;
            case "Windpower":
                this.sensor3 = obj;
                break;
            case "Nuclearpower":
                this.sensor4 = obj;
                break;
            default:
                throw new Exception("There are only 4 sensors");
        }
    }

    public String historyToString(){
        if (sensorHistory.size() == 0) {
            return "No Sensor Data available";
        } else {
            StringBuilder sb = new StringBuilder();
            for (JSONObject jo : sensorHistory) {
                sb.append(jo.toString(2)).append("\n");
            }
            return sb.toString();
        }
    }
}
