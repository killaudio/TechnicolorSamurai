package com.gs.ts.base;

import java.io.IOException;
import java.util.Iterator;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.IEntity;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.util.SAXUtils;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;
import org.andengine.util.level.EntityLoader;
import org.andengine.util.level.constants.LevelConstants;
import org.andengine.util.level.simple.SimpleLevelEntityLoaderData;
import org.andengine.util.level.simple.SimpleLevelLoader;
import org.xml.sax.Attributes;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.gs.ts.base.SceneManager.SceneType;

public class GameScene extends BaseScene implements IOnSceneTouchListener{
	
	private PhysicsWorld physicsWorld;
	
	private HUD gameHUD;
	private Text scoreText;
	
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER = "player";
    
	private Player player;
	private Text gameOverText;
	private boolean gameOverDisplayed = false;

	private int touchNumberLeft = 0;
	private int touchNumberRight = 0;
    
    //---------------------------------------------
    // Level loader stuff
    //---------------------------------------------

    private static final String TAG_ENTITY = "entity";
    private static final String TAG_ENTITY_ATTRIBUTE_X = "x";
    private static final String TAG_ENTITY_ATTRIBUTE_Y = "y";
    private static final String TAG_ENTITY_ATTRIBUTE_W = "w";
    private static final String TAG_ENTITY_ATTRIBUTE_H = "h";
    private static final String TAG_ENTITY_ATTRIBUTE_TYPE = "type";
        
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_FLOOR = "floor";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_OBS1 = "obs1";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_OBS2 = "obs2";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_ZONE = "zone";
	
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
        
        final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(0, 0.01f, 0.5f);
        
        levelLoader.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(LevelConstants.TAG_LEVEL)
        {
            public IEntity onLoadEntity(final String pEntityName, final IEntity pParent, final Attributes pAttributes, final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData) throws IOException 
            {
                return GameScene.this;
            }
        });
        
        levelLoader.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(TAG_ENTITY)
        {
            public IEntity onLoadEntity(final String pEntityName, final IEntity pParent, final Attributes pAttributes, final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData) throws IOException
            {
                final int x = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_X);
                final int y = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_Y);
                final String type = SAXUtils.getAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_TYPE);
                
                final IEntity levelObject;
                
                if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_FLOOR))
                {
                    levelObject = new Sprite(x, y, resourcesManager.floor_region, vbom);
                    PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF).setUserData("floor");
                    //levelObject.setShaderProgram(PositionColorTextureCoordinatesParabolaShaderProgram.getInstance());
                } 
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_OBS1))
                {
                    levelObject = new Sprite(x, y, resourcesManager.obs1_region, vbom);
                    PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF).setUserData("obstacle");
                }
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_OBS2))
                {
                	final int w = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_W);
                    final int h = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_H);
                	levelObject = new Rectangle(x,y,w,h,vbom);
                    levelObject.setColor(Color.CYAN);
                    PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF).setUserData("floor");
                }
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_ZONE))
                {
                    FixtureDef sensorDef = PhysicsFactory.createFixtureDef(0, 0.01f, 0.5f);
                    sensorDef.isSensor = true;
                	levelObject = new Rectangle(x,y,CAMERA_WIDTH,CAMERA_HEIGHT,vbom);
                    levelObject.setColor(Color.BLACK);
                    levelObject.setAlpha(0.3f);
                    PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, sensorDef).setUserData("zone");
                }
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER))
                {
                    player = new Player(x, y, vbom, camera, physicsWorld)
                    {
                        @Override
                        public void onDie()
                        {
                        	if (!gameOverDisplayed)
                            {
                                displayGameOverText();
                            }
                        }
                    };
                    levelObject = player;
                }
                else
                {
                    throw new IllegalArgumentException();
                }

                levelObject.setCullingEnabled(true);

                return levelObject;
            }
        });

        levelLoader.loadLevelFromAsset(activity.getAssets(), "world/" + levelID + ".lvl");
    }
	
    private void createGameOverText()
    {
        gameOverText = new Text(0, 0, resourcesManager.font, "Game Over!", vbom);
    }

    private void displayGameOverText()
    {
        camera.setChaseEntity(null);
        gameOverText.setPosition(camera.getCenterX(), camera.getCenterY());
        attachChild(gameOverText);
        gameOverDisplayed = true;
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
                    	}
                    	if(x1.getBody().getUserData().equals("zone") || x2.getBody().getUserData().equals("zone")){
                    				Iterator<Body> bodies = physicsWorld.getBodies();
//                    				bodies.next();
//                    				bodies.next().setTransform(mChildren.get(counter).getX() + edgeR + CAMERA_WIDTH + mChildren.get(counter).getWidth()/2,
//                    						mChildren.get(counter).getY(), 0);
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
	    loadLevel(1);
	    createGameOverText();
	    setOnSceneTouchListener(this);
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
