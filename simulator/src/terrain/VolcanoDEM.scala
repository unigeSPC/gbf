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
package terrain

import scala.io.Source
import terrain._
import util.arrays._

case class VolcanoDEM( terrain: DEM, vent: SafeArray[Double] )

object VolcanoDEM {

  import org.streum.configrity._
  import util.conf._
  
  lazy val ventReader = for {
    e <- read[Double]( "E" )
    n <- read[Double]( "N" )
    a <- read[Double]( "altitude" )
  } yield safe( Array( e, n, a ) )
  
  lazy val reader = for {
    demFile <- read[String]( "demFile" )
    vent <- ventReader.detaching( "vent" )
  } yield {
    val dem = DEM.read( demFile )
    VolcanoDEM( dem, vent )
  }

  lazy val default = VolcanoDEM(
    DEM.read( "dem/dem_10m.txt" ),
    safe( Array( 496682.0, 4250641.0, 400.0 ) )
  )

}

