package server;

import calculate.Edge;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    private ObjectOutputStream out;

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        while(true) {
            Main main = new Main();
            main.calculateFractal();
            System.out.println("Successful");
        }
    }

    public void calculateFractal() throws IOException, ExecutionException, InterruptedException {

        ServerSocket serverSocket = new ServerSocket(1337);

        System.out.println("waiting for client connection");
        ExecutorService pool = Executors.newCachedThreadPool();

        while (true) {
            Socket socket = serverSocket.accept();
            pool.submit(() -> {
                try {
                    DoFractalTask(socket);
                } catch (IOException | ExecutionException | InterruptedException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
        }
    }
    public void DoFractalTask(Socket incoming) throws IOException, ExecutionException, InterruptedException, ClassNotFoundException {
        ObjectInputStream inStream = new ObjectInputStream(incoming.getInputStream());
        out = new ObjectOutputStream(incoming.getOutputStream());

        System.out.println("Scanning for level");
        int level = (int)inStream.readObject();
        System.out.println("Level " + level + " chosen.");
        calculateFractal(level);
    }

    public void calculateFractals() throws IOException, ExecutionException, InterruptedException {
        for (int i = 1; i < 10; i++)
            calculateFractal(i);
    }

    public void calculateFractal(int level) throws IOException, ExecutionException, InterruptedException {

        TimeStamp time = new TimeStamp();
        time.setBegin();
        ExecutorService pool = Executors.newFixedThreadPool(3);
        KochCallable callableBot = new KochCallable("bot", level);
        KochCallable callableLeft = new KochCallable("left", level);
        KochCallable callableRight = new KochCallable("right", level);
        Future<List<Edge>> futureBot = pool.submit(callableBot);
        Future<List<Edge>> futureLeft = pool.submit(callableLeft);
        Future<List<Edge>> futureRight = pool.submit(callableRight);
        List<Edge> edges = new ArrayList<>();
        edges.addAll(futureBot.get());
        edges.addAll(futureLeft.get());
        edges.addAll(futureRight.get());
        int way = 1;
        switch (way) {
            case 1:
                out.writeObject(edges);
                break;
            case 2:
                for (Edge e : edges
                        ) {
                    out.writeObject(e);
                    out.flush();
                }
                break;
        }
        //out.writeObject(edges);
        time.setEnd();
        System.out.println(time.toString());
    }
}
