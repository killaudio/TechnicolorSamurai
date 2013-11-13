package com.gs.ts.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.IEntity;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.SAXUtils;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;
import org.andengine.util.level.EntityLoader;
import org.andengine.util.level.constants.LevelConstants;
import org.andengine.util.level.simple.SimpleLevelEntityLoaderData;
import org.andengine.util.level.simple.SimpleLevelLoader;
import org.xml.sax.Attributes;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class LevelLoaderHelper {

    
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
	private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER = "player";
	
	private boolean gameOverDisplayed = false;
	
	private SimpleLevelLoader levelLoader;
	private GameScene myGS;
	private ResourcesManager myRM;
	private PhysicsWorld myPW;
	private VertexBufferObjectManager myVBOM;
	private Player myP;
	private Text myT;
	private Camera myC;
	private ArrayList<IEntity> myEntitiesList;
	
	LevelLoaderHelper (SimpleLevelLoader ll, GameScene gs, ResourcesManager rm, Camera c, 
			PhysicsWorld pw, VertexBufferObjectManager vbm, Text t)
	{
		levelLoader = ll;
		myGS = gs;
		myRM = rm;
		myPW = pw;
		myVBOM = vbm;
		myT = t;
		myC = c;
	}
	
	public int doLoad(){

        final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(0, 0.01f, 0.5f);
    	myEntitiesList = new ArrayList<IEntity>();
    	
        levelLoader.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(LevelConstants.TAG_LEVEL)
        {
            public IEntity onLoadEntity(final String pEntityName, final IEntity pParent, final Attributes pAttributes, final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData) throws IOException 
            {
                return myGS;
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
                    levelObject = new Sprite(x, y, myRM.floor_region, myVBOM);
                    PhysicsFactory.createBoxBody(myPW, levelObject, BodyType.StaticBody, FIXTURE_DEF).setUserData("floor");
                    //levelObject.setShaderProgram(PositionColorTextureCoordinatesParabolaShaderProgram.getInstance());
                } 
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_OBS1))
                {
                    levelObject = new Sprite(x, y, myRM.obs1_region, myVBOM);
                    PhysicsFactory.createBoxBody(myPW, levelObject, BodyType.StaticBody, FIXTURE_DEF).setUserData("obstacle");
                }
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_OBS2))
                {
                	final int w = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_W);
                    final int h = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_H);
                	levelObject = new Rectangle(x,y,w,h,myVBOM);
                    levelObject.setColor(Color.CYAN);
                    PhysicsFactory.createBoxBody(myPW, levelObject, BodyType.StaticBody, FIXTURE_DEF).setUserData("floor");
                }
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER))
                {
                    myP = new Player(x, y, myVBOM, myC, myPW)
                    {
                        @Override
                        public void onDie()
                        {
                        	if (!gameOverDisplayed)
                            {
                        			myC.setChaseEntity(null);
                        	        myT.setPosition(myC.getCenterX(), myC.getCenterY());
                        	        myGS.attachChild(myT);
                        	        gameOverDisplayed = true;
                            }
                        }
                    };
                    levelObject = myP;
                }
                else
                {
                    throw new IllegalArgumentException();
                }

                levelObject.setCullingEnabled(true);
                myEntitiesList.add(levelObject);
                return levelObject;
            }
        });
		return 0;
	}
	
	public Player getPlayer()
	{
		return myP;
	}
	
    public void cleanEntities()
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
    
    protected void clearPhysicsWorld()
    {
    	Iterator<Joint> allMyJoints = myPW.getJoints();
    	while (allMyJoints.hasNext())
    	{
    		try
    		{
    			final Joint myCurrentJoint = allMyJoints.next();
    			myPW.destroyJoint(myCurrentJoint);
    		} 
    		catch (Exception localException)
    		{
    			Debug.d("SPK - THE JOINT DOES NOT WANT TO DIE: " + localException);
    		}
    	}
    	
    	Iterator<Body> localIterator = myPW.getBodies();
    	while (true)
    	{
    		if (!localIterator.hasNext())
    		{
    			myPW.clearForces();
    			myPW.clearPhysicsConnectors();
    			myPW.reset();
    			myPW.dispose();
    			myPW = null;
    			return;
    		}
    		try
    		{
    			myPW.destroyBody(localIterator.next());
    		} 
    		catch (Exception localException)
    		{
    			Debug.d("SPK - THE BODY DOES NOT WANT TO DIE: " + localException);
    		}
    	}
    }
}
