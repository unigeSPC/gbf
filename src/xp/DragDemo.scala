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

import util.arrays._
import solver._

object DragDemo extends App {

  import RK4._

  val dem = terrain.VolcanoDEM.default

  val ejectConf = EjectDragConfig(
    diameter = 0.3,
    mass = 15.9,
    density = 2000,
    pressure0 = 1.013e5,
    temp0 = 25 + 273.15,
    thermalLapse = -6.5/1000,
    reducedDragRadius = 100.0,
    vent = safe( Array(dem.vent(0),dem.vent(1),dem.vent(2)) ),
    wind = safe( Array(0.0,0.0,0.0) )
  )


  val f = EjectDrag( ejectConf )
  val solv = RK4( f, 0.0001 )
  val mon = Terrain( dem.terrain )
  
  val y0 = safe( Array( 
    60.0, 60.0, 60.0, 
    dem.vent(0),dem.vent(1),dem.vent(2)
  ))

  val (t,y) = solv.run( 0.0, y0, mon )

  println( y0.unsafe.mkString(", ") )
  println(y.mkString(", "))
  
}

