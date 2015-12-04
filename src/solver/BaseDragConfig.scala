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
import scala.math.{sqrt,pow,Pi}

case class BaseDragConfig(
  pressure0: Double,
  temp0: Double,
  thermalLapse: Double,
  reducedDragRadius: Double,
  timeStep: Double,
  vent: SafeArray[Double],
  wind: SafeArray[Double]
) 


object BaseDragConfig {
  lazy val default = BaseDragConfig(
    pressure0 = 1.01325e5,
    temp0 = 298,
    thermalLapse = -6.5e-3,
    reducedDragRadius = 200,
    timeStep = 0.01,
    vent = safe( Array( 0.0, 0.0, 0.0 ) ),
    wind = safe( Array( 0.0, 0.0, 0.0 ) )
  )

  import org.streum.configrity._

  private def r( key: String ) = read[Double](key)

  def reader( vent: SafeArray[Double], wind: SafeArray[Double] ) =
    for {
      p0 <- r("pressure0")
      t0 <- r("temp0")
      tl <- r("thermalLapse")
      redD <- r("reducedDragRadius")
      tSt <- r("timeStep")
    } yield BaseDragConfig( p0, t0, tl, redD, tSt, vent, wind )
  
}
