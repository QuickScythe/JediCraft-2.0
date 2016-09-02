package com.mineswine.jedicraft.vehicle;

public interface WASDEntity {
	//All methods should be self explanitory. 
	public void forward();
	public void backward();
	public void left();
	public void right();
	public void jump();
	public void shift();
	public void reset(); // Resets all values
}
