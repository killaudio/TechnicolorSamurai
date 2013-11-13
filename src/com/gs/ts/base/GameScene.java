package com.gs.ts.base;

import java.util.ArrayList;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;
import org.andengine.util.level.simple.SimpleLevelLoader;
import com.badlogic.gdx.math.Vector2;
import com.gs.ts.base.SceneManager.SceneType;

public class GameScene extends BaseScene {
	
	private PhysicsWorld physicsWorld;
	private Player player;
	private ArrayList<MoveBodyTask> taskList;
	private LevelLoaderHelper myLLH;
	private ControlsHelper myCH;
	
	private void createHUD(){
		HUD gameHUD = new HUD();
		myCH = new ControlsHelper(gameHUD, player, vbom, CAMERA_WIDTH, CAMERA_HEIGHT);
		myCH.loadControls();
		Text scoreText = new Text(0, 0, resourcesManager.font, "Score: 0123456789", new TextOptions(HorizontalAlign.LEFT) ,vbom);
		scoreText.setAnchorCenter(0, 0);    
		scoreText.setText("Score: 0");
		gameHUD.attachChild(scoreText);
		camera.setHUD(gameHUD);
	}
	
	private void createPhysics()
	{
		//mod gravity vector accordingly to affect gravity(new Vector2(0, -17))
	    physicsWorld = new FixedStepPhysicsWorld(60, new Vector2(0, -90), false);
	    loadLevel(1);
	    ContactListenerHelper myCLH = new ContactListenerHelper(player, taskList);
	    physicsWorld.setContactListener(myCLH.getContactListener());
	    registerUpdateHandler(physicsWorld);
	}
	
    private void loadLevel(int levelID)
    {
    	Text gameOverText = new Text(0, 0, resourcesManager.font, "Game Over!", vbom);    	
        final SimpleLevelLoader levelLoader = new SimpleLevelLoader(vbom);
        myLLH = new LevelLoaderHelper(levelLoader, this, resourcesManager, camera, 
        									physicsWorld, vbom, gameOverText);
        myLLH.doLoad();
        levelLoader.loadLevelFromAsset(activity.getAssets(), "world/" + levelID + ".lvl");
        //player is parsed from .lvl file, so we need to get the instantiation
        player = myLLH.getPlayer();
    }

    @Override
	public void createScene() {

    	taskList = new ArrayList<MoveBodyTask>();
    	setBackground(new Background(Color.BLACK));
	    createPhysics();
	    createHUD();    
	    
	    registerUpdateHandler(new IUpdateHandler()
	    {
	        @Override
	        public void onUpdate(float pSecondsElapsed) {
	            if(!taskList.isEmpty()){
	                for(int i = 0; i < taskList.size(); i++){
	                    taskList.get(i).move();
	                }
	                taskList.clear();
	            }
	        }
	        @Override
	        public void reset() {}
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

	public void clearScene()
	{
		engine.runOnUpdateThread(new Runnable()
		{
			@Override
			public void run()
			{
				myLLH.clearPhysicsWorld();
				myLLH.cleanEntities();
				myCH.destroyControls();
				clearTouchAreas();
				clearUpdateHandlers();
				myLLH = null;
				myCH = null;
				System.gc();
			}
		});
	}
	
	@Override
	public void disposeScene()
	{
	    camera.setHUD(null);
	    // removing all game scene objects.
	    clearScene();	    
	}

}
