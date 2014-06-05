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

/**
 * Class for a 3-Vector in 3D space
 * 
 * @author Ken
 *
 */
public class Vector3 {
	public float x;
	public float y;
	public float z;
	
	/**
	 * Constructors
	 */
	public Vector3() {};
	public Vector3(float a, float b, float c) {
		x = a;
		y = b;
		z = c;
	}

	/**
	 * Copies one Vector3's parameters into this one's
	 * @param a
	 */
	public void copy(Vector3 a) {
		x = a.x;
		y = a.y;
		z = a.z;
	}
	
	/**
	 * Subtract a Vector3 from this one
	 * 
	 * @param a - the Vector3 to subtract
	 */
	public void subtract(Vector3 a) {
		x-= a.x;
		y-= a.y;
		z-= a.z;
	}
	
	/**
	 * Adds a Vector3 to this one
	 * 
	 * @param a - the Vector3 to add
	 */
	public void add(Vector3 a) {
		x+= a.x;
		y+= a.y;
		z+= a.z;
	}
	
	/**
	 * Scales this Vector3 and returns a new scaled Vector3
	 * 
	 * @param a - the scale factor
	 * @return - the new vector
	 */
	public Vector3 scaled(float a) {
		Vector3 temp = new Vector3();
		temp.copy(this);
		
		temp.x*= a;
		temp.y*= a;
		temp.z*= a;
		
		return temp;
	}
	
	/**
	 * Returns the length of this Vector3
	 * 
	 * @return - the length
	 */
	public float length() {
		return (float) Math.sqrt(x*x + y*y + z*z);
	}
	
	/**
	 * Normalizes this Vector3
	 * 
	 * @return - itself
	 */
	public Vector3 normalize() {
		float length = length();	

		if(length < 0.00001f)
			length = 1.0f;

		float invLength = 1.0f/length;

		x*= invLength;
		y*= invLength;
		z*= invLength;

		return this;
	}
	
	/**
	 * Static function to calculate the dot product between two Vector3s
	 * 
	 * @param a - the first Vector3
	 * @param b - the second Vector3
	 * @return - the dot product
	 */
	public static float dotProduct(Vector3 a, Vector3 b) {
		return (a.x*b.x + a.y*b.y + a.z*b.z);
	}
	
	/**
	 * Static function to calculate the cross product between two Vector3s
	 * 
	 * @param a - the first Vector3
	 * @param b - the second Vector3
	 * @return - the cross product
	 */
	public static Vector3 crossProduct(Vector3 a, Vector3 b) {
		Vector3 cp = new Vector3();
		cp.x = (a.y*b.z - b.y*a.z);
		cp.y = (a.z*b.x - b.z*a.x);
		cp.z = (a.x*b.y - b.x*a.y);
		return cp;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Vector3))
            return false;
        if (obj == this)
            return true;
        
        Vector3 compare = (Vector3)obj;
        if(x == compare.x && y == compare.y && z == compare.z)
        	return true;
        return false;
	}
	
	public int hashCode(Object obj) {
		final int prime = 31;
        int result = 1;

        Float xf = Float.valueOf(x);
        result = prime + xf.hashCode();
        Float yf = Float.valueOf(y);
        result = prime*result + yf.hashCode();
        Float zf = Float.valueOf(z);
        result = prime*result + zf.hashCode();

        return result;
	}
}
