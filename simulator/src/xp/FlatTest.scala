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
package xp


import terrain.FlatMap
import math.RNG
import solver._
import generators._

object FlatTest extends App{

  val alt = 10
  val lx = 2000
  val ly = 2000
  val ventZ = alt

  val config = VolcanianConfig( 
    sourceX = lx/2,
    sourceY = ly/2,
    sourceZ = ventZ,
    densAvg = 2000,
    densStd = 500,
    phiAvg = -7.65,
    phiStd = 1.2,
    velocityAvg = 40,
    velocityStd = 10,
    tilt = 75,
    spread = 4,
    azimuth = 90
  )

  val dragConfig = BaseDragConfig.default

  val terrain = new FlatMap( 
    resolution = 10,
    error = 0.5,
    lx = lx,
    ly = ly,
    altitude = alt
  )

  val generator =  new VolcanianGenerator( config )
  
  val exp = Experiment.withDrag(
    generator,
    dragConfig,
    terrain,
    RNG(0),
    ImpactListState("tilt/tilt=75E_spread=4.dat")
  )

  exp.run(1000)


  
}
