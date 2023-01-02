import org.apache.thrift.TException;
import NuclearControl.NuclearControl;
public class NuclearControlHandler implements NuclearControl.Iface{
    Nuclearpower nuk;
    public NuclearControlHandler(Nuclearpower nuke){
        this.nuk = nuke;
    }

    @Override
    public boolean StartNuclearControl(boolean onSwitch) throws TException {
        if(onSwitch){
            try {
                System.out.println("Stop the Component");
                nuk.stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else{
            try {
                System.out.println("Start the Component");
                nuk.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Received control signal");
        return false;
    }
}
