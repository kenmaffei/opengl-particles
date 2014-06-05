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
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.opengl.GLES11;
import android.opengl.GLUtils;

/**
 * Abstract class for a particle system. Defines system and individual particle properties.
 * Also handles creation and destruction of particles
 * 
 * @author Kenneth Maffei
 *
 */
public abstract class ParticleSystem {
	ArrayList<Particle> particles = new ArrayList<Particle>();
	//We don't rely on particles.size() for the number of particles in the system.
	//For efficiency, the particles array is always built for maxParticles.
	//Particles are swapped in the array at particle death (see the emit() function).
	//numParticles keeps track of how many we are rendering at a given time.
	protected int numParticles; 
	int maxParticles;
	
	protected PointF startSize = new PointF();              //Particle start size
	protected PointF endSize = new PointF();                //Particle end size
	protected float particlesPerSec;                        //Particle birth rate
	protected Vector3 velocity = new Vector3();             //Particle starting velocity			
	protected Vector3 velocityVariation = new Vector3();    //Variation in the particle starting velocity
	protected Vector3 acceleration = new Vector3();         //Particle staring acceleration
	protected Vector3 origin = new Vector3();               //The origin for the system
	protected float height, width, depth;                   //Volume parameters for particle generation
	protected float radius;                                 //For radial systems
	protected float accumulatedTime;                        //For transient systems, the time the system has been alive
	protected float lifeTime;                               //Particle lifetime
	protected float lifeTimeVar;                            //Lifetime variation
	protected float startTime;                              //Delay before actually generating any particles
	protected float timeBeforeStartTime;                    //The accumulated time before the start time
	protected boolean started;                              //Indicates that the system has been started
	protected boolean fixed;                                //Fixed system or transient
	protected float duration = -1.0f;                       //Duration of particle system; duration = -1 means keep going once started
	protected boolean destroying;                           //If the system is shutting down. Generally will decrease life for graceful shut down
	protected boolean draw;                                 //Whether or not to actually render the system (for transient systems, update can be true, but draw false)
	//If elapsedTime is such that numNewParticles is 0 (because of rounding down),
	//then hold over this elapsed time and add it to the next until we get some particle production!
	protected float timeHeldOver;
	protected float numParticlesHeldOver;
	
	protected Vector3 emitterVelocity = new Vector3();      //Allows the particle emitter to move
	protected Vector3 emitterAcceleration = new Vector3();  //Acceleration for the emitter

	protected boolean radial;                               //Sets this as for radial particle production. Velocity is interpreted as radial velocity.

	protected boolean facing = true;                        //Particles always face the camera. This is usually the case, but not always.
	
	protected float rEff;                                   //Effective radius used for frustum culling

	protected Vector3 scale = new Vector3(1.0f, 1.0f, 1.0f);//Scaling of the entire system as a whole
	protected Vector3 rotate = new Vector3();               //Rotation of the entire system as a whole
	
	protected int[] glTexture = new int[1];
	
	/**
	 * Initializes a given particle with all its "start" values
	 * 
	 * @param index - the index into the particle array
	 */
	abstract void InitializeParticle(int index);
	
	/**
	 * Iterates over the particles and updates their attributes
	 * This function must also call updateSystem()
	 * 
	 * @param elapsedTime - the time since the last frame
	 */
	abstract void update(float elapsedTime);
	
	/**
	 * Renders the particle system to the screen
	 * 
	 * @param gl - the openGL context
	 */
	abstract void draw(GL10 gl);
	
