package com.gs.ts.base;

import java.util.ArrayList;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.IEntity;
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
	
	private Rectangle left;
	private Rectangle right;
	private Rectangle changeColor;
	
	private int touchNumberLeft = 0;
	private int touchNumberRight = 0;
	
	private ArrayList<IEntity> myEntitiesList;
	
	ControlsHelper(HUD h, Player p, VertexBufferObjectManager v, float w, float he)
	{
		myH = h;
		myP = p;
		myVBOM = v;
		myW = w;
		myHE = he;
		myEntitiesList = new ArrayList<IEntity>();
	}
	
	public void loadControls()
	{

		//Controls
		left = new Rectangle(myW/4, myHE/2, myW/2, myHE, myVBOM)
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
		    
		right = new Rectangle(myW - myW/4, myHE/2, myW/2, myHE, myVBOM)
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
		
		changeColor = new Rectangle(myW/2, myHE/6, myW/4, myHE/3, myVBOM)
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
		left.setAlpha(0.5f);
		right.setAlpha(0.5f);
		changeColor.setAlpha(0.5f);
		myH.registerTouchArea(changeColor);
		myH.registerTouchArea(left);
		myH.registerTouchArea(right);
		myH.attachChild(changeColor);
		myH.attachChild(left);
		myH.attachChild(right);
		myEntitiesList.add(left);
		myEntitiesList.add(right);
		myEntitiesList.add(changeColor);
	}
	
	public void destroyControls()
	{
    	for (IEntity entity: myEntitiesList)
    	{
    		entity.clearEntityModifiers();
    		entity.clearUpdateHandlers();
    		entity.detachSelf();
    		
    		if (!entity.isDisposed())
    		{
    			entity.dispose();
    		}
    	}
    	
    	myEntitiesList.clear();
    	myEntitiesList = null;
	}
}
