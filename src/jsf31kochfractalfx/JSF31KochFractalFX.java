/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsf31kochfractalfx;

import Fractal.Edge;
import calculate.*;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import timeutil.TimeStamp;

/**
 *
 * @author Nico Kuijpers
 */
public class JSF31KochFractalFX extends Application {

    //sockets
    private static String host = "localhost";
    private static int way = 2;
    // Zoom and drag
    private double zoomTranslateX = 0.0;
    private double zoomTranslateY = 0.0;
    private double zoom = 1.0;
    private double startPressedX = 0.0;
    private double startPressedY = 0.0;
    private double lastDragX = 0.0;
    private double lastDragY = 0.0;

    // Koch manager
    // TO DO: Create class KochManager in package calculate
    private KochManager kochManager;

    // Current level of Koch fractal
    private int currentLevel = 1;

    // Labels for level, nr edges, calculation time, and drawing time
    private Label labelLevel;
    private Label labelNrEdges;
    private Label labelNrEdgesText;
    private Label labelCalc;
    private Label labelCalcText;
    private Label labelDraw;
    private Label labelDrawText;

    // Koch panel and its size
    private Canvas kochPanel;
    private final int kpWidth = 500;
    private final int kpHeight = 500;

    private ProgressBar barL;
    private ProgressBar barR;
    private ProgressBar barB;
    private Label labelL;
    private Label labelR;
    private Label labelB;

    @Override
    public void start(Stage primaryStage) throws Exception {


        // Define grid pane
        GridPane grid;
        grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        // For debug purposes
        // Make de grid lines visible
        // grid.setGridLinesVisible(true);

        // Drawing panel for Koch fractal
        kochPanel = new Canvas(kpWidth,kpHeight);
        grid.add(kochPanel, 0, 3, 25, 1);

        // Labels to present number of edges for Koch fractal
        labelNrEdges = new Label("Nr edges:");
        labelNrEdgesText = new Label();
        grid.add(labelNrEdges, 0, 0, 4, 1);
        grid.add(labelNrEdgesText, 3, 0, 22, 1);

        // Labels to present time of calculation for Koch fractal
        labelCalc = new Label("Calculating:");
        labelCalcText = new Label();
        grid.add(labelCalc, 0, 1, 4, 1);
        grid.add(labelCalcText, 3, 1, 22, 1);

        // Labels to present time of drawing for Koch fractal
        labelDraw = new Label("Drawing:");
        labelDrawText = new Label();
        grid.add(labelDraw, 0, 2, 4, 1);
        grid.add(labelDrawText, 3, 2, 22, 1);

        // Label to present current level of Koch fractal
        labelLevel = new Label("Level: " + currentLevel);
        grid.add(labelLevel, 0, 6);

        // Button to increase level of Koch fractal
        Button buttonIncreaseLevel = new Button();
        buttonIncreaseLevel.setText("Start Drawing");
        buttonIncreaseLevel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    startDrawingButtonActoinPerformed(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        grid.add(buttonIncreaseLevel, 3, 6);

        // Button to decrease level of Koch fractal
        Button buttonDecreaseLevel = new Button();
        buttonDecreaseLevel.setText("Decrease Level");
        buttonDecreaseLevel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    decreaseLevelButtonActionPerformed(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        grid.add(buttonDecreaseLevel, 5, 6);

        // Button to fit Koch fractal in Koch panel
        Button buttonFitFractal = new Button();
        buttonFitFractal.setText("Fit Fractal");
        buttonFitFractal.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                fitFractalButtonActionPerformed(event);
            }
        });
        grid.add(buttonFitFractal, 14, 6);

        barL = new ProgressBar();
        barR = new ProgressBar();
        barB = new ProgressBar();
        labelL = new Label("Left");
        labelR = new Label("Right");
        labelB = new Label("Bottom");

        grid.add(barL, 1, 7);
        grid.add(barR, 1, 8);
        grid.add(barB, 1, 9);
        grid.add(labelL, 0, 7);
        grid.add(labelR, 0, 8);
        grid.add(labelB, 0, 9);


