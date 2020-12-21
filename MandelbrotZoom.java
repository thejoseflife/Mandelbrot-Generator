package mandelbrot;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;
import java.util.*;

import javax.swing.JFrame;

public class MandelbrotZoom extends Canvas implements Runnable {

	private static final long serialVersionUID = 1L;

	// Width and height of window
	private static final int WIDTH = 800, HEIGHT = 800;
	
	// Do not touch
	private Thread thread;
	private boolean running = false;
	private double LEFT_BOUND = 0;
	private double TOP_BOUND = 0;
	private double BOTTOM_BOUND = 0;
	private double RIGHT_BOUND = 0;
	private int OFFSET_REAL = WIDTH / 2;
	private int OFFSET_IMAG = HEIGHT / 2;
	
	// You can add more colors, just make sure to add the colors in init() too
	private int numberOfColors = 16;
	private Color mapping[] = new Color[numberOfColors];
	
	private final int ITERATIONS = 10000;
	private final int DIVERGENT = 10000;

	private List<Complex> points = new ArrayList<Complex>();
	
	// The point that you want at the center of graph to zoom in on
	// Cool point to try:  -1.24254013716898265806, 0.413238151606368892027
	private double focusX = -1.24254013716898265806;
	private double focusY = 0.413238151606368892027;
	
	// Scale * 2 is the width and height of the interval the graph is rendered on
	// Smallest scale is somewhere around 13 zeroes because then the decimals get too large
	// Scale of 2 means graph is on interval [focusX - 2, focusX + 2] and [focusY - 2, focusY + 2]
	private double SCALE = 2;
	
	private void init() {
		
		LEFT_BOUND = focusX - SCALE;
		RIGHT_BOUND = focusX + SCALE;
		TOP_BOUND = -focusY + SCALE;
		BOTTOM_BOUND = -focusY - SCALE;
		
	    mapping[0] = new Color(66, 30, 15);
	    mapping[1] = new Color(25, 7, 26);
	    mapping[2] = new Color(9, 1, 47);
	    mapping[3] = new Color(4, 4, 73);
	    mapping[4] = new Color(0, 7, 100);
	    mapping[5] = new Color(12, 44, 138);
	    mapping[6] = new Color(24, 82, 177);
	    mapping[7] = new Color(57, 125, 209);
	    mapping[8] = new Color(134, 181, 229);
	    mapping[9] = new Color(211, 236, 248);
	    mapping[10] = new Color(241, 233, 191);
	    mapping[11] = new Color(248, 201, 95);
	    mapping[12] = new Color(255, 170, 0);
	    mapping[13] = new Color(204, 128, 0);
	    mapping[14] = new Color(153, 87, 0);
	    mapping[15] = new Color(106, 52, 3);
		
		for (double i = -WIDTH / 2; i < WIDTH / 2; i++) {
			for (double j = -HEIGHT / 2; j < HEIGHT / 2; j++) {
				points.add(mappedPoint(translatePoint(LEFT_BOUND, RIGHT_BOUND, i), translatePoint(BOTTOM_BOUND, TOP_BOUND, j)));
			}
		}
		
	}
	
	// Find out if point c diverges or converges when plugged into z^2 + c
	private Complex mappedPoint(double x, double y) {
		List<Complex> previousValues = new ArrayList<Complex>();
		Complex c = new Complex(untranslatePoint(LEFT_BOUND, RIGHT_BOUND, x), untranslatePoint(BOTTOM_BOUND, TOP_BOUND, y));
		c.setColor(Color.black);
		previousValues.add(new Complex(0, 0));
		
		for (int i = 0; i < ITERATIONS; i++) {
			Complex z = functionZ(previousValues.get(i), new Complex(x, y));
			previousValues.add(z);
			if (z.real() > DIVERGENT || z.imag() > DIVERGENT) {
				
				if (i < ITERATIONS && i > 0) {
				    int n = i % numberOfColors;
				    c.setColor(mapping[n]);
				} else {
					c.setColor(Color.white);
				}
				
				return c;
				
			}
			
		}

		return c;
	}

	// Scale point from screen size to interval specified
	private double translatePoint(double firstBound, double secondBound, double n) {
		double axis = (firstBound + secondBound) / 2;
		
		double ratio = n / (WIDTH / 2);
		
		double ratioDistance = secondBound - axis;
		
		double updatedPoint = ratio * ratioDistance;
		
		return axis + updatedPoint;

	}

	// Scale point from interval specified to screen size
	private double untranslatePoint(double firstBound, double secondBound, double n) {
		
		double axis = (firstBound + secondBound) / 2;
		
		double bound = secondBound - firstBound;
		
		double pointDistance = n - axis;
		
		double ratio = pointDistance / (bound / 2);
		
		double updatedPoint = (WIDTH / 2) * ratio;
		
		return updatedPoint;

	}
	
	// The mandelbrot function, f(z) = z^2 + c
	private Complex functionZ(Complex z, Complex c) {
		return c.add((z.multiply(z)));
	}
	
	// Do not touch below here unless you can make an efficient rendering system
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
	
	// Game loop because I used a game template for the rendering
	public void run() {
		init();
		int count = 0;
		while(running) {
			if (count % 1000000 == 0){
				render();
			}
		}
		stop();
	}
	
	private void render() {
		BufferStrategy bs = this.getBufferStrategy();
		if(bs == null) {
			this.createBufferStrategy(2);
			return;
		}
		Graphics g = (Graphics2D) bs.getDrawGraphics().create();

		g.setColor(Color.white);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		
		g.translate(OFFSET_REAL, OFFSET_IMAG);
		
		for (Complex p: points) {
			g.setColor(p.color());
			g.drawOval((int)Math.round(p.real()), (int) Math.round(p.imag()), 1, 1);
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
		MandelbrotZoom mandelbrot = new MandelbrotZoom();
		frame.add(mandelbrot);
		frame.setVisible(true);
		mandelbrot.start();
		
	}
	
}
