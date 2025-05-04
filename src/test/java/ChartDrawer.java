import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.util.Map;

public class ChartDrawer {

    public static void main(String[] args) {
        // Тестовий запуск
        VertexCoverTest.main(args);

        // Побудова графіків
        drawChart("Час виконання алгоритмів", "Кількість вершин", "Час (мс)",
                VertexCoverTest.greedyTime, VertexCoverTest.approxTime, VertexCoverTest.backtrackingTime);

        drawChart("Розмір покриття", "Кількість вершин", "Кількість вершин у покритті",
                VertexCoverTest.greedyData, VertexCoverTest.approxData, VertexCoverTest.backtrackingData);
    }

    public static void drawChart(String title, String xLabel, String yLabel,
                                 Map<Integer, ? extends Number> greedy,
                                 Map<Integer, ? extends Number> approx,
                                 Map<Integer, ? extends Number> backtracking) {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Integer v : greedy.keySet()) {
            dataset.addValue(greedy.get(v), "Жадібний", v);
            dataset.addValue(approx.get(v), "Approx", v);
            dataset.addValue(backtracking.get(v), "Backtracking", v);
        }

        JFreeChart chart = ChartFactory.createLineChart(
                title,
                xLabel,
                yLabel,
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ChartPanel(chart));
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}