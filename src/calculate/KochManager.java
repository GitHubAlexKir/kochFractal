package calculate;

import jsf31kochfractalfx.JSF31KochFractalFX;
import timeutil.TimeStamp;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class KochManager {
    private JSF31KochFractalFX application;
    private KochFractal koch = new KochFractal();
    private ArrayList<Edge> edges = new ArrayList<Edge>();

    public KochManager(JSF31KochFractalFX application) {
        this.application = application;
    }

    public void changeLevel(int nxt) throws Exception {

        TimeStamp TS = new TimeStamp();
        koch.setLevel(nxt);
        edges.clear();
        TS.setBegin("Begin calc");
        ExecutorService pool = Executors.newFixedThreadPool(3);
        KochCallable callableBot = new KochCallable("bot",koch.getLevel());
        KochCallable callableLeft = new KochCallable("left",koch.getLevel());
        KochCallable callableRight = new KochCallable("right",koch.getLevel());

        Future<List<Edge>> futureBot = pool.submit(callableBot);
        Future<List<Edge>> futureLeft = pool.submit(callableLeft);
        Future<List<Edge>> futureRight= pool.submit(callableRight);

        addEdges(futureBot.get());
        addEdges(futureLeft.get());
        addEdges(futureRight.get());

        TS.setEnd("End calc");
        application.setTextCalc(TS.toString());
        drawEdges();

    }
    public void drawEdges() {
        TimeStamp TS = new TimeStamp();
        application.clearKochPanel();
        TS.setBegin("Begin drawing");
        for (Edge e:edges) {
            application.drawEdge(e);
        }
        TS.setEnd("End drawing");
        application.setTextDraw(TS.toString());
        application.setTextNrEdges(Integer.toString(edges.size()));
    }
    public synchronized void addEdges(List<Edge> edges) {
            this.edges.addAll(edges);
    }
}
