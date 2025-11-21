package fr.tp.inf112.projects.robotsim.model.shapes;

import com.fasterxml.jackson.annotation.JsonProperty;

import fr.tp.inf112.projects.canvas.model.RectangleShape;

public class RectangularShape extends PositionedShape implements RectangleShape {
	
	private static final long serialVersionUID = -6113167952556242089L;

	@JsonProperty("width")
	private int width;

	@JsonProperty("height")
	private int height;

	public RectangularShape(){
		this(0,0,0,0);
	}

	public RectangularShape(final int xCoordinate,
							final int yCoordinate,
							final int width,
							final int height) {
		super(xCoordinate, yCoordinate);
	
		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

//	@JsonProperty("height")
	public int getHeight() {
		return height;
	}
	
	

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public String toString() {
		return super.toString() + " [width=" + width + ", heigth=" + height + "]";
	}
}
