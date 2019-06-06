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

class Experiment[O](
  generator: BombGenerator,
  rng: RNG,
  runSingle: Bomb=>Option[Bomb],
  state: BombState[O]
){
  def run( n: Int ): O = {
    def rec( id: Int, s: state.S ): O =
      if( id == n ) state.cleanUp(s)
      else {
        val b = generator(id,rng)
        val b2 = runSingle(b)
        val s2 = state.aggregate( s, b2 )
        rec( id+1, s2 )
      }
    rec( 1, state.init )
  }
}


object Experiment {
  
  import solver._

  def withDrag[O](
    generator: BombGenerator,
    dragConf: BaseDragConfig,
    terrain: TerrainMap,
    rng: RNG,
    bombState: BombState[O]
  ): Experiment[O] = {
    val bombBridge = BombBridge( dragConf, terrain )
    val f = (b:Bomb) => Some( bombBridge.run(b) ) //TODO: Bomb should not go outside domain
    new Experiment[O]( generator, rng, f, bombState )
  }


}
