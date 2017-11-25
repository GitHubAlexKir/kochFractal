/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calculate;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
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
public class KochManager implements Observer{
    public static interface updateCallback{
        public void update(Edge e) throws InterruptedException;
    }

    private JSF31KochFractalFX application;
    private int level = 1;
    private KochFractal koch;
    private BlockingQueue<Edge> edgesQ = new LinkedBlockingQueue<>();

    private TimeStamp ts = new TimeStamp();

    private ExecutorService pool;
    private CountDownLatch lat;

    private AtomicBoolean Drawing = new AtomicBoolean(false);
    Task lt,rt,bt,end;

    public KochManager(JSF31KochFractalFX application){
        pool = Executors.newFixedThreadPool(3);
        this.application = application;
        koch = new KochFractal(this);
        koch.addObserver(this);
        koch.setLevel(level);
        changeLevel(level);
    }

    public void changeLevel(int value){
        level=value;
        koch.setLevel(level);
        if(koch.getNrOfEdges() == edgesQ.size()){
            application.requestDrawEdges(true);
            return;
        }
        ts = new TimeStamp();
        ts.setBegin();
        koch.cancel();
        if(lt!=null){
            lt.cancel(true);
            rt.cancel(true);
            bt.cancel(true);
            end.cancel(true);
            while(lt.isRunning()||rt.isRunning()||bt.isRunning()|| end.isRunning())try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        edgesQ.clear();
        application.setTextNrEdges(koch.getNrOfEdges() + "");
        application.setTextCalc("");
        application.setTextDraw("");
        lat = new CountDownLatch(3);
        lt = new Task() {
            private final List<Edge> pEdges = new LinkedList<>();
            @Override
            protected Object call() {
                koch.generateLeftEdge(lat, (Edge e) -> {
                    pEdges.add(e);
                    updateProgress(pEdges.size(), koch.getNrOfEdges()/3);

                   float percent = ((float)pEdges.size() / (float)(koch.getNrOfEdges()/3) * 100);
                    updateMessage("Left:   "+ "\t" + pEdges.size() + "/"+ koch.getNrOfEdges()/3 + "\t" + Math.round(percent) + "%");

                    edgesQ.add(e);
                    Platform.runLater(()-> application.drawEdge(new Edge(e.X1,e.Y1,e.X2,e.Y2,Color.WHITE)));

                    if(koch.getLevel()<6){
                        Thread.sleep(1);
                    }else if(koch.getLevel()>=6 && koch.getLevel()<8){
                        Thread.sleep(0, 10);
                    }else{
                        Thread.sleep(0,1);
                    }
                });
                return null;
            }
        };
        rt = new Task() {
            private final List<Edge> pEdges = new LinkedList<>();
            @Override
            protected Object call() throws Exception {
                koch.generateRightEdge(lat, (Edge e) -> {
                    pEdges.add(e);
                    updateProgress(pEdges.size(), koch.getNrOfEdges()/3);
                    float percent = ((float)pEdges.size() / (float)(koch.getNrOfEdges()/3) * 100);
                    updateMessage("Right:" + "\t"+ pEdges.size() + "/"+ koch.getNrOfEdges()/3 + "\t" + Math.round(percent) + "%");
                    edgesQ.add(e);
                    Platform.runLater(()-> application.drawEdge(new Edge(e.X1,e.Y1,e.X2,e.Y2,Color.WHITE)));
                    if(koch.getLevel()<6){
                        Thread.sleep(1);
                    }else if(koch.getLevel()>=6 && koch.getLevel()<8){
                        Thread.sleep(0, 10);
                    }else{
                        Thread.sleep(0,1);
                    }

                });
                return null;
            }
        };
        bt = new Task() {
            private final List<Edge> pEdges = new LinkedList<>();
            @Override
            protected Object call() throws Exception {
                koch.generateBottomEdge(lat, (Edge e) -> {
                    pEdges.add(e);
                    updateProgress(pEdges.size(), koch.getNrOfEdges()/3);
                    float percent = ((float)pEdges.size() / (float)(koch.getNrOfEdges()/3) * 100);
                    updateMessage("Bottom:" + "\t"+ pEdges.size() + "/"+ koch.getNrOfEdges()/3 + "\t" + Math.round(percent) + "%");
                    edgesQ.add(e);
                    Platform.runLater(()-> application.drawEdge(new Edge(e.X1,e.Y1,e.X2,e.Y2,Color.WHITE)));
                    if(koch.getLevel()<6){
                        Thread.sleep(1);
                    }else if(koch.getLevel()>=6 && koch.getLevel()<8){
                        Thread.sleep(0, 10);
                    }else{
                        Thread.sleep(0,1);
                    }

                });
                return null;
            }
        };

        application.BindPropB(bt);
        application.BindPropL(lt);
        application.BindPropR(rt);

        pool.execute(bt);
        pool.execute(lt);
        pool.execute(rt);
        end = new Task() {
            @Override
            protected Object call() throws Exception {
                try{
                    lat.await();
                } catch (InterruptedException ex) {
                    //Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
                    if(this.isCancelled())return null;
                }
                ts.setEnd();
                application.requestDrawEdges(true);
                return null;
            }
        };
        pool.execute(end);
    }

    public void drawEdges(){
        application.clearKochPanel();
        application.setTextCalc(ts.toString());
        TimeStamp ts2 = new TimeStamp();
        ts2.setBegin();
        edgesQ.forEach((e)-> application.drawEdge(e));
        ts2.setEnd();
        application.setTextDraw(ts2.toString());
        application.doneDrawing();
    }
    @Override
    public void update(Observable o, Object arg) {
        if(o instanceof KochFractal && arg instanceof Edge){
            edgesQ.add((Edge)arg);
        }
    }

    public void stop(){
        koch.cancel();
        pool.shutdown();
    }


}
