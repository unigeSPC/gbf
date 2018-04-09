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

import org.streum.configrity._
import terrain.VolcanoDEM

import math.Vec

case class VolcanianConfig( 
  sourceX: Double,
  sourceY: Double,
  sourceZ: Double,
  densAvg: Double,
  densStd: Double,
  phiAvg: Double,
  phiStd: Double,
  velocityAvg: Double,
  velocityStd: Double,
  tilt: Double,
  azimuth: Double,
  spread: Double
 ) {
  lazy val source = Vec( sourceX, sourceY, sourceZ )
  lazy val generator = new VolcanianGenerator(this)
}

object VolcanianConfig {
  
  private def r( key: String ) = read[Double](key)

  def reader( dem: VolcanoDEM ) = for{
    densA <- r( "densAvg" )
    densS <- r( "densStd" )
    phiA  <- r( "phiAvg" )
    phiS  <- r( "phiStd" )
    vA    <- r( "velocityAvg" )
    vS    <- r( "velocityStd" )
    tl  <- r( "tilt" )
    az  <- r( "azimuth" )
    spr <- r( "spread" )
  } yield {
    VolcanianConfig( 
      sourceX = dem.vent(0),
      sourceY = dem.vent(1),
      sourceZ = dem.vent(2),
      densAvg = densA,
      densStd = densS,
      phiAvg = phiA,
      phiStd = phiS,
      velocityAvg = vA,
      velocityStd = vS,
      azimuth = az,
      spread = spr,
      tilt = tl
    )
  }

  import scala.math.Pi
  lazy val default = VolcanianConfig( 
    sourceX = VolcanoDEM.default.vent(0),
    sourceY = VolcanoDEM.default.vent(1),
    sourceZ = VolcanoDEM.default.vent(2),
    densAvg = 1800,
    densStd = 100,
    phiAvg = -7.65,
    phiStd = 1.2,
    velocityAvg = 125,
    velocityStd = 25,
    spread = Pi/12,
    tilt = 0,
    azimuth = 0
  )

}

