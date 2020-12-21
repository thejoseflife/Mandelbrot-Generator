package math;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;
import java.util.*;

import javax.swing.JFrame;

public class Mandelbrot extends Canvas implements Runnable {

	private static final long serialVersionUID = 1L;

	private static final int WIDTH = 800, HEIGHT = 800;
	
	private final int ITERATIONS = 1000;
	private final int BOUNDS = 2;
	private final int DIVERGENT = 1000;
	
	private Thread thread;
	private boolean running = false;
	
	List<Complex> points = new ArrayList<Complex>();
	
	private void init() {
		
		for (double i = -WIDTH / 2; i < WIDTH / 2; i++) {
			for (double j = -HEIGHT / 2; j < HEIGHT / 2; j++) {
				
				points.add(mappedPoint(translatePoint(i), translatePoint(j)));
			}
		}
	}
	
	private Complex mappedPoint(double x, double y) {
		List<Complex> previousValues = new ArrayList<Complex>();
		Complex c = new Complex(untranslatePoint(x), untranslatePoint(y));
		c.setColor(Color.black);
		previousValues.add(new Complex(0, 0));
		
		for (int i = 0; i < ITERATIONS; i++) {
			Complex z = functionZ(previousValues.get(i), new Complex(x, y));
			previousValues.add(z);
			if (z.real() > DIVERGENT || z.imag() > DIVERGENT) {
				
				if (i > 17) {
					if (i > 100) {
						c.setColor(new Color(255, mapGreyScale(i, ITERATIONS), 0));
					} else {
						c.setColor(new Color(255, mapGreyScale(i, 100), 0));
					}
				} else {
					c.setColor(Color.white);
				}
				
				return c;
				
			}
			
		}

		return c;
	}
	
	private int mapGreyScale(int n, int max) {
		return (int) (255 * ((double)n / (double)max));
	}
	
	private double translatePoint(double n) {
		return (BOUNDS * n / (HEIGHT / 2));
	}
	
	private double untranslatePoint(double n) {
		return (n * (HEIGHT / 2) / BOUNDS);
	}
	
	private Complex functionZ(Complex z, Complex c) {
		return c.add((z.multiply(z)));
	}
	
	private synchronized void start() {
		thread = new Thread(this);
		thread.start();
		running = true;
	}

	private synchronized void stop() {
		try {
			thread.join();
			running = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void run() {
		init();
		while(running) {
			render();
		}
		stop();
	}
	
	private void render() {
		BufferStrategy bs = this.getBufferStrategy();
		if(bs == null) {
			this.createBufferStrategy(3);
			return;
		}
		Graphics g = (Graphics2D) bs.getDrawGraphics().create();

		g.setColor(Color.white);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		
		
		for (Complex p: points) {
			g.setColor(p.color());
			g.drawOval((int)Math.round(p.real()) + (WIDTH / 2), (int) Math.round(p.imag()) + (HEIGHT / 2), 1, 1);
		}
		
		g.dispose();
		bs.show();
	}
	
	public static void main(String args[]) {
		JFrame frame = new JFrame("Mandelbrot Generator");
		frame.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		frame.setMaximumSize(new Dimension(WIDTH, HEIGHT));
		frame.setMinimumSize(new Dimension(WIDTH, HEIGHT));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		Mandelbrot game = new Mandelbrot();
		frame.add(game);
		frame.setVisible(true);
		game.start();
		
	}
	
}