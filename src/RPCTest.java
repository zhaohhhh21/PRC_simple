import java.io.IOException;
import java.net.InetSocketAddress;

public class RPCTest {

    public static void main(String[] args) throws IOException{
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //实现服务端
                    Server serviceServer = new ServiceCenter(8088);
                    //此处登记的是HelloService的接口
                    serviceServer.register(HelloService.class, HelloServiceImpl.class);
                    serviceServer.register(GoodByeService.class, GoodByeServiceImpl.class);
                    serviceServer.start();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
        HelloService serviceHello = RPCClient.getRemoteProxyObj(HelloService.class, new InetSocketAddress("localhost", 8088));
        GoodByeService serviceGoodBye = RPCClient.getRemoteProxyObj(GoodByeService.class, new InetSocketAddress("localhost", 8088));

        System.out.println(serviceHello.sayHi("test"));
        System.out.println(serviceGoodBye.sayGoodBye("test"));
    }

}
