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

case class DEM( 
  resolution: Double,
  error: Double,
  cx: Double,
  cy: Double,
  private val data: Array[Array[Double]]
) extends TerrainMap {

  val nI: Int = data.size
  val nJ: Int = data(0).size


  val lx: Double = (nJ-1)*resolution
  val ly: Double = (nI-1)*resolution

  lazy val highest: Double = data.map( _.max ).max

  def apply(x: Double,y: Double): Double = {
    val i = nI - ( (y-cy)/resolution ).round.toInt - 1
    val j = ( (x-cx)/resolution ).round.toInt
    if( i < 0 || j < 0 || i >= nI || j >= nJ ) { 
      //println( s"Outside Domain $i $j $x $y")
      -1.0
    } else data(i)(j)
  }

}


object DEM {

  lazy val spaces =  """\s+"""
  val error = 1

  def read( fn: String ): DEM = {
    val lines = io.Source.fromFile( fn ).getLines.toStream
    val header = lines.take(6).map( _.split( spaces )(1) ).toVector
    val nCols = header(0).toInt
    val nRows = header(1).toInt
    val xLLCorner = header(2).toDouble
    val yLLCorner = header(3).toDouble
    val cellSize = header(4).toDouble
    val noData = header(5).toDouble
    val ary = Array.ofDim[Array[Double]]( nRows )
    lines.drop(6).zipWithIndex.foreach {
      case (l,i) => {
        val row = l.split( spaces ).map{  s => 
          val x = s.toDouble
          if( x == noData ) -1.0 else x
        }
        require( row.size == nCols )
        ary(i) = row
      }
    }
    DEM( cellSize, error, xLLCorner, yLLCorner, ary )
  }


}

