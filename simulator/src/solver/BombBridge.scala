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

import terrain.TerrainMap

trait AbstractBombBridge {

  def baseConfig: BaseDragConfig
  def forces( b: Bomb ): RK4.F
  def monitor: StepMonitor

  import BombBridge._

  private val configFactory = EjectDragConfigFactory( baseConfig ) 

  def run( b: Bomb ): Bomb = {
    val solver = RK4( forces(b), baseConfig.timeStep )
    val y0 = toVelPos( b )
    val (t,y) = solver.run( 0.0, y0, monitor )
    fromVelPos( t, y, b )
  }

}

case class BombBridge( 
  baseConfig: BaseDragConfig,
  terrain: TerrainMap
) extends AbstractBombBridge {

  import BombBridge._

  val monitor = Terrain( terrain )
  private val configFactory = EjectDragConfigFactory( baseConfig ) 
  def forces( b: Bomb ) = {
    val cfg = configFactory.fromBomb( b )
    EjectDrag( cfg )
  }

}

case class InfiniteBombBridge( 
  baseConfig: BaseDragConfig,
  maxTime: Double
) extends AbstractBombBridge {

  import BombBridge._

  val monitor = MaxTime( maxTime )
  private val configFactory = EjectDragConfigFactory( baseConfig ) 
  def forces( b: Bomb ) = {
    val cfg = configFactory.fromBomb( b )
    EjectDragConstant( cfg )
  }

}


object BombBridge {

  
  def toVelPos( b: Bomb ): SafeArray[Double] = {
    //println(s"TO VEL: ${b}" )
    val ary = Array.ofDim[Double](6)
    ary(0) = b.v.x
    ary(1) = b.v.y
    ary(2) = b.v.z
    ary(3) = b.pos.x
    ary(4) = b.pos.y
    ary(5) = b.pos.z
    //println(s"AFTER: ${ary.toList}")
    safe(ary)
  }

  import math.Vec
  def fromVelPos( t: Double, ary: Array[Double], b: Bomb ): Bomb = {
    //println(s"FROM VEL: ${ary.toList}" )
    //println(s"BEFORE: $b" )
    val b2 = b.copy(
      time=t,
      v=Vec( ary(0), ary(1), ary(2) ),
      pos = Vec( ary(3), ary(4), ary(5) )
    )
    //println(s"AFTER: $b2" )
    b2
  }

  
  case class EjectDragConfigFactory( base: BaseDragConfig ) {
    
    private lazy val tpl = EjectDragConfig(
      diameter = 0,
      mass = 0,
      density = 0,
      pressure0 = base.pressure0,
      temp0 = base.temp0,
      thermalLapse = base.thermalLapse,
      reducedDragRadius=base.reducedDragRadius,
      vent = base.vent,
      wind=base.wind
    )

    def fromBomb( b: Bomb ): EjectDragConfig = tpl.copy(
      diameter = b.diameter,
      mass = b.mass,
      density = b.density
    )

  }

}
