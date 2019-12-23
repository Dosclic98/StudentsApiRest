package dos.server;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

import dos.handler.RequestHandler;

public class Server {
	
	private static int port = 9000;
	
	public static void main(String args[]) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
		System.out.println("Server started at ports " + port);
		server.createContext("/data", new RequestHandler());
		server.setExecutor(null);
		server.start();
	}
}
