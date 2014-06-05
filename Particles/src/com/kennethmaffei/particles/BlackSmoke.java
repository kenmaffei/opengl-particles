package com.kennethmaffei.particles;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES11;

public class BlackSmoke extends ParticleSystem{

	@Override
	void InitializeParticle(int index) {
		Particle p = particles.get(index);
		p.colorR = p.colorG = p.colorB = p.colorA = 1.0f;
		
		p.lifeTime = p.life = lifeTime + Globals.random()*lifeTimeVar;
		p.deltaColorR = -(p.colorR)/p.life;
		p.deltaColorG = -(p.colorG)/p.life;
		p.deltaColorB = -(p.colorB)/p.life;
		p.deltaColorA = -1.0f/p.life;

		p.position.x = origin.x + (2.0f*Globals.random() - 1.0f)*width/2.0f;
		p.position.y = origin.y + (2.0f*Globals.random() - 1.0f)*depth/2.0f;
		p.position.z = origin.z + (2.0f*Globals.random() - 1.0f)*height/2.0f;
		
		p.velocity.x = velocity.x + (2.0f*Globals.random() - 1.0f)*velocityVariation.x;
		p.velocity.y = velocity.y + (2.0f*Globals.random() - 1.0f)*velocityVariation.y;
		p.velocity.z = velocity.z + (2.0f*Globals.random() - 1.0f)*velocityVariation.z;
		
		p.acceleration.copy(acceleration);
		
		p.size.x = startSize.x;
		p.size.y = startSize.y;

		p.deltaSize.x = (endSize.x - startSize.x)/p.life;
		p.deltaSize.y = (endSize.y - startSize.y)/p.life;
		
		//Now that the particle attributes are set, initialize the quad
		p.initializeQuad(startSize, glTexture[0], facing);
		
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
	
			p.life -= 2*elapsedTime;
	
		    p.size.x+= p.deltaSize.x * 2*elapsedTime;
			p.size.y+= p.deltaSize.y * 2*elapsedTime;
			
			p.colorA+= p.deltaColorA * 2*elapsedTime;
		    p.colorG+= p.deltaColorG * 2*elapsedTime;

			p.colorR+= p.deltaColorR * 2*elapsedTime;
			p.colorB+= p.deltaColorB * 2*elapsedTime;
				
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

		if(accumulatedTime < duration || fixed)
		{
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
    	GLES11.glBlendFunc(GLES11.GL_ZERO, GLES11.GL_ONE_MINUS_SRC_COLOR); //Special blend mode for Black Smoke!
 	    
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
