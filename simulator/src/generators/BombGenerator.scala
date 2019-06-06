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
package generators

import math.RNG


trait BombGenerator extends ((Long,RNG)=>Bomb) {

  def apply( id: Long, rng: RNG ): Bomb

}

object BombGenerator {


  def wrap( p: BombGenerator )( f: Bomb=>Bomb ): BombGenerator = new WrappedGenerator {
    val parent = p
    def handler( b: Bomb ) = f(b)
  }

  def logged( out: java.io.PrintStream, p: BombGenerator ): BombGenerator = {
    wrap( p ){ b => 
      out.println(b)
      b
    }
  }

}

trait WrappedGenerator extends BombGenerator {

  def parent: BombGenerator

  def handler( b: Bomb ): Bomb

  def apply( id: Long, rng: RNG ): Bomb =
    handler( parent( id, rng ) )

}
