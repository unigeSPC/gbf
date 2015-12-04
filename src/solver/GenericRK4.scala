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

object RK4 {

  trait F {
    def N: Int
    def fresh = Array.ofDim[Double](N)
    def apply( t: Double, y: SafeArray[Double] ): Array[Double] = {
      val yDot = fresh
      compute( t, yDot, y )
      yDot
    }

    def apply( t: Double, y: SafeArray[Double], z: Array[Double] ): Unit =
      compute( t, z, y )

    def compute( t: Double, yDot: Array[Double], y: SafeArray[Double] ): Unit
  }

  def JustG( g: Double ) = new F {
    val N = 6
    def compute( t: Double, yDot: Array[Double], y: SafeArray[Double] ): Unit  = {
      yDot(0) = 0.0
      yDot(1) = 0.0
      yDot(2) = -g
      yDot(3) = y(0)
      yDot(4) = y(1)
      yDot(5) = y(2)
    }
  }


}

case class RK4( f: RK4.F, h: Double ) {

  private type Ary = Array[Double]
  private type Safe = SafeArray[Double]

  private def accum( x: Safe, k: Double, y: Ary ): Unit = {
    var i = 0
    while( i < f.N ) {
      y(i) += x(i)*k
      i+=1
    }
  }
  private def update( x: Safe, k: Double, y: Safe, z: Ary ): Unit = {
    var i = 0
    while( i < f.N ) {
      z(i) = x(i) + k*y(i)
      i+=1
    }
  }

  class Stepper {

    private val A = f.fresh
    private val B = f.fresh
    private val C = f.fresh


    def step( t: Double, y: Array[Double] ): Unit = {
      f(t,safe(y),C)                       // C = k1 = f(t,y)
      update( safe(y), h/2, safe(C), B )   // B = y + h/2*C = y+h/2*k1
      f( t, safe(B), A )                   // A = k2 = f(t,B)
      accum( safe(A), 2, C )               // C := C + 2*A = k1 + 2*k2
      update( safe(y), h/2, safe(A), B )   // B = y + h/2*A = y+h/2*k2
      f( t, safe(B), A )                   // A = k3 = f(t,B)   
      accum( safe(A), 2, C )               // C := C + 2*A = k1 + 2*k2 + 2*k3 
      update( safe(y), h, safe(A), B )     // B = y + h*A = y+h*k3
      f( t, safe(B), A )                   // A = k4 = f(t,B)
      accum( safe(A), 1.0, C )             // C := C + A = k1 + 2*k2 + 2*k3 + k4
      accum( safe(C), h/6, y )             // y := y + h/6*C
    }
  }

  def run( t0: Double, y0: Safe, monitor: StepMonitor ): (Double,Ary) = {
    var t = t0
    var y = f.fresh
    accum( y0, 1, y )
    val stepper = new Stepper
    do {
      //println( s"$t :: ${y.mkString("[",",","]")}" )
      stepper.step( t, y )
      t += h
    } while( monitor(t,safe(y)) )
    //println
    //println("_------------------________-----___----_---_---__" )
    (t,y)
  }
}
