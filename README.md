TechnicolorSamurai
==================

TODO

   [] add lighting - http://www.lighthouse3d.com/tutorials/glsl-core-tutorial/directional-lights/ 
      - create "orb" class that emits light with descending alpha for up to a defined radius (variable?)
      - render this class after onDraw(super.draw()) so it's on top of everything
   [] integrate chi graphics and some animations
   [] first sample level
   

	@Override
    public Engine onCreateEngine(final EngineOptions pEngineOptions) {
        return new LimitedFPSEngine(pEngineOptions, 60) {
        	
        	@Override
        	public void onDrawFrame(GLState pGLState)
        			throws InterruptedException {
				
				if (!mRenderTexturesInitialized) {
					initRenderTexture(pGLState);
					mRenderTexturesInitialized = true;
				}

				super.onDrawFrame(pGLState);
				
				mRenderTexture2.begin(pGLState, false, true, Color.TRANSPARENT);
				{
					mRenderTextureSprite1.onDraw(pGLState, mCamera);
				}
				mRenderTexture2.end(pGLState);
				
				pGLState.pushProjectionGLMatrix();
				pGLState.orthoProjectionGLMatrixf(0, mCamera.getSurfaceWidth(), 0, mCamera.getSurfaceHeight(), -1, 1);
				{
					mRenderTextureSprite2.onDraw(pGLState, mCamera);
				}
				pGLState.popProjectionGLMatrix();
        	}
        	
			private void initRenderTexture(GLState pGLState) {
				mRenderTexture1 = new RenderTexture(mCamera.getSurfaceWidth(), mCamera.getSurfaceHeight(), PixelFormat.RGBA_4444);
				mRenderTexture1.init(pGLState);
				mRenderTextureSprite1 = new UncoloredSprite(0f, 0f, TextureRegionFactory.extractFromTexture(mRenderTexture1), getVertexBufferObjectManager());
				mRenderTextureSprite1.setShaderProgram(GaussianBlurPass1ShaderProgram.getInstance());

				mRenderTexture2 = new RenderTexture(mCamera.getSurfaceWidth(), mCamera.getSurfaceHeight(), PixelFormat.RGBA_4444);
				mRenderTexture2.init(pGLState);
				mRenderTextureSprite2 = new UncoloredSprite(0f, 0f, TextureRegionFactory.extractFromTexture(mRenderTexture2), getVertexBufferObjectManager());
				mRenderTextureSprite2.setShaderProgram(GaussianBlurPass2ShaderProgram.getInstance());
			}
        };
	}
