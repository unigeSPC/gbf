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

import math.Vec

object Parabola extends App {

  val bomb = Bomb(
    1L,
    0,
    Vec(0,0,0),
    Vec(100,0,100),
    1.0,
    1.0,
    -1.0
  )

  val pw = new java.io.PrintWriter("dat.out")
  var b = bomb
  val dt = 0.1
  for( i <- 0 until 200 ) {
    val t = dt*i
    b = b.move(dt)
    pw.println( t + " " + b.pos.x + " " + b.pos.y + " " + b.pos.z )
  }
  pw.close

}
