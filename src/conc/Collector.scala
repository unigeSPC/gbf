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

import xp.BombState

import akka.actor.{Actor,Props, ActorLogging}

object Collector {
  def props( state: BombState[Unit] ) = 
    Props( new Collector( state ) )
}

class Collector( state: BombState[Unit] ) extends Actor with ActorLogging {

  private var s: state.S = state.init

  override def preStart() {
    log.info( "Collector started" )
  }

  override def postStop() {
    log.info( "Collector stopped" )
  }


  def receive = {
    case BombResult( bs ) => {
      log.debug( "Aggregating" )
      s = state.aggregateMany( s, bs )
      log.debug( "Aggregated" )
    }
    case CleanUp => {
      log.info( "Cleaning-up" )
      state.cleanUp( s )
      sender ! Done
    }
  }


}
