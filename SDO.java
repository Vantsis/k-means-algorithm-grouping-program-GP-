import java.io.*;
import java.util.Random;

public class SDO {
    static final int[] totalPoints = {150, 150, 150, 150, 150, 75, 75, 75, 75, 150};
    static final double[][] coordinates = {
            {0.8, 1.2, 0.8, 1.2},
            {0.0, 0.5, 0.0, 0.5},
            {1.5, 2.0, 0.0, 0.5},
            {0.0, 0.5, 1.5, 2.0},
            {1.5, 2.0, 1.5, 2.0},
            {0.0, 0.4, 0.8, 1.2},
            {1.6, 2.0, 0.8, 1.2},
            {0.8, 1.2, 0.3, 0.7},
            {0.8, 1.2, 1.3, 1.7},
            {0.0, 2.0, 0.0, 2.0}
    };

    public static void generateDataset(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (int i = 0; i < totalPoints.length; i++) {
                generateRandomPoints(writer, totalPoints[i], coordinates[i]);
            }
        }
        catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private static void generateRandomPoints(PrintWriter writer, int totalPoints, double[] coordinates) {
        double minX = coordinates[0], maxX = coordinates[1], minY = coordinates[2], maxY = coordinates[3];
        Random rand = new Random();

        for (int i = 0; i < totalPoints; i++) {
            double x = minX + (maxX - minX) * rand.nextDouble();
            double y = minY + (maxY - minY) * rand.nextDouble();
            writer.println(x + " " + y);
        }
    }

    public static void main (String [] args) {
        generateDataset("SDO.txt");
    }
}