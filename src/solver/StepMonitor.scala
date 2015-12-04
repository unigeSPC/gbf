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


trait StepMonitor {
  def apply( t: Double, y: SafeArray[Double] ): Boolean
}

case class MaxTime( tEnd: Double ) extends StepMonitor {
  def apply( t: Double, y: SafeArray[Double] ): Boolean = 
    t < tEnd
}

case class MinAltitude( altitude: Double ) extends StepMonitor {
  def apply( t: Double, y: SafeArray[Double] ): Boolean =
    y(5) > altitude
}

import terrain.TerrainMap

case class Terrain( tm: TerrainMap ) extends StepMonitor {
  def apply( t: Double, y: SafeArray[Double] ): Boolean =
    tm( y(3), y(4) ) < y(5)

}
