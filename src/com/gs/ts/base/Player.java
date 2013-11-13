package com.gs.ts.base;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.color.Color;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public abstract class Player extends AnimatedSprite{

	// ---------------------------------------------
	// VARIABLES
	// ---------------------------------------------
	    
	private Body body;
	private boolean runRight = false;
	private boolean runLeft = false;
	private boolean stop = false;
	private int footContacts = 0;
	private boolean toggle = false;
	public enum colorsEnum {BLACK, WHITE};
	private colorsEnum myColor;
	private void createPhysics(final Camera camera, PhysicsWorld physicsWorld)
	{        
	    body = PhysicsFactory.createBoxBody(physicsWorld, this, BodyType.DynamicBody, PhysicsFactory.createFixtureDef(0, 0, 0));

	    body.setUserData("player");
	    body.setFixedRotation(true);
	    
	    physicsWorld.registerPhysicsConnector(new PhysicsConnector(this, body, true, false)
	    {
	        @Override
	        public void onUpdate(float pSecondsElapsed)
	        {
	            super.onUpdate(pSecondsElapsed);
	            camera.onUpdate(0.1f);
	            
	            if (getY() <= 0)
	            {                    
	                onDie();
	            }
	            
	            if (runRight)
	            {    
	                body.setLinearVelocity(new Vector2(12, body.getLinearVelocity().y)); 
	            }
	            if (runLeft)
	            {    
	                body.setLinearVelocity(new Vector2(-12, body.getLinearVelocity().y)); 
	            }
	            if (stop)
	            {
	            	body.setLinearVelocity(0, body.getLinearVelocity().y);
	            }
	        }
	    });
	}
	
	public Body getBody()
	{
		return this.body;
	}
	public void setRunningRight()
	{
		stop = false;
		runLeft = false;
		runRight = true;
		this.setFlippedHorizontal(false);
	    final long[] PLAYER_ANIMATE = new long[] { 100, 100, 100 };    
	    animate(PLAYER_ANIMATE, 0, 2, true);
	}

	public void setRunningLeft()
	{
		stop = false;
		runRight = false;
		runLeft = true;
		this.setFlippedHorizontal(true);
		final long[] PLAYER_ANIMATE = new long[] { 100, 100, 100 };
		animate(PLAYER_ANIMATE, 0, 2, true);
	}
	
	public void setRunningFalse() 
	{
		runLeft = false;
		runRight = false;
		stop = true;
		stopAnimation(1);
	}
	
	public boolean isRunningRight(){
		return this.runRight;
	}
	
	public boolean isRunningLeft(){
		return this.runLeft;
	}
	
	public Player(float pX, float pY, VertexBufferObjectManager vbo, Camera camera, PhysicsWorld physicsWorld) {
		super(pX, pY, (ITiledTextureRegion) ResourcesManager.getInstance().player_region, vbo);
		createPhysics(camera, physicsWorld);
	    camera.setChaseEntity(this);
	}
	
	public abstract void onDie();
	
	public void jump()
	{
	    if (footContacts < 1) 
	    {
	        return; 
	    }
	    body.setLinearVelocity(new Vector2(body.getLinearVelocity().x, 37)); 
	}
	
	public void increaseFootContacts()
	{
	    footContacts++;
	}

	public void decreaseFootContacts()
	{
	    footContacts--;
	}

	public void cycleColor() {
		if (toggle == false){
			this.setColor(Color.BLACK);
			myColor = colorsEnum.BLACK;
			toggle = !toggle;
		} else {
			this.setColor(Color.WHITE);
			myColor = colorsEnum.WHITE;
			toggle = !toggle;
		}
	}

	public boolean isColor(colorsEnum color) {
		if(myColor == color)
			return true;
		return false;
		
	}
}