	/**
     * Load a masking texture for the particle system
     * 
     * @param gl - the openGL context
     * @param file - the masking texture file name
     * @return - success or failure
     */
	boolean loadTexture(GL10 gl, String file) {
		try{
			InputStream is = Globals.context.getAssets().open(file); 
			int size = is.available(); 
			byte[] buffer = new byte[size]; 
			is.read(buffer, 0, size);
			is.close(); 
			
			//Check if png or jpg
			BitmapFactory.Options opt = new BitmapFactory.Options(); 
			opt.inDither = false;
			opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
			
			Bitmap bitmap = BitmapFactory.decodeByteArray(buffer, 0, size, opt);
			
			//Generate one texture pointer 
			GLES11.glGenTextures(1, glTexture, 0); 
			// ...and bind it to our array 
			GLES11.glBindTexture(GL10.GL_TEXTURE_2D, glTexture[0]); 
			
			//Create nearest filtered texture 
			GLES11.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR); 
			GLES11.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR); 
			GLES11.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE); 
			GLES11.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
			
			//Use Android GLUtils to specify a two-dimensional texture image from our bitmap 
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0); 
		}
		catch(IOException IOerror) {
			return false;
		}
		return true;
    }
	
	/**
	 * Resets the particle array
	 */
	public void initializeSystem() {
		particles.clear();
		numParticles = 0;
		for(int i=0; i<maxParticles; i++) {
			Particle p = new Particle();
			particles.add(p);
		}
	}
	
	/**
	 * Sets this to a radially emitting systems
	 */
	public void setRadial() {
		radial = true;
	}

	/**
	 * Particles will not rotate to face the camera
	 */
	public void facingOff() {
		facing = false;
	}

	/**
	 * Sets the volume in which particles are produced
	 * 
	 * @param width - production extent along x
	 * @param depth - production extent along y
	 * @param height - production extent along z
	 */
	public void setEmitterVolume(float width, float depth, float height) {
		this.width = width;
		this.depth = depth;
		this.height = height;
	}

	/**
	 * Sets the motion of the emitter volume
	 * 
	 * @param velocity - starting velocity
	 * @param acceleration - acceleration
	 */
	public void setEmitterMotion(Vector3 velocity, Vector3 acceleration) {
		emitterVelocity.copy(velocity);
		emitterAcceleration.copy(acceleration);
	}

	/**
	 * Sets the initial origin of the system
	 * 
	 * @param position - x, y and z coordinates of the origin
	 */
	public void setEmitterPosition(Vector3 position) {
		origin.copy(position);
	}

	/**
	 * Sets the scale factor for the system as a whole.
	 * Can be used to dynamically scale the system
	 * 
	 * @param scale - the scale factor
	 */
	public void setScale(Vector3 scale) {
		this.scale = scale;
	}

	/**
	 * For rotation of the system
	 * 
	 * @param axis - which axis to rotate
	 * @param angle - the amount to increase rotation by
	 */
	public void setRotate(int axis, float deltaAngle) {
		switch (axis)
		{
			case 0:
				rotate.x+= deltaAngle;
				break;
			case 1:
				rotate.y+= deltaAngle;
				break;
			case 2:
				rotate.z+= deltaAngle;
				break;
			default:
				break;
		}
	}
	
	/**
	 * Sets the particle attributes at creation and destruction
	 * 
	 * @param startSize - x and y start sizes
	 * @param endSize - x and y end sizes
	 */
	public void setParticleSize(PointF startSize, PointF endSize) {
		this.startSize = startSize;
		this.endSize = endSize;
	}

	/**
	 * Sets the attributes for particle creation and life
	 * 
	 * @param maxParticles - maximum allowed number of particles at any given time
	 * @param particlesPerSec - particle birth rate
	 * @param lifeTime - how long a particle lives before it is removed from the system
	 * @param lifeTimeVar - for a given particle, a variation parameter for the lifetime for randomness
	 */
	public void setParticleLife(int maxParticles, float particlesPerSec, float lifeTime, float lifeTimeVar) {
		this.maxParticles = maxParticles;
		this.particlesPerSec = particlesPerSec;
		this.lifeTime = lifeTime;
		this.lifeTimeVar = lifeTimeVar;
	}

	/**
	 * Sets the motion attributes for particles
	 * 
	 * @param velocity - the start velocity for a particle
	 * @param velocityVariation - a variation in the start velocity for randomness
	 * @param acceleration - particle acceleration
	 */
	public void setMotion(Vector3 velocity, Vector3 velocityVariation, Vector3 acceleration) {
		this.velocity.copy(velocity);
		this.velocityVariation.copy(velocityVariation);
		this.acceleration.copy(acceleration);
	}

	
	/**
	 * Starts a system
	 * 
	 * @param origin - the x, y and z coordinates of the system position
	 * @param duration - how long the system lives. A value of -1.0f means the system does not stop
	 */
	public void startSystem(Vector3 origin, float duration) {
		this.origin.copy(origin);

		this.duration = duration;
		initializeSystem();

		float test;
		float sizex = startSize.x > endSize.x? startSize.x:endSize.x;
		float sizey = startSize.y > endSize.y? startSize.y:endSize.y;

		//Calculate an effective radius for frustum culling
		if(radius > 0.0f)
		{
			rEff = sizex + radius + velocity.x*(lifeTime + lifeTimeVar);
			test = sizey + height/2.0f + velocity.z*(lifeTime + lifeTimeVar);
			if(test > rEff)
				test = rEff;
		}
		else
		{
			float size = (sizex > sizey)? sizex:sizey;
			float dim = (width > depth)? width:depth;
			dim = (dim > height)? dim:height;
			//By using the factor of 2.0, it helps to start the system before the camera fully gets around to it.
			rEff = 2.0f*(size + dim + velocity.length()*(lifeTime + lifeTimeVar));
		}
		
		started = draw = true;
		if(duration == -1.0f)
			fixed = true;
	}
	
	/**
	 * Moves the system if necessary.
	 * Adjusts our particle array when emitting particles
	 * 
	 * @param numParticlesToCreate - how many particles need to be created this frame
	 * @param deltaTime - time increment since the last frame
	 * @return
	 */
	protected void updateSystem(int numParticlesToCreate, float deltaTime) {
		//Create new particles
		origin.add(emitterVelocity.scaled(deltaTime));

		emitterVelocity.add(emitterAcceleration.scaled(deltaTime));
		while(numParticlesToCreate > 0 && numParticles < maxParticles) {
			InitializeParticle(numParticles++);
			--numParticlesToCreate;
		}
	}

	/**
	 * Causes an immediate killing and reset of the system
	 */
	public void killSystem() {
		if(numParticles != 0) {
			particles.clear();
			numParticles = 0;
		}
	}
}
