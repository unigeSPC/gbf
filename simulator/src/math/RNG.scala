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

import ec.util.{MersenneTwisterFast => MT}
import java.lang.ThreadLocal
import scala.math.{exp,log,sqrt,E,pow}


class RNG( val seed: Int ) {

  private lazy val seeder = new MT(seed)

  private def nextSeed: Int = seeder.synchronized {
    seeder.nextInt
  }

  private val rng = new ThreadLocal[MT] {
    override protected def initialValue() = new MT( nextSeed )
  }

  def nextDouble: Double = rng.get.nextDouble

  def nextDouble( max: Double ): Double = 
    rng.get.nextDouble*max

  def nextGaussian( avg: Double = 0.0, std: Double = 1.0): Double =
    rng.get.nextGaussian*std + avg

  def nextLogNormal( avg: Double, std: Double ): Double =
    exp( avg + std * nextGaussian() )

  //Adapted from: https://github.com/jliszka/probability-monad/blob/master/src/main/scala/probability-monad/Distribution.scala

  def nextGamma( k: Double, theta: Double): Double = {
    val n = k.toInt
    val gammaInt = Array.fill( n )( -log( nextDouble ) ).sum //TODO: Optimize
    val gammaFrac = {
      val delta = k - n
      def helper(): Double = {
        val u1 = nextDouble
        val u2 = nextDouble
        val u3 = nextDouble
        val (zeta, eta) = {
          val v0 = E / (E + delta)
          if (u1 <= v0) {
            val zeta = pow(u2, 1/delta)
            val eta = u3 * pow(zeta, delta - 1)
            (zeta, eta)
          } else {
            val zeta = 1 - log(u2)
            val eta = u3 * exp(-zeta)
            (zeta, eta)
          }
        }
        if (eta > pow(zeta, delta - 1) * exp(-zeta))
          helper()
        else
          zeta
      }
      helper()
    }
    (gammaInt + gammaFrac) * theta
  }

  def nextBeta(a: Double, b: Double): Double = {
    val x = nextGamma(a, 1)
    val y = nextGamma(b, 1)
    x / (x + y)
  }

  def nextBeta(a: Double, b: Double, min: Double, max: Double): Double = {
    val x = nextBeta( a, b )
    x*(max-min) + min
  }

}

object RNG {

  def apply() = {
    val seed = (System.currentTimeMillis() >> 7 ).toInt
    new RNG( seed )
  }
  
  def apply( seed: Int ) = new RNG( seed )

  import org.streum.configrity._
  lazy val reader = read[Int]( "seed", -1 ) map { s =>
    if( s == -1 ) apply() else apply(s)
  }


}
