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

import generators.VolcanianConfig
import generators.BombGenerator
import terrain.{TerrainMap,FlatMap,VolcanoDEM}
import math.RNG
import solver._
import util.arrays._




case class SimpleDragExperiment(
  generator: BombGenerator,
  solver: BombBridge,
  output: String,
  rng: RNG
) {

  def run( n: Int ): Unit = {
    val out = new java.io.PrintWriter(output)
    ( 0 until n ) foreach { id =>
      val b0 = generator( id, rng ) 
	  val b = solver.run( b0 )
      out.println( ImpactList.asString( b ) )
    }
    out.close()
  }
  
}

object SimpleDragExperiment extends App {

  val h = 0.1

  val bombCfg = VolcanianConfig.default
  
  val vent = safe{ 
    import bombCfg._
    Array( sourceX, sourceY, sourceZ )
  }

  val bombGen =  bombCfg.generator
  val baseDrag = BaseDragConfig.default.copy(
    wind = safe( Array( 0.0, 16.0 ) ),
    timeStep = h
  )

  val solver = BombBridge( baseDrag, VolcanoDEM.default.terrain )

  val exp = SimpleDragExperiment(
    bombGen,
    solver,
    "dragTest_wind.dat",
    RNG( 12 )
  )

  exp.run(1000 * 1000)

}


/*
object WindParticleDragExperiment extends App {

  import math.Vec

  val h = 0.01

  val bombCfg = VolcanianTest.config
  
  val vent = safe{ 
    import bombCfg._
    Array( 0.0, 0.0, 0.0 )
  }

  val baseDrag = BaseDragConfig(
    pressure0 = 1.01325e5,
    temp0 = 298,
    thermalLapse = -6.5e-3,
    reducedDragRadius = 0,
    vent = vent,
    wind = safe( Array( 0.0, 0.0 ) )
  )

  val solver = InfiniteBombBridge( baseDrag, 100000, h )

  import scala.math.{cos,sin,Pi,pow}

  val density = 2500.0
  val diam = 1.0
  val mass = density * 4.0/3*Pi * pow(diam/2,3)

  val v = Vec( 0.0, 0.0, 0.0 )

  val b = Bomb( 
    ID=0, 
    time=0, 
    pos=Vec(0,0,20000), 
    v=v, 
    mass=mass, 
    diameter=diam 
  )

  val b2 = solver.run(b)
  
  println( b )

  println( b2 )

}


object SingleParticleDragExperiment extends App {

  import math.Vec

  val h = 0.0001

  val terrain = new FlatMap( 
    resolution=2, 
    error=0, 
    lx=2000, 
    ly=2000, 
    altitude=0
  )

  val bombCfg = VolcanianTest.config
  
  val vent = safe{ 
    import bombCfg._
    Array( 0.0, 0.0, 0.0 )
  }

  val baseDrag = BaseDragConfig(
    pressure0 = 1.01325e5,
    temp0 = 298,
    thermalLapse = -6.5e-3,
    reducedDragRadius = 200,
    vent = vent,
    wind = safe( Array( 0.0, 0.0 ) )
  )

  val solver = BombBridge( baseDrag, terrain, h )

  import scala.math.{cos,sin,Pi,pow}

  val density = 2500.0
  val diam = 1.0
  val mass = density * 4.0/3*Pi * pow(diam/2,3)

  val vNorm = 150.0
  val phi = 70.0 / 180 * Pi


  val v = Vec( vNorm*cos(phi), 0.0, vNorm*sin(phi))

  val b = Bomb( 
    ID=0, 
    time=0, 
    pos=Vec(0,0,0), 
    v=v, 
    mass=mass, 
    diameter=diam 
  )

  val b2 = solver.run(b)
  
  println( b )

  println( b2 )

}
*/
