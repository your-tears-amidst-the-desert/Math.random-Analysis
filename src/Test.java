//Как итог работы программы получаем весьма различные результаты для оценки качества работы Math.random() - 
//значения автокорреляционной функции может быть как < |0.1|, так и значительно превосходить его. Зависит от конкретной итерации, внутреннего состояния системы
//условие для проверки качества ДПСЧ было взято с сайта - https://intuit.ru/studies/courses/623/479/lecture/21088?page=3

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

public class TEST {
    
    static String funcName = "График функции";
    static String frameName = "Тестовое задание";
    static String graphicName = "y = F(x)";
    static XYSeries series1 = new XYSeries(funcName);
    static XYSeries series2 = new XYSeries(funcName);
    static String densityName = "y = F'(x) = f(x)";

    //округление до нужного знака
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static void main(String[] args) {
        
        final int scale = 1000;
        final int shift = 7;
        final int range = scale - shift;
        double Dx = 0.0;
        double Mx = 0.0;
        int sumElements = 0;

        //генерация последовательности
        ArrayList<Double> counts = new ArrayList<>();
        double x;
        double y;
        for(int i = 0; i < scale; i++) {
            x = Math.random();
            //y = Math.pow(1-x, Math.sin(Math.E - x)) * i / 1;
            counts.add(x);
        }

        //вычисляем центрированную корреляционную функцию
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

        ArrayList<Double> countsSorted = new ArrayList<>(counts);
        Collections.sort(countsSorted);

        //вычисляем математическое ожидание и дисперсию случайной величины
        ArrayList<Integer> density = new ArrayList<>();
        int count = 0;
        double chance = 0.0;
        for(double i = 0; i < (double)countsSorted.size() / scale; i += 0.001) {
            if(i == round(countsSorted.get((int)i * scale), 3)) {
                count++;
            }
            chance = (double)count / scale;
            try {
                double mx;
                double dx = 0;
                mx = i * chance;
                if (mx != 0) {
                    dx = Math.pow(countsSorted.get((int)(i * scale)) - 0.5, 2) * chance;
                }
                Mx += mx;
                Dx += dx;
            } catch (ArithmeticException e) {
                System.out.println("Ошибка!");
            }
            sumElements += count;
            density.add(count);
        }

        //принтуем итоги 
        System.out.println("Всего элементов = " + sumElements);
        System.out.println("Математическое ожидание = " + Mx);
        System.out.println("Дисперсия = " + Dx);
        System.out.println("Центрированная корреляционная функция = " + R + ";");

        double normalizedR = R / Dx / Dx;
        System.out.println("Нормированная корреляционная функция(автокорреляционная ф.) = " + normalizedR + ";");

        if (-0.1 < normalizedR && normalizedR < 0.1) {
            System.out.println('\n' + "Итог: ХОРОШИЙ ДПСЧ( < |0.1| );" + '\n');
        } else {
            System.out.println('\n' + "Итог: ПЛОХОЙ ДПСЧ( > |0.1| );" + '\n');
        }

        //добавляем данные в датасет для визуализации на графике
        System.out.println(density);
        System.out.println(countsSorted);
        for (double i = 0; i < 1; i += 0.001) {
            series1.add(i, counts.get((int)(i*scale)));
            //series2.add(i, density.get((int)(i * scale)));
            series2.add(i, countsSorted.get((int)(i * scale)));
        }

        //создание фрэйма и помещение в него графика
        XYDataset xyDataset1 = new XYSeriesCollection(series1);
        XYDataset xyDataset2 = new XYSeriesCollection(series2);
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
