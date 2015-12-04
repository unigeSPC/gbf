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

package ch.unige.gbf.util


import scala.reflect.ClassTag
import scala.{specialized => spec}


object arrays {

  
  class SafeArray[@spec(Int,Double) T]( val unsafe: Array[T] ) {
    def length = unsafe.length
    def size = unsafe.size
    def apply( i: Int ): T = unsafe(i)
  }


  def arrayCopy[@spec(Int,Double) T:ClassTag]( ts: Array[T] ): Array[T] = {
    val ary = Array.ofDim[T]( ts.size )
    Array.copy( ts, 0, ary, 0, ts.size )
    ary
  }

  def safe[@spec(Int,Double) T]( ary: Array[T] ): SafeArray[T] = new SafeArray(ary)
  
  def safeCopy[@spec(Int,Double) T:ClassTag]( ts: Array[T] ): SafeArray[T] = 
    safe( arrayCopy(ts) )

}

