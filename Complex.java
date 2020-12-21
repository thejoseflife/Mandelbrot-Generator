package math;

import java.awt.Color;

public class Complex extends Object {

	private double x, y;

	private Color c;
	
	public Complex(double u, double v) {
		x = u;
		y = v;
	}

	public double real() {
		return x;
	}

	public double imag() {
		return y;
	}
	
	public Color color() {
		return c;
	}
	
	public void setColor(Color c) {
		this.c = c;
	}

	public Complex add(Complex w) {
		return new Complex(x + w.real(), y + w.imag());
	}

	public Complex multiply(Complex w) {
		return new Complex(x * w.real() - y * w.imag(), x * w.imag() + y * w.real());
	}

}
