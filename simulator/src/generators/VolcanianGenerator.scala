/*
 Copyright 2012-2015, University of Geneva.

 This file is part of Great Balls of Fire (GBF).

 Great Balls of Fire (GBF) is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.
 
 Great Balls of Fire (GBF) is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Great Balls of Fire (GBF).  If not, see
 <http://www.gnu.org/licenses/>.
*/

package ch.unige.gbf
package generators

import math.Vec
import math.RNG
import scala.math._

import org.streum.configrity._

class VolcanianGenerator( config: VolcanianConfig ) extends BombGenerator {
  import config._
 
  private def rotation( tilt: Double, azimuth: Double ): Vec=>Vec = { 
    val a = tilt * Pi /180
    val b = ( (azimuth+540) % 360 ) * Pi / 180 //Change angle rotation to CCW
    val cosa = cos(a)
    val sina = sin(a)
    val cosb = cos(b)
    val sinb = sin(b)
    (v: Vec) => {
      val x = -(sina*sinb*v.z - cosa*sinb*v.y + cosb*v.x)
      val y = -sina*cosb*v.z + cosa*cosb*v.y + sinb*v.x
      val z = cosa*v.z + sina*v.y
      Vec(x,y,z)
    }
  }

  //FIXME: For testing purpose only
  private lazy val testRot = rotation( 30*Pi/180, 0 * Pi / 180 )

  def apply( id: Long, rng: RNG ): Bomb = {
    val vNorm = rng.nextGaussian( velocityAvg, velocityStd )
    val spreadRad = spread * Pi / 180
    val phi = abs( rng.nextGaussian( 0, spreadRad ) )
    val vz = vNorm * cos(phi)
    if( vz <= 0 ) {
      apply(id,rng)
    } else {
      val pos = source
      val theta = rng.nextDouble( 2*Pi )
      val sinPhi = sin(phi)
      val vx = vNorm * sinPhi * cos(theta)
      val vy = vNorm * sinPhi * sin(theta)
      val v0 = Vec( vx, vy, vz ) //FIXME: Restore after testing
      val v = rotation( tilt, azimuth )(v0)
      val density = rng.nextGaussian( densAvg, densStd )
      val phiSize = rng.nextGaussian( phiAvg, phiStd )
      val diameter = pow( 2.0, -phiSize )/1000
      if( diameter <= 0 || density <= 0 ) {
	    apply(id,rng)
      } else {
	    val volume = 4.0/3.0*Pi*pow(diameter/2, 3)
	    val mass = volume*density
	    Bomb( id, 0, pos, v, mass, diameter, phi )
      }
    }
  }

}

