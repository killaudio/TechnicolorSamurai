package com.gs.ts.base;

import org.andengine.entity.scene.background.Background;
import org.andengine.entity.text.Text;
import org.andengine.util.adt.color.Color;

import com.gs.ts.base.SceneManager.SceneType;

public class LoadingScene extends BaseScene {

	private Text loading = null;
	
	@Override
	public void createScene() {
		setBackground(new Background(Color.WHITE));
		loading = new Text(0, 0, resourcesManager.font, "Loading...", vbom);
		attachChild(loading);
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
		// TODO Auto-generated method stub
		
	}

}
