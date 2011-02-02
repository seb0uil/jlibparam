 /*
  *
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU Lesser General Public License as published by the Free Software Foundation, either version 3
  * of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License along with this library.
  * If not, see <http://www.gnu.org/licenses/>.
  * 
  *  The Original Code was written by Sébastien Bettinger <seb0uil@gmail.com>
  *  for jLibParamLibrary
  */

package sample;

import org.jlibparam.Param;

public class Config extends Param {
		
    public static String STRING  = "initial value";
    
    public static Integer INTEGER = 1;
    
    public static Boolean BOOLEAN = true;
    
	private static String SAMPLE_PRIVATE_STRING = "private string from sample";
	public static String SAMPLE_PUBLIC_STRING  = "public string from sample";
	public static String NULL_STRING = null;
	
    /**
     * Valeur ne servant qu'a initialiser la classe pour ses valeurs statiques
     * Celle-ci étendant param, la méthode init() permet de modifier les valeurs définies
     * ci-dessus par celle définie dans le fichier properties
     */
    static {init();}
}
