package com.gs.ts.base;

import java.util.ArrayList;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.input.touch.TouchEvent;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;
import org.andengine.util.level.simple.SimpleLevelLoader;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.gs.ts.base.SceneManager.SceneType;

public class GameScene extends BaseScene implements IOnSceneTouchListener{
	
	private PhysicsWorld physicsWorld;
	
	private HUD gameHUD;
	private Text scoreText;
	
	private Player player;
	private Text gameOverText;

	private int touchNumberLeft = 0;
	private int touchNumberRight = 0;
	
	private ArrayList<MoveBodyTask> taskList;

    private int REPEATING_OBJECT_PIXEL_X = 2480;
	
	private void createBackground(){
		setBackground(new Background(Color.BLUE));
	}
	
	private void createHUD(){
		gameHUD = new HUD();
		//initializing score text with all the chars it'll use
		scoreText = new Text(0, 0, resourcesManager.font, "Score: 0123456789", new TextOptions(HorizontalAlign.LEFT) ,vbom);
		scoreText.setAnchorCenter(0, 0);    
		scoreText.setText("Score: 0");
		//Controls
		final Rectangle left = new Rectangle(CAMERA_WIDTH/4, CAMERA_HEIGHT/2, CAMERA_WIDTH/2, CAMERA_HEIGHT, vbom)
		{
			public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y)
		    {
				if (touchEvent.isActionDown()){
					touchNumberLeft++;
					if (touchNumberLeft + touchNumberRight == 1)
					{
						player.setRunningLeft();
			        } else {
			        	player.jump();
			        }
				}
				if(touchEvent.isActionUp())
				{
					touchNumberLeft--;
					if (touchNumberLeft + touchNumberRight == 0)
						player.setRunningFalse();
					if (touchNumberLeft == 0 && player.isRunningLeft())
						player.setRunningRight();
				}
				return true;
		    };
		};
		    
		final Rectangle right = new Rectangle(CAMERA_WIDTH - CAMERA_WIDTH/4, CAMERA_HEIGHT/2, CAMERA_WIDTH/2, CAMERA_HEIGHT, vbom)
		{
			public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y)
		    {
				if (touchEvent.isActionDown()){
					touchNumberRight++;
					if (touchNumberLeft + touchNumberRight == 1)
					{
						player.setRunningRight();
			        } else {
			        	player.jump();
			        }
				}
				if(touchEvent.isActionUp())
				{
					touchNumberRight--;
					if (touchNumberLeft + touchNumberRight == 0)
						player.setRunningFalse();
					if (touchNumberRight == 0 && player.isRunningRight())
						player.setRunningLeft();
				}
				return true;
		    };
		};
		
		final Rectangle changeColor = new Rectangle(CAMERA_WIDTH/2, CAMERA_HEIGHT/6, CAMERA_WIDTH/4, CAMERA_HEIGHT/3, vbom)
		{
			public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y)
		    {
				if (touchEvent.isActionDown()){
					player.cycleColor();
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
		gameHUD.registerTouchArea(changeColor);
		gameHUD.registerTouchArea(left);
		gameHUD.registerTouchArea(right);
		gameHUD.attachChild(changeColor);
		gameHUD.attachChild(left);
		gameHUD.attachChild(right);
		gameHUD.attachChild(scoreText);
		camera.setHUD(gameHUD);
	}
	
	private void createPhysics()
	{
		//mod gravity vector accordingly to affect gravity(new Vector2(0, -17))
	    physicsWorld = new FixedStepPhysicsWorld(60, new Vector2(0, -90), false);
	    physicsWorld.setContactListener(contactListener());
	    registerUpdateHandler(physicsWorld);
	}
	
    private void loadLevel(int levelID)
    {
    	
        final SimpleLevelLoader levelLoader = new SimpleLevelLoader(vbom);
        final LevelLoaderHelper myLLH = new LevelLoaderHelper(levelLoader, this, resourcesManager, camera, 
        									physicsWorld, vbom, gameOverText);
        myLLH.doLoad();
        levelLoader.loadLevelFromAsset(activity.getAssets(), "world/" + levelID + ".lvl");
        player = myLLH.getPlayer();
    }
	
    private void createGameOverText()
    {
        gameOverText = new Text(0, 0, resourcesManager.font, "Game Over!", vbom);
    }

    private ContactListener contactListener()
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
                    		player.increaseFootContacts();
                    		//REPEATING_OBJECT_PIXEL_X here is the object that repeats in 0,0 in the first and last level frame. 
                    		if (player.getBody().getPosition().x > REPEATING_OBJECT_PIXEL_X / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT)
                    			taskList.add(new MoveBodyTask(player, player.getBody().getPosition().x - REPEATING_OBJECT_PIXEL_X / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT , 
                    					player.getBody().getPosition().y, 0));
                    		if (player.getBody().getPosition().x < 0 / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT)
                    			taskList.add(new MoveBodyTask(player, player.getBody().getPosition().x + REPEATING_OBJECT_PIXEL_X / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT , 
                    					player.getBody().getPosition().y, 0));
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
                    		player.decreaseFootContacts();
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
                    		if (player.isColor(Player.colorsEnum.BLACK))
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

    @Override
	public void createScene() {
	    createBackground();
	    createHUD();
	    createPhysics();
	    createGameOverText();
	    loadLevel(1);	    
	    setOnSceneTouchListener(this);
	    taskList = new ArrayList<MoveBodyTask>();
	    registerUpdateHandler(new IUpdateHandler()
	    {

	        @Override
	        public void onUpdate(float pSecondsElapsed) {
	            if(!taskList.isEmpty())
	            {
	                for(int i = 0; i < taskList.size(); i++)
	                {
	                    taskList.get(i).move();
	                }
	                taskList.clear();
	            }

	        }
	        @Override
	        public void reset() {
	            // TODO Auto-generated method stub

	        }
	    });
	}

	@Override
	public void onBackKeyPressed() {
		SceneManager.getInstance().loadMenuScene(engine);
	}

	@Override
	public SceneType getSceneType() {
		return SceneType.SCENE_GAME;
	}

	@Override
	public void disposeScene()
	{
	    camera.setHUD(null);
	    camera.setCenter(CAMERA_WIDTH/2, CAMERA_HEIGHT/2);

	    // removing all game scene objects.
	}

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {

		return false;
	}
	
}