        // Add mouse clicked event to Koch panel
        kochPanel.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        kochPanelMouseClicked(event);
                    }
                });

        // Add mouse pressed event to Koch panel
        kochPanel.addEventHandler(MouseEvent.MOUSE_PRESSED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        kochPanelMousePressed(event);
                    }
                });

        // Add mouse dragged event to Koch panel
        kochPanel.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                kochPanelMouseDragged(event);
            }
        });

        // Create Koch manager and set initial level
        resetZoom();
        kochManager = new KochManager(this,host,way);
        kochManager.changeLevel(currentLevel);

        // Create the scene and add the grid pane
        Group root = new Group();
        Scene scene = new Scene(root, kpWidth+50, kpHeight+170);
        root.getChildren().add(grid);

        // Define title and assign the scene for main window
        primaryStage.setTitle("Koch Fractal");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void clearKochPanel() {
        GraphicsContext gc = kochPanel.getGraphicsContext2D();
        gc.clearRect(0.0,0.0,kpWidth,kpHeight);
        gc.setFill(Color.BLACK);
        gc.fillRect(0.0,0.0,kpWidth,kpHeight);
    }

    public void drawEdge(Edge e) {
        GraphicsContext gc = kochPanel.getGraphicsContext2D();

        // Adjust edge for zoom and drag
        Edge e1 = edgeAfterZoomAndDrag(e);
        int r = e.color.getRed();
        int g = e.color.getGreen();
        int b = e.color.getBlue();
        int a = e.color.getAlpha();
        double opacity = a / 255.0 ;
        javafx.scene.paint.Color efx = javafx.scene.paint.Color.rgb(r, g, b, opacity);
        // Set line color
        gc.setStroke(efx);

        // Set line width depending on level
        if (currentLevel <= 3) {
            gc.setLineWidth(2.0);
        }
        else if (currentLevel <=5 ) {
            gc.setLineWidth(1.5);
        }
        else {
            gc.setLineWidth(1.0);
        }

        // Draw line
        gc.strokeLine(e1.X1,e1.Y1,e1.X2,e1.Y2);
    }

    public void BindPropL(Task t){
        barL.progressProperty().bind(t.progressProperty());
        labelL.textProperty().bind(t.messageProperty());
    }

    public void BindPropR(Task t){
        barR.progressProperty().bind(t.progressProperty());
        labelR.textProperty().bind(t.messageProperty());
    }

    public void BindPropB(Task t){
        barB.progressProperty().bind(t.progressProperty());
        labelB.textProperty().bind(t.messageProperty());
    }

    public void setTextNrEdges(String text) {
        labelNrEdgesText.setText(text);
    }

    public void setTextCalc(String text) {
        labelCalcText.setText(text);
    }

    public void setTextDraw(String text) {
        labelDrawText.setText(text);
    }

    private AtomicBoolean isDrawing = new AtomicBoolean(false);
    public void requestDrawEdges(){
        requestDrawEdges(false);
    }
    public void requestDrawEdges(boolean force) {
        //if(!force && !isDrawing.compareAndSet(false, true))return;

        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                kochManager.drawEdges();
            }
        });
    }

    public void doneDrawing(){
        isDrawing.set(false);
    }

    private void startDrawingButtonActoinPerformed(ActionEvent event) throws Exception {
        if (currentLevel < 12) {
            // resetZoom();
            currentLevel++;
            labelLevel.setText("Level: " + currentLevel);
            kochManager.changeLevel(currentLevel);
        }
    }
    private void dstartDrawingButtonActoinPerformed(ActionEvent event) throws  Exception {
        TimeStamp ts = new TimeStamp();
        int way =1;
        ObjectInputStream ois = null;
        ts.setBegin();
        switch(way){
            case 1:
                FileInputStream inputStream = new FileInputStream("6.txt");
                ois = new ObjectInputStream(inputStream);
                break;
            case 2:
                FileInputStream inputStream2 = new FileInputStream("6.bin");
                InputStream buffer = new BufferedInputStream(inputStream2);
                ois = new ObjectInputStream(buffer);
        }

        List<Edge> DrawingEdges = (List<Edge>)ois.readObject();
        ts.setEnd();
        System.out.println(ts.toString());
        clearKochPanel();
        for (Edge e : DrawingEdges
             ) {
            drawEdge(e);
        }
    }
    private void decreaseLevelButtonActionPerformed(ActionEvent event) throws Exception {
        if (currentLevel > 1) {
            // resetZoom();
            currentLevel--;
            labelLevel.setText("Level: " + currentLevel);
            kochManager.changeLevel(currentLevel);
        }
    }

    private void fitFractalButtonActionPerformed(ActionEvent event) {
        resetZoom();
        kochManager.drawEdges();
    }

    private void kochPanelMouseClicked(MouseEvent event) {
        if (Math.abs(event.getX() - startPressedX) < 1.0 &&
                Math.abs(event.getY() - startPressedY) < 1.0) {
            double originalPointClickedX = (event.getX() - zoomTranslateX) / zoom;
            double originalPointClickedY = (event.getY() - zoomTranslateY) / zoom;
            if (event.getButton() == MouseButton.PRIMARY) {
                zoom *= 2.0;
            } else if (event.getButton() == MouseButton.SECONDARY) {
                zoom /= 2.0;
            }
            zoomTranslateX = (int) (event.getX() - originalPointClickedX * zoom);
            zoomTranslateY = (int) (event.getY() - originalPointClickedY * zoom);
            kochManager.drawEdges();
        }
    }

    private void kochPanelMouseDragged(MouseEvent event) {
        zoomTranslateX = zoomTranslateX + event.getX() - lastDragX;
        zoomTranslateY = zoomTranslateY + event.getY() - lastDragY;
        lastDragX = event.getX();
        lastDragY = event.getY();
        kochManager.drawEdges();
    }

    private void kochPanelMousePressed(MouseEvent event) {
        startPressedX = event.getX();
        startPressedY = event.getY();
        lastDragX = event.getX();
        lastDragY = event.getY();
    }

    private void resetZoom() {
        int kpSize = Math.min(kpWidth, kpHeight);
        zoom = kpSize;
        zoomTranslateX = (kpWidth - kpSize) / 2.0;
        zoomTranslateY = (kpHeight - kpSize) / 2.0;
    }

    private Edge edgeAfterZoomAndDrag(Edge e) {
        int r = e.color.getRed();
        int g = e.color.getGreen();
        int b = e.color.getBlue();
        int a = e.color.getAlpha();
        double opacity = a / 255.0 ;
        javafx.scene.paint.Color efx = javafx.scene.paint.Color.rgb(r, g, b, opacity);
        // Set line color
        return new Edge(
                e.X1 * zoom + zoomTranslateX,
                e.Y1 * zoom + zoomTranslateY,
                e.X2 * zoom + zoomTranslateX,
                e.Y2 * zoom + zoomTranslateY,
                efx);
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 0) {
            if (!args[0].isEmpty()) {
                host = args[0];
            }
            if (!args[1].isEmpty()) {
                way = Integer.parseInt(args[1]);
            }
        }
        launch(args);
    }
}
