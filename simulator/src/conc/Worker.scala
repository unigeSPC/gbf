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

import math.RNG
import generators.BombGenerator
import solver.BombBridge

import akka.actor.{Actor,Props,ActorLogging}

object Worker {
  def props( gen: BombGenerator, solver: BombBridge, seed: Int  ) =
    Props( new Worker( gen, solver, seed ) )
}

class Worker( gen: BombGenerator, solver: BombBridge, seed: Int  ) 
extends Actor with ActorLogging{

  override def preStart() {
    log.info( "Worker started" )
  }

  override def postStop() {
    log.info( "Worker stopped" )
  }


  private def compute( id: Long, rng: RNG ) = {
    val b = gen( id, rng )
    solver.run( b )
  }

  def receive = {
    case IDBatch( from, to ) => {
      log.debug( s"Received batch ($from-$to)" )
      val rng = RNG( ( seed + from ).toInt )
      val bs = ( from until to ) map { id => Some( compute(id,rng) ) }
      log.debug( s"Computed batch ($from-$to)" )
      sender() ! BombResult( bs )
    }
  }


}
