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
package solver

import util.arrays._
import org.streum.configrity._

case class Wind( speed: Double, direction: Double ) {

  import scala.math.{cos,sin,Pi}

  private val phi = {
    val d = ( 90 - direction + 360 ) % 360
    d/180.0 * Pi
  }

  lazy val array: SafeArray[Double] = safe{
    Array(
      cos(phi)*speed,
      sin(phi)*speed
    )
  }

}

object Wind {

  lazy val reader = for {
    sp <- read( "speed", 0.0 )
    dir <- read("direction", 0.0 )
  } yield Wind(sp,dir)
  

}
