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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES11;
import android.opengl.GLUtils;

/**
 * Representation and rendering of a quad
 * 
 * @author Kenneth Maffei
 *
 */
public class Quad {
	private ByteBuffer vertexByteBuffer;
	private ByteBuffer textureByteBuffer;
    private FloatBuffer vertexBuffer;   //Buffer holding the vertices 
    
    public float transX;				//X translation
    public float transY;				//Y translation
    public float transZ;				//Z translation
    
    public float scaleX = 1.0f;				//X scale
    public float scaleY = 1.0f;				//Y scale
    public float scaleZ = 1.0f;				//Z scale
    
    public float rotateX = 0.0f;				//X rotation
    public float rotateY = 0.0f;				//Y rotation
    public float rotateZ = 0.0f;				//Z rotation
    
    public float width;						//start width
    public float height;						//start height
  
    public boolean isParticle;			//Are we a particle
    public boolean isFacingParticle;	//Are we a facing particle, or just a regular quad
    
    //The current colors for the quad
    public float r = 1.0f;
    public float g = 1.0f;
    public float b = 1.0f;
    public float a = 1.0f;
    
    private float vertices[] = { 
            0.0f, 0.0f,  0.0f,        // V1 - bottom left 
            0.0f, 0.0f,  0.0f,        // V2 - top left 
            0.0f, 0.0f,  0.0f,        // V3 - bottom right 
            0.0f, 0.0f,  0.0f         // V4 - top right 
    }; 
    

    private FloatBuffer textureBuffer;  // buffer holding the texture coordinates 
    private float texture[] = { 
            // Mapping coordinates for the vertices 
            0.0f, 1.0f,     // top left     (V2) 
            0.0f, 0.0f,     // bottom left  (V1) 
            1.0f, 1.0f,     // top right    (V4) 
            1.0f, 0.0f      // bottom right (V3) 
    }; 
    

    /** The texture pointer */ 
    public int[] glTexture = new int[1]; 
    
    //Places the square at the center of the screen
    public Quad(float width, float height, Vector3 position) { 
    	transX = position.x;
    	transY = position.y;
    	transZ = position.z;
    	
    	this.width = width;
    	this.height = height;
    	
    	vertices[0] = -width/2.0f;
    	vertices[1] = -height/2.0f;
    	vertices[3] = -width/2.0f;
    	vertices[4] = height/2.0f;
    	vertices[6] = width/2.0f;
    	vertices[7] = -height/2.0f;
    	vertices[9] = width/2.0f;
    	vertices[10] = height/2.0f;
    	
    	vertexByteBuffer = ByteBuffer.allocateDirect(vertices.length * 4); 
    	vertexByteBuffer.order(ByteOrder.nativeOrder()); 
    	vertexBuffer = vertexByteBuffer.asFloatBuffer(); 

    	vertexBuffer.put(vertices); 
    	vertexBuffer.position(0); 

	    textureByteBuffer = ByteBuffer.allocateDirect(texture.length * 4); 
	    textureByteBuffer.order(ByteOrder.nativeOrder()); 
	    textureBuffer = textureByteBuffer.asFloatBuffer();

        textureBuffer.put(texture); 
        textureBuffer.position(0); 
    	
    	glTexture[0] = -1;
    } 
    
    public void reinitialize(float width, float height, Vector3 position) {
    	transX = position.x;
    	transY = position.y;
    	transZ = position.z;
    	
    	this.width = width;
    	this.height = height;
    	
    	scaleX = scaleY = 1.0f;
    	
    	vertices[0] = -width/2.0f;
    	vertices[1] = -height/2.0f;
    	vertices[3] = -width/2.0f;
    	vertices[4] = height/2.0f;
    	vertices[6] = width/2.0f;
    	vertices[7] = -height/2.0f;
    	vertices[9] = width/2.0f;
    	vertices[10] = height/2.0f;

    	vertexBuffer.put(vertices); 
    	vertexBuffer.position(0); 

        textureBuffer.put(texture); 
        textureBuffer.position(0); 
    }
    
    /**
     * We will use the glTranslate and glScale during rendering rather than resetting the vertices
     * Updating the vertex buffers is too expensive an operation
     * 
     * @param newWidth - updated width
     * @param newHeight - updated height
     * @param position - updated position;
     */
    public void resetSize(float newWidth, float newHeight, Vector3 position) {
    	transX = position.x;
    	transY = position.y;
    	transZ = position.z;
    	
    	scaleX = newWidth/width;
    	scaleY = newHeight/height;
    }
    
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
			if(file.indexOf(".png") > 0)
				opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
			else
				opt.inPreferredConfig = Bitmap.Config.RGB_565;
			
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
    	catch(IOException IOerror)
		{
    		return false;
		}
    	return true;
    } 
    
    /**
     * If creating the texture externally, set it here
     * 
     * @param tex
     */
    public void setTexture(int tex) {
    	glTexture[0] = tex;
    }
    
    /** The draw method for the square with the GL context */ 
    public void draw(GL10 gl) { 

    	GLES11.glColor4f(r, g, b, a); 
	    
	    GLES11.glPushMatrix();
	    
	    GLES11.glTranslatef(transX, transY, transZ);
	    
	    if(!isParticle) {
	    	//GLES11.glEnable(GLES11.GL_LIGHTING);
	    	GLES11.glEnable(GLES11.GL_BLEND);
	    	GLES11.glTexEnvi(GLES11.GL_TEXTURE_ENV, GLES11.GL_TEXTURE_ENV_MODE, GLES11.GL_MODULATE);
	    	GLES11.glBlendFunc (GLES11.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);
	 	    
	    	GLES11.glBindTexture(GLES11.GL_TEXTURE_2D, glTexture[0]);
	    }
	    
	    //If we are a facing particle, then we have to apply the billboard routine
	    if(isFacingParticle) {
		    if (Globals.thetaTest)
				GLES11.glRotatef((float)(Math.acos(Globals.theta)*180.0f/3.14f), Globals.upAux.x, Globals.upAux.y, Globals.upAux.z);
		    
		    if (Globals.phiTest) {
				if (Globals.billboardDirectionTest)
					GLES11.glRotatef((float) (Math.acos(Globals.phi)*180.0f/3.14f), 1, 0, 0);	
				else
					GLES11.glRotatef((float) (Math.acos(Globals.phi)*180.0f/3.14f), -1,0, 0);
			}
	    }
	    else {
	    	GLES11.glRotatef(rotateZ, 0, 0, 1);
	    	GLES11.glRotatef(rotateY, 0, 1, 0);
	    	GLES11.glRotatef(rotateX, 1, 0, 0);
	    }
	    
	    GLES11.glScalef(scaleX, scaleY, scaleZ);

        //Point to our vertex buffer 
        GLES11.glVertexPointer(3, GLES11.GL_FLOAT, 0, vertexBuffer); 
        GLES11.glTexCoordPointer(2, GLES11.GL_FLOAT, 0, textureBuffer); 

        //Draw the vertices as triangle strip 
        GLES11.glDrawArrays(GLES11.GL_TRIANGLE_STRIP, 0, vertices.length / 3);  
        
        GLES11.glPopMatrix();
        
        if(!isParticle)
        	GLES11.glDisable(GLES11.GL_LIGHTING);
    } 
    
    public void DeleteTexture() {
    	if(glTexture[0] > -1)
    		GLES11.glDeleteTextures(1, glTexture, 0);
    	glTexture[0] = -1;
    }
}
