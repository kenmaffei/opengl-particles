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

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES11;
import android.opengl.GLUtils;

/**
 * This system creates a fire particle system
 * As such, the colors do not need to be set, however the option to set them exists if you want a different effect
 * It uses a start color, mid color and end color for particles
 * A particle will morph from it's start color to its mid and end colors
 * The mid color appears at the midPercent point in it's life cycle
 * The colors include an alpha value
 * 
 * @author Kenneth Maffei
 *
 */
public class Fire extends ParticleSystem {

	protected float startColorR = 1.0f;
	protected float startColorG = 1.0f;
	protected float startColorB = 1.0f;
	protected float startColorA = 1.0f;
	protected float midColorR = 1.0f;
	protected float midColorG = 1.0f;
	protected float midColorB = 1.0f;
	protected float midColorA = 1.0f;
	protected float endColorR = 1.0f;
	protected float endColorG = 1.0f;
	protected float endColorB = 1.0f;
	protected float endColorA = 1.0f;

	protected float midPercent = 0.5f;			//At what point in the particles life it reaches the mid color

	protected boolean useDefaultColors = true;	//Use the default red/yellow fire colors
	
	/**
	 * Set the particle start color
	 * Does not need to be called if using the default system
	 * 
	 * @param r - red
	 * @param g - greed
	 * @param b - blue
	 * @param a - alpha
	 */
	void setStartColor(float r, float g, float b, float a) {
		startColorR = r;
		startColorG = g;
		startColorB = b;
		startColorA = a;
		useDefaultColors = false;
	}
	
	/**
	 * Set the particle mid color
	 * Does not need to be called if using the default system
	 * 
	 * @param r - red
	 * @param g - greed
	 * @param b - blue
	 * @param a - alpha
	 */
	void setMidColor(float r, float g, float b, float a) {
		midColorR = r;
		midColorG = g;
		midColorB = b;
		midColorA = a;
		useDefaultColors = false;
	}
	
	/**
	 * Set the particle end color
	 * Does not need to be called if using the default system
	 * 
	 * @param r - red
	 * @param g - greed
	 * @param b - blue
	 * @param a - alpha
	 */
	void setEndColor(float r, float g, float b, float a) {
		endColorR = r;
		endColorG = g;
		endColorB = b;
		endColorA = a;
		useDefaultColors = false;
	}
	
	/**
	 * Sets the midpoint for particle color changes
	 * Does not need to be called if using the default system
	 * 
	 * @param midPercent - the midpoint percentage in the particle's life
	 */
	void setMidPercent(float midPercent) {
		this.midPercent = midPercent/100.0f;
		if(midPercent == 0.0f)
			midPercent = .001f;
	}
	
	@Override
	void InitializeParticle(int index) {
		Particle p = particles.get(index);
		p.position.x = origin.x + (2.0f*Globals.random() - 1.0f)*width/2.0f;
		p.position.y = origin.y + (2.0f*Globals.random() - 1.0f)*depth/2.0f;
		p.position.z = origin.z + (2.0f*Globals.random() - 1.0f)*height/2.0f;
		
		p.velocity.x = velocity.x + (2.0f*Globals.random() - 1.0f)*velocityVariation.x;
		p.velocity.y = velocity.y + (2.0f*Globals.random() - 1.0f)*velocityVariation.y;
		p.velocity.z = velocity.z + (2.0f*Globals.random() - 1.0f)*velocityVariation.z;
		
		p.acceleration.copy(acceleration);
		
		p.size.x = startSize.x;
		p.size.y = startSize.y;
		p.lifeTime = p.life = lifeTime + Globals.random()*lifeTimeVar;
		if(useDefaultColors)
		{
			p.colorR = 1.0f;
			p.colorG = 0.5f + Globals.random() * 0.5f;
			p.colorB = 0.0f;
			p.colorA = 1.0f;

			p.deltaColorR = 0.0f;
			p.deltaColorG = -(p.colorG)/p.life;
			p.deltaColorB = 0.0f;
			p.deltaColorA = -1.0f/p.life;
		}
		else
		{
			p.colorR = startColorR;
			p.colorG = startColorG;
			p.colorB = startColorB;
			p.colorA = startColorA;
		}

		p.deltaSize.x = (endSize.x - startSize.x)/p.life;
		p.deltaSize.y = (endSize.y - startSize.y)/p.life;
		
		//Now that the particle attributes are set, initialize the quad
		p.initializeQuad(startSize, glTexture[0], facing);
	}
	
