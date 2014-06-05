opengl-particles
================

Android OpenGL Particle System

This is an additive particle system which I adapted from my Ignition Game Engine (a c++ app for Windows). You can create effects like fire and smoke with this system. Each type of system is extended from the base ParticleSystem class. The demo has three subclasses: Generic, Fire and Black Smoke. The code uses OpenGLES11. At some point I'll update it for OpenGLES2.

Additive particle systems are very useful, however you must be aware that they exhibit two limitation. The first is that they are not appropriate if constructed over white or very light backgrounds. This is due to the additive blending which will wash out the effect. The second issue is that very dark particle colors will not show up. This is a result of the color blending per particle against the transparency map that is used. You can google to see how some people have tried to ameliorate these problems, however, in most cases you don't need to worry about them too much.

Given the issue with dark particle colors, the black smoke system uses a different blending mode. The only point here is that the system is strictly black with no option to change it's color.

Particles are planar objects in 3D space, so if the camera moves around the user will see that they are planes. Therefore a billboarding technique so that the particles always orient themselves towards the camera. You can find the billboarding technique used here: http://www.lighthouse3d.com/opengl/billboarding/

Some final notes. The transparency map should not be a 32 bit image with alpha. It should be a 24 bit jpg or other format readable by Android. Also, because of performance, your systems should be limited to a couple hundred particles or less.
