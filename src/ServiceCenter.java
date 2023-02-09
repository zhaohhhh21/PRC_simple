import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServiceCenter implements Server{
    //线程池
    private static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private static final HashMap<String, Class<?>> serviceRegistry = new HashMap<String, Class<?>>();

    private static boolean isRunning = false;

    private static int port;

    public ServiceCenter(int _port){
        port = _port;
    }


    @Override
    public void stop() {
        isRunning = false;
        executor.shutdown();
    }

    @Override
    public void start() throws IOException {
        //socket服务
        ServerSocket server = new ServerSocket();
        //ip address + port number
        server.bind(new InetSocketAddress(port));
        System.out.println("start server");
        try {
            while(true){
                executor.execute(new ServiceTask(server.accept()));
            }
        }finally {
            server.close();
        }
    }

    @Override
    public void register(Class<?> serviceInterface, Class<?> impl) {
        System.out.println("Register:" + serviceInterface.getName());
        serviceRegistry.put(serviceInterface.getName(), impl);
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public int getPort() {
        return port;
    }


    private static class ServiceTask implements Runnable{
        Socket client = null;

        public ServiceTask(Socket client){
            this.client = client;
        }

        @Override
        public void run() {
            //反序列化流
            ObjectInputStream input = null;
            ObjectOutputStream output = null;
            try{
                input = new ObjectInputStream(client.getInputStream());
                //UTF-8编码
                //将客户端的码流反序列成对象，反射调用服务提供这，获取执行结果
                String serviceName = input.readUTF();
                String methodName = input.readUTF();
                Class<?>[] parameterTypes = (Class<?>[]) input.readObject();
                //参数
                Object[] arguments = (Object[]) input.readObject();
                Class<?> serviceClass = serviceRegistry.get(serviceName);
                if(serviceClass == null){
                    throw new ClassNotFoundException(serviceName + " not found");
                }
                Method method = serviceClass.getMethod(methodName, parameterTypes);
                //调用函数
                Object result = method.invoke(serviceClass.getDeclaredConstructor().newInstance(), arguments);

                //将执行结果序列化，通过socket发给客户端
                output = new ObjectOutputStream(client.getOutputStream());
                output.writeObject(result);

            } catch (IOException | ClassNotFoundException | NoSuchMethodException | InstantiationException |
                     IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }finally{
                if(output != null){
                    try{
                        output.close();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
                if(input != null){
                    try{
                        input.close();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
                if(client != null){
                    try{
                        client.close();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
