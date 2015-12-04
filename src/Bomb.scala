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

import math.Vec
import constants._

import scala.math.{Pi,pow}

case class Bomb( 
  ID: Long, 
  time: Double, 
  pos: Vec,
  v: Vec,
  mass: Double,
  diameter: Double,
  ejectionAngle: Double
) {

  def kineticEnergy: Double = {
    0.5 * mass*v.norm2
  }

  def volume = 0.75 * Pi * pow( diameter/2, 3 )
  def density = mass / volume
  
  def atTime( t: Double ): Bomb = move( t-time )

  def move( dt: Double ): Bomb = {
    val vz = g*dt + v.z
    val rx = v.x*dt + pos.x
    val ry = v.y*dt + pos.y
    val rz = 0.5*g*dt*dt + v.z*dt + pos.z
    val newV = Vec( v.x, v.y, vz )
    val newPos = Vec( rx, ry, rz )
    copy( time=time+dt, v=newV, pos=newPos )
  }

  def columns: Seq[String] = List(
    ID,
    pos.x, pos.y, pos.z,
    v.x, v.y, v.z,
    mass,
    diameter,
    kineticEnergy
  ).map( _.toString )  

}
