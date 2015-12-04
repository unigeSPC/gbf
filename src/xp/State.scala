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

package ch.unige.gbf.xp

trait State[I,O] { self =>
  type S
  def init: S
  def aggregate( old: S, i: I ): S
  def cleanUp( current: S ): O

  def aggregateMany( old: S, is: Seq[I] ): S = {
    def rec( cur: S, rest: Seq[I] ): S =
      if( rest.isEmpty ) cur
      else rec( aggregate( cur, rest.head ), rest.tail ) 
    rec( old, is )
  }
  
  def run( is: Stream[I] ): O = {
    cleanUp( aggregateMany( init, is ) )
  }

  def comap[J]( f: J=>I ) = new State[J,O]{
    type S = self.S
    def init = self.init
    def aggregate( old: S, j: J ): S =
      self.aggregate( old, f(j) )
    def cleanUp( s: S ) = self.cleanUp(s)
  }

}

object State {


  def toFile( fn: String ) = new State[String,Unit] {
    import java.io.PrintWriter
    type S = PrintWriter
    def init = new PrintWriter(fn)
    def aggregate( pw: PrintWriter, line: String ): PrintWriter = {
      pw.println( line )
      pw
    }
    def cleanUp( pw: PrintWriter ) = pw.close()
  }

}
