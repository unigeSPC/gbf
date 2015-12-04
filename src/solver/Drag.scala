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

case class EjectDrag( dc: EjectDragConfig ) extends RK4.F {
  import constants._
  import dc._

  val N = 6

  def temp( z: Double ) = temp0 + thermalLapse * z

  def pressure( T: Double ): Double = {
    pressure0 * pow( T/temp0, -g / (R*thermalLapse) )
  }

  def rhoAir( p: Double, T: Double ) = p / (R*T)
    
  def viscAir( T: Double ) = 
    0.0000172 * 390 / (T+117) * pow( T/273, 1.5 )

  private def reynolds( rhoA: Double, v: Double, diam: Double, viscA: Double ) =
    rhoA*v*diam / viscA

  private def soundSpeed( T: Double ) = 
    sqrt( specHeatRatio*R*T )

  private def mach( v: Double, c: Double ) = v/c

  private def reducedDrag( Cd: Double, r: Double ):Double = {
    val rd = reducedDragRadius
    if( r < rd ) Cd * pow( r/rd, 2 ) else Cd
  }

  private def dragCoef( re: Double ): Double = 
    if( re < 2e5 ) 0.5 else 0.1

  private def distFromVent( y: SafeArray[Double] ) = {
    val dx = y(3) - vent(0)
    val dy = y(4) - vent(1)
    val dz = y(5) - vent(2)
    sqrt( dx*dx + dy*dy + dz*dz )
  }

  private def speed( y: SafeArray[Double] ) =
    sqrt(y(0)*y(0) + y(1)*y(1) + y(2)*y(2)) 

  lazy val area = 0.25 * Pi * diameter*diameter

  def compute( t: Double, yDot: Array[Double], y: SafeArray[Double]): Unit = {
    val vw = safe{ Array( y(0)-dc.wind(0), y(1)-dc.wind(1), y(2) ) }
    val v = speed(vw)
    val T = temp( y(5) )
    val rhoA = rhoAir( pressure(T), T )
    val re = reynolds( rhoA, v, diameter, viscAir(T) )
    val machNum = mach( v, soundSpeed(T) )
    //println( s"RE: $re  MACH: $machNum" )    
    //require( machNum <= 1.0, s"High mach number (v/c=$machNum)" )
    val Cd = reducedDrag( dragCoef(re), distFromVent(y) )
    //println( s"CD: $Cd" )
    val vk = -v*rhoA*area*Cd / (2*mass)
    yDot(0) = vk*vw(0)
    yDot(1) = vk*vw(1) 
    yDot(2) = vk*y(2) - g //*(density-rhoA)/density
    yDot(3) = y(0)
    yDot(4) = y(1)
    yDot(5) = y(2)
  }
}


