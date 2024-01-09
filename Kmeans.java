import java.io.*;
import java.util.*;
import java.util.List;

public class Kmeans {
    static final int numberOfPoints = 1200, runs = 15;
    private static List<Point> points = new ArrayList<>();
    private static final List<Centroid> centroids = new ArrayList<>();
    private static final ArrayList <Double> listOfErrors = new ArrayList<>(runs);
    static final int[] M = {3, 6, 9, 12};

    public static void main(String[] args) {
        readDataset();

        for (int currentM : M) {
            runKMeans(currentM);

            try (PrintWriter writer = new PrintWriter(new FileWriter("errors.txt", true))) {
                writer.println(currentM + " " + Collections.min(listOfErrors) );
            } catch (IOException e) { e.printStackTrace(System.err); }


            try (PrintWriter writer = new PrintWriter(new FileWriter(currentM + "centroids.txt"))) {
                for (Centroid centroid : centroids)
                    writer.println(centroid.x + " " + centroid.y);
            } catch (IOException e) { e.printStackTrace(System.err); }

            try {
                Thread.sleep(1000); // delay
            } catch (InterruptedException e) { e.printStackTrace(System.err); }
        }
    }

    private static void readDataset() {
        try (Scanner scanner = new Scanner(new File("SDO.txt"))) {
            int counter = 0;
            while (scanner.hasNextLine() && counter < numberOfPoints) {
                String line = scanner.nextLine();
                String[] coordinates = line.split(" ");

                if (coordinates.length == 2) {
                    double x = Double.parseDouble(coordinates[0]), y = Double.parseDouble(coordinates[1]);
                    points.add(new Kmeans.Point(x, y));
                    counter++;
                } else {
                    System.out.println("Invalid format on line " + (counter + 1));
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace(System.err);
        }
    }
    private static void runKMeans(int currentM) {
        int pos;
        listOfErrors.clear();
        boolean hasConverged;
        Centroid [][] mapCentroids = new Centroid[runs][currentM];
        Random rand = new Random();

        // Execute K-means
        for(int i = 0; i < runs; i++) {
            // Centroids stuff (Clear previous centroids)
            centroids.clear();
            for (int j = 0; j < currentM; j++) {
                int randP = rand.nextInt(points.size());
                centroids.add(new Centroid(points.get(randP).x, 0.0, points.get(randP).y, 0.0));
            }

            // Repeat until convergence
            while(true) {
                combinePointsWithCentroids(currentM);
                updateCentroids(currentM);
                hasConverged = false;
                for(int j = 0; j < currentM; j++) {
                    if (!convergence(j)) {
                        hasConverged = true;
                        break;
                    }
                }
                if (hasConverged) break;
            }

            // map centroids
            for (int k = 0; k < currentM; k++) mapCentroids[i][k] = new Centroid(centroids.get(k).x, 0.0, centroids.get(k).y, 0.0);

            // Handle error
            listOfErrors.add(SSE());
        }


        centroids.clear();
        System.out.println("\n|M = " + currentM + "|. K-means algorithm finished after " + runs + " runs!  >best error: " + Collections.min(listOfErrors) + "<\n\nBest centroids by error:");
        pos = listOfErrors.indexOf(Collections.min(listOfErrors));

        for (int i = 0; i < currentM; i++) centroids.add(new Centroid(mapCentroids[pos][i].x, 0, mapCentroids[pos][i].y, 0));
        for (int i = 0; i < currentM; i++) System.out.println("(" + (i+1) + ")  " + mapCentroids[pos][i].x + " " + mapCentroids[pos][i].y);
        System.out.println("==========================================");
    }
    private static void combinePointsWithCentroids(int M) {
        for (Point point : points) {
            if(point == null) continue;  // Skip null points

            double minDistance = Double.MAX_VALUE;
            int assignedCentroidIndex = -1;
            for (int j = 0; j < M; j++) {
                double distance = euclideanDistance(point, centroids.get(j));

                if (distance < minDistance) {
                    minDistance = distance;
                    assignedCentroidIndex = j;
                }
            }
            point.clusterIndex = assignedCentroidIndex;
        }
    }
    private static boolean convergence(int j) { return centroids.get(j).lastStateOfX != centroids.get(j).x || centroids.get(j).lastStateOfY != centroids.get(j).y; }
    private static void updateCentroids(int M) {
        for (int j = 0; j < M; j++) {
            double totalX = 0, totalY = 0;
            int count = 0;

            for (Point point : points) {
                if (point == null) continue;  // Skip null points

                if (point.clusterIndex == j) {
                    totalX = totalX + point.x;
                    totalY = totalY + point.y;
                    count++;
                }
            }

            if (count > 0) {
                centroids.get(j).lastStateOfX = centroids.get(j).x;  // Update before changing x
                centroids.get(j).x = totalX / count;
                centroids.get(j).lastStateOfY = centroids.get(j).y;  // Update before changing y
                centroids.get(j).y = totalY / count;
            } else {
                centroids.get(j).lastStateOfX = centroids.get(j).x;  // Update before changing x
                centroids.get(j).lastStateOfY = centroids.get(j).y;  // Update before changing y
            }
        }
    }
    private static double euclideanDistance(Point p1, Centroid p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }
    private static double SSE() {
        double tempSSE = 0;
        ErrorCalculator calculator = new ErrorCalculator(0);

        for (Point point : points) {
            if (point == null) continue;  // Skip null points

            double dx = point.x - centroids.get(point.clusterIndex).x;
            double dy = point.y - centroids.get(point.clusterIndex).y;

            // Square the differences before summing
            double maxDistance = Math.sqrt(dx * dx + dy * dy);

            calculator.addMaxDistance(maxDistance);
            tempSSE = tempSSE + calculator.getError();
        }
        return tempSSE;
    }
    private static class ErrorCalculator {
        double error;
        public ErrorCalculator(double initialError) { this.error = initialError; }
        public void addMaxDistance(double maxDistance) { error = error + maxDistance; }
        public double getError() { return error; }
    }
    static class Point {
        double x, y;
        int clusterIndex;
        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
    private static class Centroid {
        double x, y, lastStateOfX, lastStateOfY;

        Centroid(double x, double lastStateOfX, double y, double lastStateOfY) {
            this.x = x;
            this.lastStateOfX = lastStateOfX;
            this.y = y;
            this.lastStateOfY = lastStateOfY;
        }
    }
}