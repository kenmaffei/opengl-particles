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

import android.graphics.PointF;

/**
 * An individual particle, with attributes and a quad for rendering
 * @author Kenneth Maffei
 *
 */
public class Particle {
	public Vector3 position = new Vector3();			//Current position
	public Vector3 velocity = new Vector3();			//Current velocity
	public Vector3 acceleration = new Vector3();		//Current acceleration
	public float lifeTime; 							//Total lifetime
	public float life;  								//Accumulated life at time t
	public PointF size = new PointF();					//Current size
	public PointF deltaSize = new PointF();			//Change in size per time increment
	
	//Current colors and color changes per time increment
	public float colorR;
	public float colorG;
	public float colorB;
	public float colorA;
	//deltaColor is used for the special case of fire, but not for the generic particle systems
	public float deltaColorR;
	public float deltaColorG;
	public float deltaColorB;
	public float deltaColorA;
	
	public Quad quad;
	
	/**
	 * Since particles are re-used, we do not need to re-allocate everything
	 * If the quad already exists, then perform a reinitialize
	 * 
	 * @param startSize - the starting size for the particle
	 * @param tex - openGL texture
	 * @param isFacingParticle - tells the quad what kind we are
	 */
	public void initializeQuad(PointF startSize, int tex, boolean isFacingParticle) {
		if(quad == null) {
			quad = new Quad(startSize.x, startSize.y, position);
			quad.setTexture(tex);
			quad.isParticle = true;
			quad.isFacingParticle = isFacingParticle;
		}
		else
			quad.reinitialize(startSize.x, startSize.y, position);
		
		quad.r = colorR;
		quad.g = colorG;
		quad.b = colorB;
		quad.a = colorA;
	}
	
	/**
	 * Called each frame to update the quad's position
	 */
	public void updateQuad() {
		quad.resetSize(size.x, size.y, position);
		quad.r = colorR;
		quad.g = colorG;
		quad.b = colorB;
		quad.a = colorA;
	}
}
