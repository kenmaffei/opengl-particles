/*******************************************************************************
 * Copyright 2014 Kenneth Maffei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.kennethmaffei.particles;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.graphics.PointF;
import android.opengl.GLES11;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;

/**
 * This is our GLES11 rendering class
 * 
 * @author Kenneth Maffei
 *
 */
public class GLRenderer implements Renderer {

	boolean graphicsLoaded;
	private static GenericParticleSystem steam = new GenericParticleSystem();
	private static Fire fire = new Fire();
	private static BlackSmoke blackSmoke = new BlackSmoke();
	Quad firePit;
	long currentTime;
	long prevTime;
	float deltaT;
	
	enum PARTICLE_TYPE {STEAM, BLACK_SMOKE, FIRE};
	PARTICLE_TYPE particleType = PARTICLE_TYPE.STEAM;
	
	//Vector3s needed for billboarding (facing particles)
	Vector3 camPos = new Vector3(0.0f, 0.0f, 1500.0f);
	Vector3 objToCamProj = new Vector3();
	Vector3 lookAt = new Vector3();
	Vector3 objToCam = new Vector3();
	
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		if(height == 0)                       			//Prevent A Divide By Zero By 
		    height = 1;                       			

		GLES11.glViewport(0, 0, width, height);     	//Reset The Current Viewport 
		
		GLES11.glMatrixMode(GL10.GL_PROJECTION);
		GLES11.glLoadIdentity();
		
		GLU.gluPerspective(gl, 45.0f, (float)width/(float)height, 1.0f, 5000.0f);
		
