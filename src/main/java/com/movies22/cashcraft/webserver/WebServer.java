package com.movies22.cashcraft.webserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class WebServer {
	public WebServer() {

	}

	public static boolean enable() {
		Thread thread = new Thread() {
			public void run() {
				try (ServerSocket serverSocket = new ServerSocket(25566)) {
					System.out.println("Server started. Waiting for messages.");
					System.out.println("Thread running!");
					while (true) {
						try {
							Socket socket = serverSocket.accept();
							System.out.println(socket.getRemoteSocketAddress().toString() + " connecting...");
							PrintWriter out = new PrintWriter(socket.getOutputStream());
							out.println("HTTP/1.1 200 OK");
			                out.println("Content-Type:text/html;charset=utf-8");
			                out.println();
			                out.println("<head></head>");
			                out.println("<body>");
			                out.println("<h1>Hello World!</h1>");
			                out.println("</body>");
			                out.flush();
							out.close();
							socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		thread.start();
		return true;
	}
	public boolean disable() {
		return false;
	}
}
