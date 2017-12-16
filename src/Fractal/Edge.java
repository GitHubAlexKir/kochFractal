/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Fractal;

import javafx.scene.paint.Color;

import java.io.Serializable;

/**
 *
 * @author Peter Boots
 */
public class Edge implements Serializable {
    public double X1, Y1, X2, Y2;
    public java.awt.Color color;

    public Edge(double X1, double Y1, double X2, double Y2, Color fx) {
        this.X1 = X1;
        this.Y1 = Y1;
        this.X2 = X2;
        this.Y2 = Y2;
        this.color = new java.awt.Color((float) fx.getRed(),
                (float) fx.getGreen(),
                (float) fx.getBlue(),
                (float) fx.getOpacity());
    }
}
