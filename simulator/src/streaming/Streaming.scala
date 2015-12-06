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
package streaming

import akka.stream.scaladsl._
import java.io.PrintWriter
import scala.concurrent._

import generators.BombGenerator
import math.RNG
import solver.BombBridge
import xp.ImpactList



object Streaming {

  def serial(
    rng: RNG,
    gen: BombGenerator,
    solver: BombBridge,
    pw: PrintWriter
  ):Seq[Long]=>Unit = { ids => 
    for( id <- ids ) {
      val b0 =  gen(id, rng)
      val b1 = solver.run(b0)
      val s = ImpactList.asString(b1)
      pw.println(s)
    }
    pw.close
  }

  def pipeline(
    rng: RNG,
    gen: BombGenerator,
    solver: BombBridge
  ): Flow[Long,String,Unit] =
    Flow[Long]
      .map( id => gen(id, rng) )
      .map( b => solver.run(b) )
      .map( ImpactList.asString )

  def fused(
    rng: RNG,
    gen: BombGenerator,
    solver: BombBridge
  ): Flow[Long,String,Unit] =
    Flow[Long].map{ id =>
      val b = gen(id, rng) 
      val r = solver.run(b)
      ImpactList.asString(r)
    }

  def parallel(
    parallelism: Int,
    rng: RNG,
    gen: BombGenerator,
    solver: BombBridge
  )(implicit ec: ExecutionContext): Flow[Long,String,Unit] =
    Flow[Long]
      .mapAsyncUnordered( parallelism ){ id =>
      Future{
        val b = gen(id, rng)
        solver.run(b)
      }
    }.map( ImpactList.asString ).log( "Done" )

  def parallelBatch(
    parallelism: Int,
    batchSize: Int,
    rng: RNG,
    gen: BombGenerator,
    solver: BombBridge
  )(implicit ec: ExecutionContext): Flow[Long,String,Unit] =
    Flow[Long]
      .grouped( batchSize )
      .mapAsyncUnordered( parallelism ){ ids =>
      Future{
        ids.map( gen(_, rng) ).map( solver.run(_) )
      }
    }.mapConcat( _.map(ImpactList.asString) )

  def parallelCon(
    parallelism: Int,
    rng: RNG,
    gen: BombGenerator,
    solver: BombBridge
  )(implicit ec: ExecutionContext): Flow[Long,String,Unit] =
    Flow[Long]
      .mapAsyncUnordered( parallelism )( id => Future(gen( id, rng ) ))
      .mapAsyncUnordered( parallelism ){ b => Future{
        solver.run(b)
      }}
      .map( ImpactList.asString )


  def fileSink( pw: PrintWriter ): Sink[String,Future[Unit]] =
    Sink.foreach( pw.println )

}

object StreamingDemo extends App {
  import ch.unige.gbf.util.conf._
  import math.RNG
  import terrain.VolcanoDEM
  import solver._
  import xp._
  import generators._
  import org.streum.configrity._
  import akka.actor._
  import akka.stream._

  val conf = loadConfig( args(0) )

  val outputR =
    read[String]( "experiment.outputFile" )
      .map( new PrintWriter(_) )

  val confR = for {
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
  } yield ( RNG(seed), gen, bombBridge  )

  val itersR = read[Long]( "experiment.size" ).map( 1L to _ )

  def parallel() = {

    implicit val system = ActorSystem("Sys")
    implicit val materializer = ActorMaterializer()
    import system.dispatcher

    val flowR = confR.map {
      case (r,g,bb) => Streaming.parallelCon( 5, r, g, bb  )
      //case (r,g,bb) => Streaming.fused( r, g, bb  )
    }

    val streamR = for {
      flow <- flowR
      output <- outputR
      iters <- itersR
      sink = Streaming.fileSink( output )
      source = Source(iters)
    } yield {
      val res = source.via( flow ).runWith( sink )
      res.onComplete{ _ =>
        output.close()
        system.shutdown
      }
    }

    streamR(conf)
  }

  def serial() = {
    val serialR = for {
      conf <- confR
      (r,g,bb) = conf
      pw <- outputR
      iters <- itersR
    } yield {
      Streaming.serial(r,g,bb,pw)(iters)
    }

    serialR(conf)
  }

  parallel

}
