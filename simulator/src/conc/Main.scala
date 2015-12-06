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
package conc

import akka.actor._

import terrain.VolcanoDEM
import solver.{Wind,BaseDragConfig,BombBridge}
import xp.ImpactListState
import generators.VolcanianConfig
import math.RNG
import util.conf._

import org.streum.configrity._

object ConcMain extends App {

  if( args.size != 2 ) {
    println( "ERROR: must provide a valid config file" )
    println( "Usage: gbf <config> <numWorkers>" )
    sys.exit
  }

  val numWorkers = args(1).toInt

  if( numWorkers < 1 ) {
    ch.unige.gbf.Main.main( Array( args(0) ) )
  } else {

    run( numWorkers, args(0) )
  }

  def run( nWorkers: Int, confFN: String ) = {

    val conf = loadConfig( confFN )

    val workerPropsR = for {
      seed <- read[Int]( "rng.seed" )
      wind <- Wind.reader.detaching( "wind" ) 
      dem <- VolcanoDEM.reader.detaching( "terrain" )
      baseDragConfig <- BaseDragConfig.reader( 
        wind=wind.array,
        vent=dem.vent 
      ).detaching( "drag" )
      volConf <- VolcanianConfig.reader(dem).detaching( "source" )
      terrain = dem.terrain
      bombBridge = BombBridge( baseDragConfig, terrain )
      gen = volConf.generator
    } yield Worker.props( gen, bombBridge, seed )
    
    val collectorPropsR = for {
      outputFile <- read[String]( "experiment.outputFile" )
    } yield Collector.props( ImpactListState( outputFile ) )
    
    val managerPropsR = for {
      wp <- workerPropsR
      cp <- collectorPropsR
    } yield Manager.props( workerProps=wp, collectorProps=cp )
    
    val managerProps = managerPropsR( conf )
    
    val system = ActorSystem( "gbf" )
    
    val manager = system.actorOf( managerProps, "manager" )
    
    manager ! Start(
      RunConfig(
        numBombs = conf[Int]( "experiment.size" ),
        numWorkers = nWorkers, 
        batchSize = 1000
      ): ch.unige.gbf.conc.RunConfig
    )
  }
  
}
