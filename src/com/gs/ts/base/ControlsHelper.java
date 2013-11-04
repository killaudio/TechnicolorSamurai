package com.gs.ts.base;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.color.Color;

public class ControlsHelper {

	private HUD myH;
	private Player myP;
	private VertexBufferObjectManager myVBOM;
	private float myW;
	private float myHE;
	
	private int touchNumberLeft = 0;
	private int touchNumberRight = 0;
	
	ControlsHelper(HUD h, Player p, VertexBufferObjectManager v, float w, float he)
	{
		myH = h;
		myP = p;
		myVBOM = v;
		myW = w;
		myHE = he;
	}
	
	public void loadControls()
	{
		//Controls
		final Rectangle left = new Rectangle(myW/4, myHE/2, myW/2, myHE, myVBOM)
		{
			public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y)
		    {
				if (touchEvent.isActionDown()){
					touchNumberLeft++;
					if (touchNumberLeft + touchNumberRight == 1)
					{
						myP.setRunningLeft();
			        } else {
			        	myP.jump();
			        }
				}
				if(touchEvent.isActionUp())
				{
					touchNumberLeft--;
					if (touchNumberLeft + touchNumberRight == 0)
						myP.setRunningFalse();
					if (touchNumberLeft == 0 && myP.isRunningLeft())
						myP.setRunningRight();
				}
				return true;
		    };
		};
		    
		final Rectangle right = new Rectangle(myW - myW/4, myHE/2, myW/2, myHE, myVBOM)
		{
			public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y)
		    {
				if (touchEvent.isActionDown()){
					touchNumberRight++;
					if (touchNumberLeft + touchNumberRight == 1)
					{
						myP.setRunningRight();
			        } else {
			        	myP.jump();
			        }
				}
				if(touchEvent.isActionUp())
				{
					touchNumberRight--;
					if (touchNumberLeft + touchNumberRight == 0)
						myP.setRunningFalse();
					if (touchNumberRight == 0 && myP.isRunningRight())
						myP.setRunningLeft();
				}
				return true;
		    };
		};
		
		final Rectangle changeColor = new Rectangle(myW/2, myHE/6, myW/4, myHE/3, myVBOM)
		{
			public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y)
		    {
				if (touchEvent.isActionDown()){
					myP.cycleColor();
				}
				return true;
		    };
		};
		
		left.setColor(Color.RED);
		right.setColor(Color.CYAN);
		changeColor.setColor(Color.PINK);
		left.setAlpha(0.0f);
		right.setAlpha(0.0f);
		changeColor.setAlpha(0.0f);
		myH.registerTouchArea(changeColor);
		myH.registerTouchArea(left);
		myH.registerTouchArea(right);
		myH.attachChild(changeColor);
		myH.attachChild(left);
		myH.attachChild(right);
	}
	
}
