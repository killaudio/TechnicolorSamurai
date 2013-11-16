package com.prettyboyspresent.ts.base;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.text.Text;
import org.andengine.util.adt.color.Color;

import com.prettyboyspresent.ts.base.SceneManager.SceneType;

public class LoadingScene extends BaseScene {

	private Text loading = null;
	private HUD loadingHUD;
	
	@Override
	public void createScene() {
		loadingHUD = new HUD();
		setBackground(new Background(Color.WHITE));
		loading = new Text(0, 0, resourcesManager.font, "Loading...", vbom);
		loading.setPosition(CAMERA_WIDTH/2, CAMERA_HEIGHT/2);
		loadingHUD.attachChild(loading);
		camera.setHUD(loadingHUD);
	}

	@Override
	public void onBackKeyPressed() {
		return;		
	}

	@Override
	public SceneType getSceneType() {
		return SceneType.SCENE_LOADING;
	}

	@Override
	public void disposeScene() {
		loading.detachSelf();
		loading.dispose();
		this.detachSelf();
		this.dispose();		
		
	}
	
	public void attachLoadingHUD()
	{
		camera.setHUD(loadingHUD);
	}

	public void detachLoadingHUD()
	{
		camera.setHUD(null);
	}
}
