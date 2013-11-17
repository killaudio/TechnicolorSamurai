package com.prettyboyspresent.ts.base;

import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

public class Orb extends Sprite {

	private int mColor;
	
	public Orb(float pX, float pY, ITextureRegion pTiledTextureRegion,
			VertexBufferObjectManager pVertexBufferObjectManager, int color) {
		super(pX, pY, pTiledTextureRegion, pVertexBufferObjectManager);
			mColor = color;
	}

}
