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
package math

import scala.math.sqrt

case class Vec private( val unsafe: Array[Double] ) extends AnyVal {
  def norm2 = unsafe(0)*unsafe(0) + unsafe(1)*unsafe(1) + unsafe(2)*unsafe(2)
  def norm = sqrt( norm2 )
  def apply( i: Int ): Double = unsafe(i)
  def x = unsafe(0)
  def y = unsafe(1)
  def z = unsafe(2)
  override def toString = unsafe.mkString( "Vec(", ",", ")" )
}

object Vec {
  
  private val D = 3
  
  private def newArray = Array.ofDim[Double](D)
  
  def apply( x: Double, y: Double, z: Double ): Vec = {
    val ary = newArray
    ary(0) = x
    ary(1) = y
    ary(2) = z
    Vec( ary )
  }
  
  def apply( f: (Int)=>Double ): Vec = {
    apply( f(0), f(1), f(2) )
  }
  
  
}
