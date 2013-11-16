package com.prettyboyspresent.ts.base;

import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.shader.ShaderProgram;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

public class Orb extends Sprite {

	private int mColor;
	
	public Orb(float pX, float pY, ITextureRegion pTiledTextureRegion,
			VertexBufferObjectManager pVertexBufferObjectManager,
			ShaderProgram pShaderProgram, int color) {
		super(pX, pY, pTiledTextureRegion, pVertexBufferObjectManager, pShaderProgram);
			mColor = color;
	}

}
