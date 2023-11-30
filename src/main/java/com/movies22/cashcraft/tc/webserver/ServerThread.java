package com.movies22.cashcraft.tc.webserver;

public class ServerThread extends Thread {
   private MainServer srv;

   public ServerThread(MainServer s) {
      this.srv = s;
   }

   public void run() {
      this.srv.enable();
   }
}
