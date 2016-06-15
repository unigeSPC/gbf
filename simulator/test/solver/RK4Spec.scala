package ch.unige.gbf
package solver

import org.scalatest.prop.PropertyChecks
import org.scalatest.FreeSpec
import org.scalatest._

import util.arrays._


class RK4Spec extends FreeSpec with Matchers with PropertyChecks {

   lazy val NoForce = new RK4.F {
     val N = 6
     def compute( 
       t: Double, yDot: Array[Double], y: SafeArray[Double] 
     ): Unit  = {
       yDot(0) = 0.0
       yDot(1) = 0.0
       yDot(2) = 0.0
       yDot(3) = y(0)
       yDot(4) = y(1)
       yDot(5) = y(2)
     }
   }

  def WithForce( fx: Double, fy: Double, fz: Double ) = new RK4.F {
     val N = 6
     def compute( 
       t: Double, yDot: Array[Double], y: SafeArray[Double] 
     ): Unit  = {
       yDot(0) = fx
       yDot(1) = fy
       yDot(2) = fz
       yDot(3) = y(0)
       yDot(4) = y(1)
       yDot(5) = y(2)
     }
   }


  val h = 0.01

  "With no force" - {
    
    val NoForceRK4 = RK4( NoForce, h )
    
    "if v=0 then doesn't move" in {
      val init = safe( Array( 0.0, 0.0, 0.0, 1.0, 2.0, 3.0 ) )
      val (_,res) = NoForceRK4.run( t0=0, y0=init, monitor=MaxTime(100) )
      init.unsafe should be (res)
    }

    "if v!=0 then it moves linearly" in {
      val init = safe( Array( -1.0, 0.0, 2.0, 1.0, 2.0, 3.0 ) )
      val t = 100
      val (_,res) = NoForceRK4.run( t0=0, y0=init, monitor=MaxTime(t) )
      res(0) should be (init(0))
      res(1) should be (init(1))
      res(2) should be (init(2))
      res(3) should be ( (init(3) + init(0)*t) +- 1e-8)
      res(4) should be ( (init(4) + init(1)*t) +- 1e-8)
      res(5) should be ( (init(5) + init(2)*t) +- 1e-8)
    }

  }

  "With constant force" - {

    val fx = 1.0
    val fy = 0.0
    val fz = -1.0
    val WithForceRK4 = RK4( WithForce(fx,fy,fz), h )

    val eps = 1e-8

    def linear( x: Double, xD: Double, t: Double ) = 
      (x + xD*t) +- eps
    def quadratic( x: Double, xD: Double, xDD: Double, t: Double ) = 
        ( x + xD*t + xDD*t*t/2 ) +- eps

    "if v!=0 then it moves linearly" in {
      val init = safe( Array( -1.0, 0.0, 2.0, 1.0, 2.0, 3.0 ) )
      val t = 100
      val (_,res) = WithForceRK4.run( t0=0, y0=init, monitor=MaxTime(t) )
      res(0) should be (linear(init(0), fx, t))
      res(1) should be (linear(init(1), fy, t))
      res(2) should be (linear(init(2), fz, t))
      res(3) should be (quadratic(init(3), init(0), fx, t))
      res(4) should be (quadratic(init(4), init(1), fy, t))
      res(5) should be (quadratic(init(5), init(2), fz, t))
    }
  }
    


}