		GLES11.glMatrixMode(GL10.GL_MODELVIEW);     	//Select The Modelview Matrix 
	}

	@Override
	/**
	 * Set up our openGL context
	 */
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES11.glEnable(GL10.GL_TEXTURE_2D);            //Enable Texture Mapping
		GLES11.glShadeModel(GL10.GL_SMOOTH);            //Enable Smooth Shading 
		GLES11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);    //Black Background 
		GLES11.glClearDepthf(1.0f);                     //Depth Buffer Setup 
		GLES11.glEnable(GL10.GL_DEPTH_TEST);            //Enables Depth Testing 
		GLES11.glDepthFunc(GL10.GL_LEQUAL);             //The Type Of Depth Testing To Do 
		GLES11.glFrontFace(GLES11.GL_CW);				//Set the face rotation 
		
		//Nice Perspective Calculations 
		GLES11.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
		
		//Create our systems
		CreateSystems(gl);
	}
	
	/**
	 * Calculate the time between each frame
	 * 
	 * @return - the time in seconds
	 */
	public float deltaTime()
	{
		currentTime = System.currentTimeMillis();
		deltaT = (float)(currentTime - prevTime)/1000.0f;
		prevTime = currentTime;
		
		return deltaT;
	}
	
	@Override
	public void onDrawFrame(GL10 gl) {

		if(!graphicsLoaded)
			return;
		
		float deltaT = deltaTime();
		
		//Clear Screen and Depth Buffer 
		GLES11.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT); 
		GLES11.glMatrixMode(GLES11.GL_MODELVIEW);
		GLES11.glLoadIdentity();
		
		GLES11.glEnableClientState(GLES11.GL_VERTEX_ARRAY); 
		GLES11.glEnableClientState(GLES11.GL_TEXTURE_COORD_ARRAY); 
		
		GLU.gluLookAt(gl, camPos.x, camPos.y, camPos.z, 0, 0, 0, 0, 1, 0.0f);
		
		//Billboarding!
		//This is how we make the particles turn towards the camera
		//To keep things efficient we only do it once per frame
		//The more correct, but expensive, way is to do it for each particle
		//Here we just do it against the particle system's origin rather than each particles origin
		objToCamProj.copy(camPos);
		objToCamProj.subtract(fire.origin);
		objToCamProj.y = 0.0f;
		objToCamProj.normalize();
		
		lookAt.x = 0;
		lookAt.y = 0;
		lookAt.z = 1;
		Globals.upAux = Vector3.crossProduct(lookAt, objToCamProj);
		Globals.upAux.normalize();
		
		Globals.theta = Vector3.dotProduct(lookAt, objToCamProj);
		Globals.thetaTest = (Globals.theta < 0.99990) && (Globals.theta > -0.9999);
		
		objToCam.copy(camPos);
		objToCam.subtract(fire.origin);
		objToCam.normalize();
		
		Globals.phi = Vector3.dotProduct(objToCamProj, objToCam);
		Globals.phiTest = (Globals.phi < 0.99990) && (Globals.phi > -0.9999);
		
		Globals.billboardDirectionTest = (objToCam.y < 0);
			    
		switch (particleType) {
			case STEAM:
				GLES11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
				steam.update(deltaT);
				steam.draw(gl);
				break;
			case BLACK_SMOKE:
				GLES11.glClearColor(0.8f, 0.8f, 0.8f, 1.0f);
				blackSmoke.update(deltaT);
				blackSmoke.draw(gl);
				firePit.draw(gl);
				break;
			case FIRE:
				GLES11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
				fire.update(deltaT);
				fire.draw(gl);
				firePit.draw(gl);
		}
		
		//Disable the client state before leaving 
		GLES11.glDisableClientState(GLES11.GL_VERTEX_ARRAY); 
		GLES11.glDisableClientState(GLES11.GL_TEXTURE_COORD_ARRAY);
	}
	
	/**
	 * Create the particle systems for the demo
	 * 
	 * @param gl - the openGl context
	 */
	void CreateSystems(GL10 gl) {
		//Steam system
		steam.loadTexture(gl, "particle_transp.jpg");
		steam.setStartColor(0.75f, 0.75f, 0.9f, 0.25f);
		steam.setMidColor(0.75f, 0.75f, 0.9f, 0.15f);
		steam.setEndColor(0.75f, 0.75f, 0.9f, 0.0f);
		steam.setEmitterVolume(10.0f, 1.0f, 10.0f);
		PointF startSize = new PointF();
		startSize.x = 100.0f;
		startSize.y = 150.0f;
		PointF endSize = new PointF();
		endSize.x = 200.0f;
		endSize.y = 800.0f;
		steam.setParticleSize(startSize, endSize);
		steam.setParticleLife(200, 55, 1.0f, 0.5f);
		Vector3 velocity = new Vector3();
		velocity.x = 0.0f;
		velocity.y = 1000.0f;
		velocity.z = 0.0f;
		Vector3 velocityVariation = new Vector3(150.0f, 30.0f, 150.0f);
		Vector3 acceleration = new Vector3(0.0f, -800.0f, 0.0f);
		steam.setMotion(velocity, velocityVariation, acceleration);
		Vector3 origin = new Vector3(0.0f, -300.0f, 0.0f);
		steam.startSystem(origin, -1.0f);
		
		//Fire system
		fire.loadTexture(gl, "particle_transp.jpg");
		fire.setEmitterVolume(240.0f, 100.0f, 240.0f);
		startSize = new PointF();
		startSize.x = 160.0f;
		startSize.y = 240.0f;
		endSize = new PointF();
		endSize.x = 120.0f;
		endSize.y = 600.0f;
		fire.setParticleSize(startSize, endSize);
		fire.setParticleLife(80, 60.0f, 2.0f, 0.5f);
		velocity = new Vector3(0.0f, 500.0f, 0.0f);
		velocityVariation = new Vector3(120.0f, 180.0f, 120.0f);
		acceleration = new Vector3();
		fire.setMotion(velocity, velocityVariation, acceleration);
		origin = new Vector3(0.0f, -300.0f, 0.0f);
		fire.startSystem(origin, -1.0f);
		
		Vector3 firePitPosition = new Vector3(0.0f, -325.0f, 200.0f);
		firePit = new Quad(500, 300, firePitPosition);
		firePit.isFacingParticle = false;
		firePit.isParticle = false;
		firePit.loadTexture(gl, "fire_pit.png");
		
		//Black smoke system
		blackSmoke.loadTexture(gl, "particle_transp.jpg");
		blackSmoke.setEmitterVolume(100.0f, 50.0f, 100.0f);
		startSize = new PointF();
		startSize.x = 100.0f;
		startSize.y = 180.0f;
		endSize = new PointF();
		endSize.x = 500.0f;
		endSize.y = 300.0f;
		blackSmoke.setParticleSize(startSize, endSize);
		blackSmoke.setParticleLife(50, 20.0f, 4.0f, 2.0f);
		velocity = new Vector3(0.0f, 250.0f, 0.0f);
		velocityVariation = new Vector3(60.0f, 100.0f, 60.0f);
		acceleration = new Vector3();
		blackSmoke.setMotion(velocity, velocityVariation, acceleration);
		origin = new Vector3(0.0f, -300.0f, 0.0f);
		blackSmoke.startSystem(origin, -1.0f);
		
		graphicsLoaded = true;
		currentTime = prevTime = System.currentTimeMillis(); //Reset timers
	}
	
	public void setToSteam() {
		particleType = PARTICLE_TYPE.STEAM;
	}
	
	public void setToFire() {
		particleType = PARTICLE_TYPE.FIRE;
	}
	
	public void setToBlackSmoke() {
		particleType = PARTICLE_TYPE.BLACK_SMOKE;
	}
}
