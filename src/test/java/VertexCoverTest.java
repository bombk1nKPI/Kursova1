import com.bombk1n.coursework1.model.Graph;
import com.bombk1n.coursework1.util.VertexCoverAlgorithms;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class VertexCoverTest {

    // Use TreeMap to keep entries sorted by vertex count
    public static TreeMap<Integer, Integer> greedyData = new TreeMap<>();
    public static TreeMap<Integer, Integer> approxData = new TreeMap<>();
    public static TreeMap<Integer, Integer> backtrackingData = new TreeMap<>();
    public static TreeMap<Integer, Long> greedyTime = new TreeMap<>();
    public static TreeMap<Integer, Long> approxTime = new TreeMap<>();
    public static TreeMap<Integer, Long> backtrackingTime = new TreeMap<>();

    public static void main(String[] args) {
        // Test configurations - add more data points for better trend visualization
        int[] vertexCounts = {0,5, 10, 15, 20, 25};
        int[] edgeCounts = {0,10, 20, 30, 40, 50};

        // Run tests for each graph configuration
        for (int i = 0; i < vertexCounts.length; i++) {
            int v = vertexCounts[i];
            // Calculate a reasonable number of edges based on vertex count
            int e = i < edgeCounts.length ? edgeCounts[i] : v * (v - 1) / 4;

            // Generate graph
            Graph graph = Graph.generateRandomGraph(v, e);

            System.out.println("=== Testing graph: " + v + " vertices, " + e + " edges ===");

            // Test each algorithm
            testAlgorithm("Greedy", graph, VertexCoverAlgorithms::greedy, greedyData, greedyTime, v);
            testAlgorithm("Approx-Vertex-Cover", graph, VertexCoverAlgorithms::approx, approxData, approxTime, v);

            // For larger graphs, backtracking might take too long
            if (v <= 20) {
                testAlgorithm("Backtracking", graph, VertexCoverAlgorithms::backtracking, backtrackingData, backtrackingTime, v);
            }

            System.out.println();
        }

        // Display results with improved charts
        createTimeChart("Execution Time vs Vertex Count", greedyTime, approxTime, backtrackingTime);
        createSizeChart("Vertex Cover Size vs Vertex Count", greedyData, approxData, backtrackingData);
    }

    private static void testAlgorithm(String name, Graph graph, AlgorithmFunction function,
                                      Map<Integer, Integer> data, Map<Integer, Long> timeData, int v) {
        // Measure execution time
        long startTime = System.nanoTime();
        Set<Integer> cover = function.run(graph);
        long endTime = System.nanoTime();

        // Calculate elapsed time in milliseconds
        long elapsedMs = (endTime - startTime) / 1_000_000;

        // Store results
        data.put(v, cover.size());
        timeData.put(v, elapsedMs);

        // Print results to console
        System.out.printf("Algorithm: %-20s | Cover Size: %3d | Execution Time: %d ms\n",
                name, cover.size(), elapsedMs);
    }

    private static void createTimeChart(String title,
                                        TreeMap<Integer, Long> greedyData,
                                        TreeMap<Integer, Long> approxData,
                                        TreeMap<Integer, Long> backtrackingData) {
        XYSeriesCollection dataset = new XYSeriesCollection();

        // Create series for each algorithm
        XYSeries greedySeries = new XYSeries("Greedy");
        XYSeries approxSeries = new XYSeries("Approx");
        XYSeries backtrackingSeries = new XYSeries("Backtracking");

        // Add data points to series
        for (Map.Entry<Integer, Long> entry : greedyData.entrySet()) {
            greedySeries.add(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<Integer, Long> entry : approxData.entrySet()) {
            approxSeries.add(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<Integer, Long> entry : backtrackingData.entrySet()) {
            backtrackingSeries.add(entry.getKey(), entry.getValue());
        }

        // Add series to dataset
        dataset.addSeries(greedySeries);
        dataset.addSeries(approxSeries);
        dataset.addSeries(backtrackingSeries);

        // Create chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                "Vertex Count",
                "Time (ms)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        // Customize chart
        XYPlot plot = chart.getXYPlot();

        // Determine if log scale is needed (if large time differences exist)
        boolean useLogScale = shouldUseLogScale(greedyData, approxData, backtrackingData);

        if (useLogScale) {
            LogAxis logAxis = new LogAxis("Time (ms) - Log Scale");
            logAxis.setBase(10);
            logAxis.setSmallestValue(0.1); // Start near zero but prevent log(0) error
            plot.setRangeAxis(logAxis);
        } else {
            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            // Explicitly force y-axis to start from zero
            rangeAxis.setAutoRangeIncludesZero(true);
            rangeAxis.setAutoRange(false);

            // Find maximum value for upper bound
            double maxTime = 1.0; // Default minimum if no data
            for (Map<Integer, Long> map : Arrays.asList(greedyData, approxData, backtrackingData)) {
                for (Long value : map.values()) {
                    if (value > maxTime) maxTime = value;
                }
            }

            // Set bounds with a bit of padding at the top
            rangeAxis.setRange(0, maxTime * 1.1);
        }

        // Set integer tickmarks for X-axis
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        // Ensure X-axis also starts at or before the first data point
        if (!greedyData.isEmpty() || !approxData.isEmpty() || !backtrackingData.isEmpty()) {
            int minVertex = Integer.MAX_VALUE;
            for (Map<Integer, ?> map : Arrays.asList(greedyData, approxData, backtrackingData)) {
                if (!map.isEmpty()) {
                    minVertex = Math.min(minVertex, map.keySet().stream().min(Integer::compare).orElse(Integer.MAX_VALUE));
                }
            }
            if (minVertex != Integer.MAX_VALUE) {
                domainAxis.setLowerBound(Math.max(0, minVertex - 1));
            }
        }

        // Customize renderer to show shapes at data points
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesPaint(1, Color.GREEN);
        renderer.setSeriesPaint(2, Color.RED);
        renderer.setBaseShapesVisible(true);
        renderer.setBaseStroke(new BasicStroke(2.0f));
        plot.setRenderer(renderer);

        // Display the chart
        displayChart(chart, title);
    }

    private static void createSizeChart(String title,
                                        TreeMap<Integer, Integer> greedyData,
                                        TreeMap<Integer, Integer> approxData,
                                        TreeMap<Integer, Integer> backtrackingData) {
        XYSeriesCollection dataset = new XYSeriesCollection();

        // Create series for each algorithm
        XYSeries greedySeries = new XYSeries("Greedy");
        XYSeries approxSeries = new XYSeries("Approx");
        XYSeries backtrackingSeries = new XYSeries("Backtracking");

        // Add data points to series
        for (Map.Entry<Integer, Integer> entry : greedyData.entrySet()) {
            greedySeries.add(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<Integer, Integer> entry : approxData.entrySet()) {
            approxSeries.add(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<Integer, Integer> entry : backtrackingData.entrySet()) {
            backtrackingSeries.add(entry.getKey(), entry.getValue());
        }

        // Add series to dataset
        dataset.addSeries(greedySeries);
        dataset.addSeries(approxSeries);
        dataset.addSeries(backtrackingSeries);

        // Create chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                "Vertex Count",
                "Cover Size",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        // Customize chart
        XYPlot plot = chart.getXYPlot();

        // Find the maximum cover size for setting the Y-axis upper bound
        double maxCoverSize = 1.0; // Default minimum if no data
        for (Map<Integer, Integer> map : Arrays.asList(greedyData, approxData, backtrackingData)) {
            for (Integer value : map.values()) {
                if (value > maxCoverSize) maxCoverSize = value;
            }
        }

        // Set Y-axis to explicitly start from zero
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setAutoRange(false);
        rangeAxis.setRange(0, maxCoverSize * 1.1); // Add 10% padding at top
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        // Set integer tickmarks for X-axis
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        // Ensure X-axis also starts at or before the first data point
        if (!greedyData.isEmpty() || !approxData.isEmpty() || !backtrackingData.isEmpty()) {
            int minVertex = Integer.MAX_VALUE;
            for (Map<Integer, ?> map : Arrays.asList(greedyData, approxData, backtrackingData)) {
                if (!map.isEmpty()) {
                    minVertex = Math.min(minVertex, map.keySet().stream().min(Integer::compare).orElse(Integer.MAX_VALUE));
                }
            }
            if (minVertex != Integer.MAX_VALUE) {
                domainAxis.setLowerBound(Math.max(0, minVertex - 1));
            }
        }

        // Customize renderer to show shapes at data points
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesPaint(1, Color.GREEN);
        renderer.setSeriesPaint(2, Color.RED);
        renderer.setBaseShapesVisible(true);
        renderer.setBaseStroke(new BasicStroke(2.0f));
        plot.setRenderer(renderer);

        // Display the chart
        displayChart(chart, title);
    }

    private static boolean shouldUseLogScale(Map<Integer, Long> ... dataMaps) {
        List<Long> allTimes = new ArrayList<>();
        for (Map<Integer, Long> dataMap : dataMaps) {
            allTimes.addAll(dataMap.values());
        }

        if (allTimes.isEmpty()) return false;

        long min = Collections.min(allTimes);
        long max = Collections.max(allTimes);

        // Use log scale if max time is more than 20x the min time
        return max > min * 20 && min > 0;
    }

    private static void displayChart(JFreeChart chart, String title) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(title);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(800, 500));
            frame.add(chartPanel);
            frame.pack();
            frame.setVisible(true);
        });
    }

    // Functional interface for algorithms
    @FunctionalInterface
    interface AlgorithmFunction {
        Set<Integer> run(Graph g);
    }
}