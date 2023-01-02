import org.apache.thrift.TException;
import WindControl.WindControl;

public class WindControlHandler implements WindControl.Iface {
    Windpower Wind;
    public WindControlHandler(Windpower Wind){
        this.Wind =  Wind;
    }

    @Override
    public boolean StartWindControl(boolean onSwitch) throws TException {
        if(onSwitch){
            try {
                System.out.println("Stop the Component");
                Wind.stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else{
            try {
                System.out.println("Start the Component");
                Wind.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Received control signal");
        return false;
    }
}