package server;

import calculate.Edge;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;

public class KochCallable implements Callable , Observer{
    private  KochFractal fractal;
    private List<Edge> edges;
    private String side;
    public KochCallable(String side,int level) {
        this.side = side;
        fractal = new KochFractal();
        fractal.setLevel(level);
        fractal.addObserver(this);
        edges = new ArrayList<>();

    }

    @Override
    public List<Edge> call() throws Exception {
        switch (side) {
            case "left": fractal.generateLeftEdge();
                break;
            case "right": fractal.generateRightEdge();
                break;
            case "bot": fractal.generateBottomEdge();
                break;
        }
        return edges;
    }

    @Override
    public void update(Observable o, Object arg) {
        edges.add((Edge)arg);
    }
}
