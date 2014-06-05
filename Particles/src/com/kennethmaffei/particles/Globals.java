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

import java.util.Random;

import android.content.Context;

/**
 * These are parameters and functions that may be used/shared by various classes
 * @author Kenneth Maffei
 *
 */
public class Globals {

	public static float deviceWidth = 0.0f;
	public static float deviceHeight = 0.0f;
	
	public static Context context;
	
	public static Random r = new Random();
	
	public static float Deg2Rad(float deg) {
		return (3.14159265359f/180.0f)*(deg);
	}
	
	public static float random() {
		return r.nextFloat();
	}
	
	//Globals for billboarding
	public static float theta;
	public static float phi;
	public static Vector3 upAux = new Vector3();
	public static boolean billboardDirectionTest;
	public static boolean thetaTest;
	public static boolean phiTest;
}
