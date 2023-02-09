import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;

import static jdk.xml.internal.SecuritySupport.getClassLoader;

public class RPCClient<T> {
    public static <T> T getRemoteProxyObj(final Class<?> serviceInterface, final InetSocketAddress address){
        //动态代理 serviceInterface为传输的接口，读取其Class对应的值，然后通过构造器实例化一个和所要代理的类相同的代理类。
        return (T) Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class<?>[]{serviceInterface}, new InvocationHandler(){
                    public Object invoke(Object proxy, Method method,Object[] args) throws Throwable{
                        Socket socket = null;
                        ObjectOutputStream output = null;
                        ObjectInputStream input = null;
                        try{
                            //创建Socket客户端，根据指定地址连接远程服务提供者
                            socket = new Socket();
                            socket.connect(address);

                            //将远程服务调用所需的接口类，方法名，参数列表等编码后发给服务提供者
                            output = new ObjectOutputStream(socket.getOutputStream());
                            output.writeUTF(serviceInterface.getName());
                            output.writeUTF(method.getName());
                            output.writeObject(method.getParameterTypes());
                            output.writeObject(args);

                            //同步阻塞等待服务器返回应答，获取应答后返回
                            input = new ObjectInputStream(socket.getInputStream());
                            return input.readObject();
                        }finally{
                            if(socket!=null){
                                socket.close();
                            }
                            if(output!=null){
                                output.close();
                            }
                            if(input!=null){
                                input.close();
                            }
                        }
                    }
                });

    }

}
