/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calculate;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import Fractal.Edge;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import jsf31kochfractalfx.JSF31KochFractalFX;
import timeutil.TimeStamp;

/**
 *
 * @author rick-
 */
public class KochManager{
    private JSF31KochFractalFX application;
    private int level = 1;
    private int way;
    private String host;
    private TimeStamp ts = new TimeStamp();
    private List<Edge> edges = new ArrayList<>();
    private ExecutorService pool;
    public KochManager(JSF31KochFractalFX application,String host, int way){
        this.application = application;
        this.host = host;
        this.way = way;
        //changeLevel(level);
        pool = Executors.newFixedThreadPool(1);
        changeLevel(level);
    }
    public void changeLevel(int level){

        Task lt = new Task() {
            @Override
            protected Object call() throws Exception {
                int numberEdges = (int) (3 * Math.pow(4, level - 1));
                Platform.runLater(()-> application.setTextNrEdges(String.valueOf(numberEdges)));
                TimeStamp ts = new TimeStamp();

                ts.setBegin();
                try (
                        Socket socket = new Socket(host, 1337);
                        ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
                        ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream())
                ) {
                    outStream.writeObject(new Request(level,way));
                    Platform.runLater(()-> application.clearKochPanel());

                    switch (way){
                        case 1:
                            edges = (List<Edge>) inStream.readObject();
                            Platform.runLater(()-> drawEdges());
                            break;
                        case  2:
                            for (int i = 0; i < numberEdges;i++)
                            {
                                Edge edge = (Edge)inStream.readObject();
                                Platform.runLater(()-> application.drawEdge(edge));
                            }
                    }

                    ts.setEnd();
                    Platform.runLater(()->application.setTextDraw(ts.toString()));
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        pool.execute(lt);
    }

    public void drawEdges(){
        application.clearKochPanel();
        application.setTextCalc(ts.toString());
        TimeStamp ts2 = new TimeStamp();
        ts2.setBegin();
        edges.forEach((e)-> application.drawEdge(e));
        ts2.setEnd();
        application.setTextDraw(ts2.toString());
        application.doneDrawing();
    }


}
