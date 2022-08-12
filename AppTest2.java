import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class AppTest2 {
    static Random rnd = new Random();

    public static class Logic {
        static String funcName = "График функции";
        static XYSeries series1 = new XYSeries(funcName);
        static XYSeries series2 = new XYSeries(funcName);
        static int sumElements = 0;

        //генерация последовательности
        public static ArrayList<Double> A() {
            double x;
            double y;
            double scale = 1000;
            ArrayList<Double> test = new ArrayList<>();
            ArrayList<Double> testSorted;
            for (int i = 0; i < 1000; i ++) {
                x = Math.random();
                //y = 0.5 + Math.pow(-1, i) * Math.pow(1 - x, Math.sin(x * Math.PI / 2)) / 2;
                //y = Math.pow(1 - x, Math.sin(x * Math.PI / 2));
                test.add(x);
                //System.out.println("x = " + x + ", " + (i+1) + "-ое случайное число = " + y);
            }
            //Collections.sort(test);
            testSorted = new ArrayList<>(test);
            return test;
        }

        //АКФ и анализ ДПСЧ
        public static void B(double scale, ArrayList<Double> counts, double Dx) {
            final int shift = 7;
            final int range = (int) scale - shift;
            double R = ((double) 1 / range);
            double r = 0;
            for (int j = 0; j < range; j++) {
                r += (counts.get(j) - 0.5) * (counts.get(j + shift) - 0.5);
            }
            R *= r;
            System.out.println("Центрированная автокорреляционная функция = " + R + ";");
            double normalizedR = R / Dx;
            System.out.println("Нормированная автокорреляционная функция = " + normalizedR + ";");
            if (-0.1 < normalizedR && normalizedR < 0.1) {
                System.out.println("Хороший ДПСЧ;" + '\n');
            } else {
                System.out.println("Плохой ДПСЧ;" + '\n');
            }
        }

        //Вычисление Mx, Dx и обработка данных для создания графика плотности
        public static Double Dx(ArrayList<Double> countsScaledSorted, double scale) {
            double Mx = 0;
            double Dx = 0;
            ArrayList<Integer> density = new ArrayList<>();
            //System.out.println("Плотность распределения:");
            for(double i = 0; i < 1; i += 0.001) {
                int count = 0;
                //тут ошибка???
                for (double j : countsScaledSorted) {
                    if (((double) ((int) (i * scale))) / scale == j) {
                        count++;
                    }
                }
                double ver;
                ver = (double) count / scale;
                try {
                    double mx;
                    double dx = 0;
                    mx = i * ver;
                    if (mx != 0) {
                        dx = Math.pow(countsScaledSorted.get((int)(i * scale)) - 0.5, 2) * ver;
                    }
                    Mx += mx;
                    Dx += dx;
                } catch (ArithmeticException e) {
                System.out.println("Ошибка!");
                }
                sumElements += count;
                density.add(count);
            //System.out.printf("x = %s - %s раз;", ((double) ((int) (i * scale))) / scale, count);
            //System.out.println();
            }
            for(double i = 0; i < 1; i += 0.001) {
                //Collections.sort(density);
                series2.add(i, density.get((int)(i * scale)));
            }
            System.out.println(density);
            System.out.printf("\n"+"Элементов = %d, Mx = %.2f, Dx = %.2f;",sumElements,Mx,Dx);
            System.out.println();
            return Dx;
        }

        //округление до знака, равному значению places
        public static double round(double value, int places) {
            if (places < 0) throw new IllegalArgumentException();

            long factor = (long) Math.pow(10, places);
            value = value * factor;
            long tmp = Math.round(value);
            return (double) tmp / factor;
        }

        //вычисляет плотность распределения
        public static double density(ArrayList<Double> countsScaledSorted, int k, int N) {
            double density = 0;
            double dx = (countsScaledSorted.get(N-1)-countsScaledSorted.get(0)) / (k+2);
            ArrayList<Double> result = new ArrayList<>();
            ArrayList<Double> resultX = new ArrayList<>();
            double x;
            int j = 0;
            for(int i = 0; i < dx; i++) {
                x = countsScaledSorted.get(0) + i * dx;
                resultX.add(x);
                if(x > countsScaledSorted.get(i + 1)) {
                    i += 1;
                }
                result.add(i + (x - countsScaledSorted.get(i) / countsScaledSorted.get(i + 1) - countsScaledSorted.get(i)));
            }
            density = 0.5 / dx / N;
            return density;
        }
    }

    //построение графиков и вывод информации
    public static class Graphic extends JPanel {

        public Graphic() {
            String frameName = "Тестовое задание";
            String graphicName = "y = F(x)";
            String densityName = "y = F'(x) = f(x)";
            final double scale = 1000;

            ArrayList<Double> counts;
            counts = Logic.A();

            for (double i = 0; i < 1; i += 0.001) {
                Logic.series1.add(i, counts.get((int)(i*scale)));
            }
            System.out.println("\n" + "Отсчёты - " + counts + ";");

            ArrayList<Double> countsPseudoScaled = new ArrayList<>(counts);
            Collections.sort(countsPseudoScaled);
            System.out.println("Стандартная сортировка - " + countsPseudoScaled + ";");

            ArrayList<Double> countsPreScaled;
            countsPreScaled = new ArrayList<>(counts);
            ArrayList<Double> countsScaled = new ArrayList<>();
            double cur;
            for (double i = 0; i < 1; i += 0.001) {
                cur = Logic.round((countsPreScaled.get((int)(i*scale))), 3);
                countsScaled.add(cur);
            }
            System.out.println("Нестандартное округление - " + countsScaled + ";");

            ArrayList<Double> countsScaledSorted;
            countsScaledSorted = new ArrayList<>(countsScaled);
            Collections.sort(countsScaledSorted);
            System.out.println("Нестандартная сортировка - " + countsScaledSorted + ";");

            /*Logic.series1.clear();
            for (double i = 0; i < 1; i += 0.001) {
                Logic.series1.add(i, countsScaledSorted.get((int)(i*scale)));
            }*/

            Double Dx;
            Dx = Logic.Dx(countsScaled, scale);

            Logic.B(scale, countsScaledSorted, Dx);

            System.out.printf("Плотность = %s", Logic.round(Logic.density(countsScaledSorted, 100, Logic.sumElements), 3));

            XYDataset xyDataset1 = new XYSeriesCollection(Logic.series1);
            XYDataset xyDataset2 = new XYSeriesCollection(Logic.series2);
            JFreeChart chart1 = ChartFactory.createXYLineChart(graphicName, "x", "y", xyDataset1, PlotOrientation.VERTICAL, true, true, true);
            JFreeChart chart2 = ChartFactory.createXYLineChart(densityName, "x", "y", xyDataset2, PlotOrientation.VERTICAL, true, true, true);

            JFrame frame = new JFrame(frameName);
            frame.getContentPane().add(new ChartPanel(chart1));
            frame.getContentPane().add(new ChartPanel(chart2));
            frame.setLayout(new GridLayout());
            frame.setSize(1200, 600);
            frame.setLocation(360, 240);
            frame.setVisible(true);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        }
    }

    public static void main(String[] args) {
        double start, stop, totalTime;
        start = System.currentTimeMillis();

        Graphic g = new Graphic();

        stop = System.currentTimeMillis();
        totalTime = (stop-start) / 1000.0;
        System.out.println('\n' + "Всего прошло " + totalTime + " секунды.");
    }
}