	@Override
	void update(float elapsedTime) {
		if(!started)
			return;

		timeBeforeStartTime+= elapsedTime;
		if(timeBeforeStartTime < startTime)
			return;

		accumulatedTime+= elapsedTime;

		/* Frustum culling (NOT IMPLEMENTED HERE)
		* If you are doing frustum culling, then check here.
		* For example:
		* 	
		*   //If the system is outside the frustion, don't draw it.
		*   //However, depending on what the system is, you may still
		*   //want to update it. For example, transient systems like
		*   //an explosion should still be updated, while fixed system
		*   //like mist or fire would not need to be updated.
		*   if(!frustum->sphereInFrustum(origin, rEff)) {
		*   	draw = false;
		*		return; (Optional)
		*	}
		*
		*/

		//Iterate over all the particles and update their attributes
		for(int i=0; i < numParticles; )
		{
			Particle p = particles.get(i);
		    //Update the particle's position based on the elapsed time and velocity
		    p.position.x+= p.velocity.x * elapsedTime;
		    p.position.y+= p.velocity.y * elapsedTime;
		    p.position.z+= p.velocity.z * elapsedTime;
		    p.velocity.x+= p.acceleration.x * elapsedTime;
		    p.velocity.y+= p.acceleration.y * elapsedTime;
		    p.velocity.z+= p.acceleration.z * elapsedTime;
	
		    if(destroying) //accelerate the particle death a little
				p.life-= 3*elapsedTime;
			else
				p.life -= 2*elapsedTime;
	
		    p.size.x+= p.deltaSize.x * elapsedTime;
			p.size.y+= p.deltaSize.y * elapsedTime;
			
			if(useDefaultColors) {
				p.colorA+= p.deltaColorA * elapsedTime;
				p.colorG+= p.deltaColorG * elapsedTime;

				p.colorR+= p.deltaColorR * elapsedTime;
				p.colorB+= p.deltaColorB * elapsedTime;
			}
			else {
				float percentComplete = (p.lifeTime - p.life)/p.lifeTime;
				if(percentComplete < midPercent)
				{
					percentComplete = percentComplete/midPercent;
					p.colorR = startColorR + (midColorR - startColorR)*percentComplete;
					p.colorG = startColorG + (midColorG - startColorG)*percentComplete;
					p.colorB = startColorB + (midColorB - startColorB)*percentComplete;
					p.colorA = startColorA + (midColorA - startColorA)*percentComplete;
				}
				else
				{
					percentComplete = (percentComplete - midPercent)/(1.0f - midPercent);
					p.colorR = midColorR + (endColorR - midColorR)*percentComplete;
					p.colorG = midColorG + (endColorG - midColorG)*percentComplete;
					p.colorB = midColorB + (endColorB - midColorB)*percentComplete;
					p.colorA = midColorA + (endColorA - midColorA)*percentComplete;
				}
			}
				
		    //Kill the particle if it's been around long enough
		    if(p.life <= 0.0) {
		    	//Swap the last particle with the current position, and decrease the count
		    	Particle dead = particles.get(i);
		    	particles.set(i, particles.get(numParticles - 1));
		    	particles.set(numParticles - 1, dead);
		    	numParticles--;
		    }
		    else {
		    	p.updateQuad();
		    	i++;
		    }
		}

		if(accumulatedTime < duration || fixed) {
			float numParticlesThisFrame = elapsedTime*particlesPerSec;
			float numNewParticles = numParticlesThisFrame + numParticlesHeldOver;
			int numParticlesToEmit = (int) Math.floor(numNewParticles);
			numParticlesHeldOver = numNewParticles - numParticlesToEmit;
			updateSystem(numParticlesToEmit, elapsedTime);
			
			return;
		}
		else
			destroying = true;
	}

	@Override
	void draw(GL10 gl) {
		GLES11.glDepthMask(false);
		GLES11.glEnable(GLES11.GL_BLEND);
		GLES11.glTexEnvi(GLES11.GL_TEXTURE_ENV, GLES11.GL_TEXTURE_ENV_MODE, GLES11.GL_MODULATE);
		GLES11.glBlendFunc(GLES11.GL_SRC_ALPHA, GL10.GL_ONE);
		
		GLES11.glBindTexture(GLES11.GL_TEXTURE_2D, glTexture[0]);
		
		GLES11.glPushMatrix();
		//No need for translation, it's done per particle when they are created
		//We don't translate an already-emitted particle's origin as the the emitter moves
		GLES11.glRotatef(rotate.x, 0, 0, 1);
		GLES11.glRotatef(rotate.y, 0, 1, 0);
		GLES11.glRotatef(rotate.x, 1, 0, 0);
		GLES11.glScalef(scale.x, scale.y, scale.z);
		
		for(int i=0; i<numParticles; i++)
			particles.get(i).quad.draw(gl);
		
		GLES11.glPopMatrix();
		
		GLES11.glDepthMask(true);
	}

}
