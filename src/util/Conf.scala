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

import org.streum.configrity.{Reader,Configuration}

package object conf {

  implicit class RichReader[A]( reader: Reader[A] ) {
    def comap( f: Configuration=>Configuration ) = new Reader[A] {
      def apply( conf: Configuration ): A = 
        reader( f(conf) )
    }
    def detaching( prefix: String ): Reader[A] =
      comap( _.detach( prefix ) )
  }

}
