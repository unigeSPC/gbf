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
import akka.routing._


object Manager {
  def props( workerProps: Props, collectorProps: Props ) = 
    Props( new Manager( workerProps, collectorProps ) )
}

class Manager( workerProps: Props, collectorProps: Props ) 
extends Actor with ActorLogging {

  private var runConf: RunConfig = _
  private var next: Long = -1L
  private var available: Int = -1
  private var workers: Router = _
  private var collector: ActorRef = _

  def receive = {
    case Start( rc ) => {
      log.info( "Starting" )
      runConf = rc
      next = 0
      startWorkers()
      startCollector()
      sendJobs()
    }
    case bRes: BombResult => {
      collector ! bRes
      available += 1
      if( allDone ) {
        collector ! CleanUp
        workers.route( PoisonPill, self )
      } else {
        sendJobs()
      }
    }
    case Done => {
      log.info( "Shutting Down" )
      context.system.terminate()
    }
  }

  private def allSent: Boolean = 
    next >= runConf.numBombs

  private def allDone: Boolean =
    allSent && available == runConf.numWorkers 

  private def sendJobs(): Unit = {
    if( ! allSent ) {
      val job = IDBatch( 
        next, 
        ( next + runConf.batchSize ) min runConf.numBombs
      )
      log.info( s"Sending $job" )
      workers.route( job, self )
      available -= 1
      next += ( job.to - job.from )
      if( available > 0 ) sendJobs()
    }
  }

  private def startWorkers(): Unit = {
    val router = {
      val routees = Vector.tabulate( runConf.numWorkers ) { i =>
        val r = context.actorOf( 
          workerProps.withDispatcher("worker-dispatcher"), 
          s"worker_$i" 
        )
        ActorRefRoutee(r)
      }
      Router(SmallestMailboxRoutingLogic(), routees)
    }
    workers = router
    available = runConf.numWorkers
  }

  private def startCollector(): Unit = {
    collector = context.actorOf( collectorProps, "collector" )
  }

}
