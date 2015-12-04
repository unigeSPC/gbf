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

trait BombState[O] extends State[Option[Bomb],O]

trait BombDirectState[O] extends BombState[O] {
  type S = O
  def cleanUp( s: S ) = s
}


import terrain.DEM



case class ImpactListState( fn: String ) extends BombState[Unit] {
  import java.io.PrintWriter
  type S = PrintWriter
  def init = { 
    val pw = new PrintWriter(fn)
    pw.println( ImpactList.header )
    pw
  }
  def aggregate( pw: PrintWriter, impact: Option[Bomb] ) = {
    impact.map( ImpactList.asString ).foreach( pw.println )
    pw
  }
  override def cleanUp( pw: PrintWriter ) = pw.close
  }


object ImpactList {
  
  import scala.math._
  
  lazy val header = List(
    "E", "N", "alt", "mass", "diam", "kinE", "incid", "eject", "flight"
    ).mkString(" ")

  private def twoDig( x: Double ) = "%.2f" format(x)
  private def threeDig( x: Double ) = "%.3f" format(x)
  
  def asString( b: Bomb ): String = {
    val pos = List( b.pos.x.toInt, b.pos.y.toInt, b.pos.z.toInt )
    val phys = List( 
      threeDig(b.mass), 
      twoDig(b.diameter), 
      twoDig( b.kineticEnergy/1000 )
    )
    val incid = (180 - acos( b.v.z / b.v.norm )*180/Pi).toInt 
    val angles = List(  incid, (b.ejectionAngle * 180 / Pi ).toInt )
    val fl = b.time.toInt
    ( pos ++ phys ++ angles :+ fl ).mkString( " " )
  }
    
}



