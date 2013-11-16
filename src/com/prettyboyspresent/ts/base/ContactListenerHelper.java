package com.prettyboyspresent.ts.base;

import java.util.ArrayList;

import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

public class ContactListenerHelper {
	
	private Player myP;
	private ArrayList<MoveBodyTask> myTL;
	private int REPEATING_OBJECT_PIXEL_X = 2480;
	
	ContactListenerHelper(Player p, ArrayList<MoveBodyTask> tl)
	{
		myP = p;
		myTL = tl;
	}
	
	public ContactListener getContactListener()
	{
		ContactListener contactListener = new ContactListener()
        {
        	boolean isSensor = false;
        	Body bodySensor;
            public void beginContact(Contact contact)
            {
                final Fixture x1 = contact.getFixtureA();
                final Fixture x2 = contact.getFixtureB();
                
                if (x1.getBody().getUserData() != null && x2.getBody().getUserData() != null)
                {
                    if (x1.getBody().getUserData().equals("player") || x2.getBody().getUserData().equals("player")){
                    	if (x1.getBody().getUserData().equals("floor") || x2.getBody().getUserData().equals("floor")){
                    		myP.increaseFootContacts();
                    		//REPEATING_OBJECT_PIXEL_X here is the object that repeats in 0,0 in the first and last level frame. 
                    		if (myP.getBody().getPosition().x > REPEATING_OBJECT_PIXEL_X / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT)
                    			myTL.add(new MoveBodyTask(myP, myP.getBody().getPosition().x - REPEATING_OBJECT_PIXEL_X / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT , 
                    					myP.getBody().getPosition().y, 0));
                    		if (myP.getBody().getPosition().x < 0 / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT)
                    			myTL.add(new MoveBodyTask(myP, myP.getBody().getPosition().x + REPEATING_OBJECT_PIXEL_X / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT , 
                    					myP.getBody().getPosition().y, 0));
                    	}
                    }
                }
            }

            public void endContact(Contact contact)
            {
                final Fixture x1 = contact.getFixtureA();
                final Fixture x2 = contact.getFixtureB();

                if (x1.getBody().getUserData() != null && x2.getBody().getUserData() != null)
                {
                    if (x1.getBody().getUserData().equals("player") || x2.getBody().getUserData().equals("player"))
                    	if (x1.getBody().getUserData().equals("floor") || x2.getBody().getUserData().equals("floor"))
                    	{
                    		myP.decreaseFootContacts();
                    	}
                  
                    if (x1.getBody().getUserData().equals("player") || x2.getBody().getUserData().equals("player"))
                    	if (x1.getBody().getUserData().equals("obstacle") || x2.getBody().getUserData().equals("obstacle"))
                    	{
		                    if (isSensor){
		                		bodySensor.getFixtureList().get(0).setSensor(false);
		                		isSensor = false;
		                    }
                    	}
                }
            }

            public void preSolve(Contact contact, Manifold oldManifold)
            {
            	final Fixture x1 = contact.getFixtureA();
                final Fixture x2 = contact.getFixtureB();

                if (x1.getBody().getUserData() != null && x2.getBody().getUserData() != null)
                {
                	if (x1.getBody().getUserData().equals("player") || x2.getBody().getUserData().equals("player"))
                    	if (x1.getBody().getUserData().equals("obstacle") || x2.getBody().getUserData().equals("obstacle"))
                    	{
                    		if (myP.isColor(Player.colorsEnum.BLACK))
                    		{
                    			if (x1.getBody().getUserData().equals("obstacle"))
                    			{
                    				x1.getBody().getFixtureList().get(0).setSensor(true);
                    				bodySensor = x1.getBody(); 
                    			} else {
                    				x2.getBody().getFixtureList().get(0).setSensor(true);
                    				bodySensor = x2.getBody();
                    			}
                    			isSensor = true;
                    		}
                    	}
                }
            }

            public void postSolve(Contact contact, ContactImpulse impulse)
            {
         		
            }
        };
        return contactListener;
	}
}
