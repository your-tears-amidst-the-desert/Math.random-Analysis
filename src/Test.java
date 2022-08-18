//Как итог работы программы получаем весьма хорошие результаты для оценки качества работы Math.random().
//Значения автокорреляционной функции всегда значительно < |0.1|, лишь изредка приближаясь к пороговому значению, 
//мат. ожидание же в худшем случае колеблется в пределах +-0.018 от ожидаемого, в среднем +-0.008(строго говоря, это зависит от степени округления отсчётов,
//где опытным путём наиболее точные показатели были получены для степени округления равной 7му разряду.
//На основании данных наблюдений, реализацию метода Math.random() можно счесть положительной.
//Условие для проверки качества ДПСЧ было взято с сайта - https://intuit.ru/studies/courses/623/479/lecture/21088?page=3 

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
import java.util.List;

public class Analysis {
    static String funcName = "График функции";
    static String frameName = "Анализ Math.random()";
    static String graphicName = "y = F(x)";
    static XYSeries series1 = new XYSeries(funcName);
    static XYSeries series2 = new XYSeries(funcName);
    static String densityName = "Отсортированная последовательность СВ";

    //округление до нужного разряда
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    //генерация последовательности
    public static List<Double> Generate(int scale) {
        ArrayList<Double> counts = new ArrayList<>();
        double x;
        for(int i = 0; i < scale; i++) {
            x = Math.random();
            counts.add(x);
        }
        return counts;
    }

    //вычисление центрированной корреляционной функции
    public static Double R(int range, List<Double> counts, int shift) {
        double sum = 0;
        double R = ((double) 1 / range);
        ArrayList<Double> r = new ArrayList<>();
        double cur;
        for (int j = 0; j < counts.size() - shift; j++) {
            cur = ((counts.get(j) - 0.5) * (counts.get(j + shift) - 0.5));
            r.add(cur);
            sum += r.get(j);
        }
        R *= sum;
        return R;
    }

    //вычисление мат. ожидания
    public static Double Mx(List<Double> countsSorted, int scale, List<Double> counts) {
        double Mx = 0;
        List<Double> chance = new ArrayList<>();
        double mx = 0;
        for(double i = 0; i < (double)(countsSorted.size() / scale); i += 0.001) {
            int count = 0;
            for(int j = 0; j < scale - 1; j++) {
                if (round(countsSorted.get((int)(i * scale)), 7) == round(countsSorted.get(j+1), 7)) {
                    count++;
                }
            }
            chance.add( (double)count / scale );
            try {
                mx = counts.get((int)(i * scale)) * chance.get((int)(i * scale));
                Mx += mx;
            } catch (ArithmeticException e) {
                System.out.println("Ошибка!");
            }
        }
        return Mx;
    }

    //вычисление дисперсии
    public static Double Dx(List<Double> countsSorted, int scale, List<Double> counts) {
        double Mx = 0;
        double Dx = 0;
        ArrayList<Integer> density = new ArrayList<>();
        List<Double> chance = new ArrayList<>();
        double mx = 0;
        double dx = 0;
        for(double i = 0; i < (double)(countsSorted.size() / scale); i += 0.001) {
            int count = 0;
            for(int j = 0; j < scale - 1; j++) {
                if (round(countsSorted.get((int)(i * scale)), 7) == round(countsSorted.get(j+1), 7)) {
                    count++;
                }
            }
            chance.add( (double)count / scale );
            try {
                mx = counts.get((int)(i * scale)) * chance.get((int)(i * scale));
                if (mx != 0) {
                    dx = Math.pow(countsSorted.get((int)(i * scale)) - 0.5, 2) * chance.get((int)(i * scale));
                }
                Mx += mx;
                Dx += dx;
            } catch (ArithmeticException e) {
                System.out.println("Ошибка!");
            }
            density.add(count);
        }
        return Dx;
    }

    //рисуем графики
    public static void Graphic(List<Double> counts, int scale, List<Double> countsSorted) {
        //добавление значений x и y
        for (double i = 0; i < 1; i += 0.001) {
            series1.add(i, counts.get((int)(i*scale)));
            //series2.add(i, density.get((int)(i * scale))); количество попаданий в конкретную точку
            series2.add(i, countsSorted.get((int)(i * scale)));
        }

        XYDataset xyDataset1 = new XYSeriesCollection(series1);
        XYDataset xyDataset2 = new XYSeriesCollection(series2);
        JFreeChart chart1 = ChartFactory.createXYLineChart(graphicName, "x", "y", xyDataset1, PlotOrientation.VERTICAL, true, true, true);
        JFreeChart chart2 = ChartFactory.createXYLineChart(densityName, "x", "y", xyDataset2, PlotOrientation.VERTICAL, true, true, true);

        //создание фрэйма и добавление в него графиков
        JFrame frame = new JFrame(frameName);
        frame.getContentPane().add(new ChartPanel(chart1));
        frame.getContentPane().add(new ChartPanel(chart2));
        frame.setLayout(new GridLayout());
        frame.setSize(1200, 600);
        frame.setLocation(360, 240);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        double start, stop, totalTime;
        start = System.currentTimeMillis();
        //размер выборки
        final int scale = 1000;
        //шаг
        final int shift = 7;
        final int range = scale - shift;
        //дисперсия
        double Dx = 0.0;
        //мат. ожидание
        double Mx = 0.0;

        //отсчёты
        List<Double> counts = Generate(scale);

        //центрированная корреляционная функция
        double R = R(range, counts, shift);

        //создаём массив отсортированных отсчётов
        ArrayList<Double> countsSorted = new ArrayList<>(counts);
        Collections.sort(countsSorted);

        //математическое ожидание и дисперсия выборки
        Mx = Mx(countsSorted, scale, counts);
        Dx = Dx(countsSorted, scale, counts);

        //принтуем итоги
        System.out.println("Всего элементов = " + scale);
        System.out.println("Математическое ожидание = " + Mx);
        System.out.println("Дисперсия = " + Dx);
        System.out.println("Центрированная корреляционная функция = " + R + ";");

        //вычисление автокорреляционной функции
        double normalizedR = R / Dx;
        System.out.println("Нормированная корреляционная функция(автокорреляционная ф.) = " + normalizedR + ";");

        if (-0.1 < normalizedR && normalizedR < 0.1) {
            System.out.println('\n' + "Итог: ХОРОШИЙ ДПСЧ( < |0.1| );" + '\n');
        } else {
            System.out.println('\n' + "Итог: ПЛОХОЙ ДПСЧ( > |0.1| );" + '\n');
        }

        //рисуем графики
        Graphic(counts, scale, countsSorted);

        //считаем время работы программы
        stop = System.currentTimeMillis();
        totalTime = (stop-start) / 1000.0;
        System.out.println("Всего прошло " + totalTime + " секунды.");
    }
}