//TODO: Unify
case class EjectDragNoG( dc: EjectDragConfig ) extends RK4.F {
  import constants._
  import dc._

  val N = 6

  def temp( z: Double ) = temp0 + thermalLapse * z

  def pressure( T: Double ): Double = {
    pressure0 * pow( T/temp0, -g / (R*thermalLapse) )
  }

  def rhoAir( p: Double, T: Double ) = p / (R*T)
    
  def viscAir( T: Double ) = 
    0.0000172 * 390 / (T+117) * pow( T/273, 1.5 )

  private def reynolds( rhoA: Double, v: Double, diam: Double, viscA: Double ) =
    rhoA*v*diam / viscA

  private def soundSpeed( T: Double ) = 
    sqrt( specHeatRatio*R*T )

  private def mach( v: Double, c: Double ) = v/c

  private def reducedDrag( Cd: Double, r: Double ):Double = {
    val rd = reducedDragRadius
    if( r < rd ) Cd * pow( r/rd, 2 ) else Cd
  }

  private def dragCoef( re: Double ): Double = 
    if( re < 2e5 ) 0.5 else 0.1

  private def distFromVent( y: SafeArray[Double] ) = {
    val dx = y(3) - vent(0)
    val dy = y(4) - vent(1)
    val dz = y(5) - vent(2)
    sqrt( dx*dx + dy*dy + dz*dz )
  }

  private def speed( y: SafeArray[Double] ) =
    sqrt(y(0)*y(0) + y(1)*y(1) + y(2)*y(2)) 

  lazy val area = 0.25 * Pi * diameter*diameter

  def compute( t: Double, yDot: Array[Double], y: SafeArray[Double]): Unit = {
    val vw = safe{ Array( y(0)-dc.wind(0), y(1)-dc.wind(1), y(2) ) }
    val v = speed(vw)
    val T = temp( y(5) )
    val rhoA = rhoAir( pressure(T), T )
    val re = reynolds( rhoA, v, diameter, viscAir(T) )
    //val re = reynolds( rhoA, speed(y), diameter, viscAir(T) )
    val machNum = mach( v, soundSpeed(T) )
    //println( s"RE: $re  MACH: $machNum" )    
    //require( machNum <= 1.0, s"High mach number (v/c=$machNum)" )
    val Cd = reducedDrag( dragCoef(re), distFromVent(y) )
    //println( s"CD: $Cd" )
    val vk = -v*rhoA*area*Cd / (2*mass)
    yDot(0) = vk*vw(0)
    yDot(1) = vk*vw(1) 
    yDot(2) = vk*y(2) // - g*(density-rhoA)/density
    yDot(3) = y(0)
    yDot(4) = y(1)
    yDot(5) = y(2)
  }
}

//TODO: unify
case class EjectDragConstant( dc: EjectDragConfig ) extends RK4.F {
  import constants._
  import dc._

  println("EJECT DRAG: wind: " + dc.wind.unsafe.mkString(",") )

  val N = 6

  def rhoAir( p: Double, T: Double ) = p / (R*T)
    
  def viscAir( T: Double ) = 
    0.0000172 * 390 / (T+117) * pow( T/273, 1.5 )

  private def reynolds( rhoA: Double, v: Double, diam: Double, viscA: Double ) =
    rhoA*v*diam / viscA

  private def soundSpeed( T: Double ) = 
    sqrt( specHeatRatio*R*T )

  private def mach( v: Double, c: Double ) = v/c

  private def reducedDrag( Cd: Double, r: Double ):Double = {
    val rd = reducedDragRadius
    if( r < rd ) Cd * pow( r/rd, 2 ) else Cd
  }

  private def dragCoef( re: Double ): Double = 
    if( re < 2e5 ) 0.5 else 0.1

  private def distFromVent( y: SafeArray[Double] ) = {
    val dx = y(3) - vent(0)
    val dy = y(4) - vent(1)
    val dz = y(5) - vent(2)
    sqrt( dx*dx + dy*dy + dz*dz )
  }

  private def speed( y: SafeArray[Double] ) =
    sqrt(y(0)*y(0) + y(1)*y(1) + y(2)*y(2)) 

  lazy val area = 0.25 * Pi * diameter*diameter

  def compute( t: Double, yDot: Array[Double], y: SafeArray[Double]): Unit = {
    val vw = safe{ Array( y(0)-dc.wind(0), y(1)-dc.wind(1), y(2) ) }
    val v = speed(vw)
    val T = temp0
    val p = pressure0
    val rhoA = rhoAir( p, T )
    val re = reynolds( rhoA, v, diameter, viscAir(T) )
    val machNum = mach( v, soundSpeed(T) )
    //require( machNum <= 1.0, s"High mach number (v/c=$machNum)" )
    val Cd = reducedDrag( dragCoef(re), distFromVent(y) )
    val vk = -v*rhoA*area*Cd / (2*mass)
    yDot(0) = vk*vw(0)
    yDot(1) = vk*vw(1) 
    yDot(2) = vk*y(2) - g //*(density-rhoA)/density
    yDot(3) = y(0)
    yDot(4) = y(1)
    yDot(5) = y(2)
  }
}
