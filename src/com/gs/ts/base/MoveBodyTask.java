package com.gs.ts.base;

public class MoveBodyTask {

	Player pl;
	float x;
	float y;
	float angle;
	
	MoveBodyTask(Player b, float x1, float y1, float ang)
	{
	    pl = b;
	    x = x1;
	    y = y1;
	    angle = ang;
	}
	
	public void move()
	{
		pl.getBody().setTransform(x, y, angle);	
	}
}