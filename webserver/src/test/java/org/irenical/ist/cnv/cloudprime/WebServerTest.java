package org.irenical.ist.cnv.cloudprime;

import org.apache.http.client.fluent.Request;

import java.io.IOException;

public class WebServerTest {
    private static final int SPAWN_THREAD = 50;
    private static final int TEST_NUMBER = Integer.MAX_VALUE;


    public static void main(String[] args) {
        for(int i = 0; i<SPAWN_THREAD;i++)
            new Thread(){
                @Override
                public void run() {
                    try {
                        Request.Get("http://localhost:8080/f.html?n=" + TEST_NUMBER).execute();
                        System.out.println("DONE");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
    }
}
