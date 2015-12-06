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

import terrain.VolcanoDEM
import solver._
import xp._
import generators._
import math.RNG

import org.streum.configrity._

object Main extends App {

  import util.conf._

  if( args.size != 1 ) {
    println( "ERROR: must provide a valid config file" )
    println( "Usage: gbf <config>" )
    sys.exit
  }

  val conf = loadConfig( args(0) )

  val exp = for {
    rng <- RNG.reader.detaching("rng")
    wind <- Wind.reader.detaching( "wind" ) 
    dem <- VolcanoDEM.reader.detaching( "terrain" )
    baseDragConfig <- BaseDragConfig.reader( 
      wind=wind.array,
      vent=dem.vent 
    ).detaching( "drag" )
    volConf <- VolcanianConfig.reader(dem).detaching( "source" )
    outputFile <- read[String]( "experiment.outputFile" )
    terrain = dem.terrain
    state = ImpactListState( outputFile )
    exp = Experiment.withDrag( 
      volConf.generator,
      baseDragConfig,
      terrain,
      rng,
      state
    )
    iter <- read[Int]( "experiment.size")
    //iter = 100
  } yield exp.run(iter)

  exp(conf)

}

