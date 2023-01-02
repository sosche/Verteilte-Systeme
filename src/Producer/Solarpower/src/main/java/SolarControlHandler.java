import org.apache.thrift.TException;

public class SolarControlHandler implements SolarControl.SolarControl.Iface {
    Solarpower solarpower;
    public SolarControlHandler(Solarpower solarpower){
        this.solarpower =  solarpower;
    }

    @Override
    public boolean StartSolarControl(boolean onSwitch) throws TException {
        if(onSwitch){
            try {
                System.out.println("Stop the Component");
                solarpower.stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else{
            try {
                System.out.println("Start the Component");
                solarpower.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Received control signal");
        return false;
    }
}