import ConsumerControl.ConsumerControl;
import org.apache.thrift.TException;

public class ConsumerControlHandler implements ConsumerControl.Iface{
    Consumer Co;
    public ConsumerControlHandler(Consumer con){
    this.Co = con;
    }
    @Override
    public boolean StartConsumerControl(boolean onSwitch) throws TException {
        if(onSwitch){
            try {
                System.out.println("Stop the Component");
                Co.stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else{
            try {
                System.out.println("Start the Component");
                Co.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Received control signal");
        return false;
    }
}
