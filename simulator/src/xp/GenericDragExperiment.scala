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

import generators.BombGenerator
import terrain.TerrainMap
import math.RNG
import solver._

abstract class DragExperiment[E](
  val generator: BombGenerator,
  val dragConf: BaseDragConfig,
  val terrain: TerrainMap,
  rng: RNG
) {

  val landing = BombBridge( dragConf, terrain )
  
  def run( n: Int ): E = {
    val res = ( 0 until n ).foldLeft( init ){
      case (state,id) => {
	    val b = landing.run( generator( id, rng ) )
	    aggregate( state, Some(b) )
      }
    }
    cleanUp()
    res
  }

  def init: E
  def aggregate( previous: E, impact: Option[Bomb] ): E
  
  def cleanUp(): Unit = {}
}
