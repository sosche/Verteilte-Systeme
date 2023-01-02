import Printing.DataSender;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.util.Arrays;
import java.util.List;


public class ExternalClient {
    private static int packageSize = 5 ;
    private static int PackageNumber = 1;
    public static void main(String[] args)  {
        String server_central = System.getenv("DESTINATION");
        if(server_central == null) server_central = "localhost";
        try {
            TTransport transport;
            transport = new TSocket(server_central, 9090);
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            Printing.DataSender.Client client = new Printing.DataSender.Client(protocol);
//            Printing out all History without Pagination
//                        perform(client);
            save(client);
            transport.close();
        } catch (TException e) {
            e.printStackTrace();
        }
    }

    private static void save (Printing.DataSender.Client client) throws TException {
       String history = client.printing("Begin Thrifting");
        String[] arrHistory = history.split(";");
//        System.out.println(arrHistory.length);
//        System.out.println(arrHistory[0]);

        for(int i = 0 ; i < arrHistory.length; i++){
            List<String> HistoryList =  Arrays.asList(arrHistory[i].split("}"));
                Paginate(HistoryList);
        }
//        List<String> HistoryList = Arrays.asList(arrHistory);
//        int fromIndex = (PackageNumber-1) * packageSize;
//        int TotalPage = HistoryList.size() / packageSize ; // Calculate total Page
//        System.out.println( "Total History Number :" +
//                + HistoryList.size() + "\n");
//        System.out.println("Total Page :" + TotalPage);
//        if( HistoryList.size() < packageSize ){
//            System.out.println( "++++++++++++++++ History +++++++++++++++++ "+ "\n"
//                    + HistoryList.subList(0 , HistoryList.size())  + "\n");
//            return;
//        }
//        if((fromIndex > HistoryList.size())){
//            System.out.println("invalid page number or Page Site");
//            return;
//        }
//
//        System.out.println( "++++++++++++++++ History +++++++++++++++++ "+ "\n" + "Current Page: " + PackageNumber + "\n"
//                + HistoryList.subList(fromIndex, ((PackageNumber-1) * packageSize) + packageSize) +  "\n");

    }

    private static void perform(Printing.DataSender.Client client) throws TException {
            String print = client.printing("Hello from the other side \n");
            System.out.println(print);
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