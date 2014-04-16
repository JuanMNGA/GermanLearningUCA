package es.uca.tabu.utils;


public class Environment {

	private static Environment instance = null;
	
	int width, height;
	
	public static Environment getInstance() {
		if(instance == null)
			instance =  new Environment();
		return instance;
	}
	
	public void setScreenWidth(int width) {
		this.width = width;
	}
	
	public void setScreenHeight(int height) {
		this.height = height;
	}
	
	public int getScreenWidth() {
		return width;
	}
	
	public int getScreenHeight() {
		return height;
	}
}
