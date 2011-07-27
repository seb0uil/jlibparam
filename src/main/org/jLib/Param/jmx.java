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

package org.jLib.Param;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * l'annotation jmx permet de définir les valeurs qui seront exportées pour jmx
 * @author Sébastien Bettinger
 *
 */
@Target(value={ElementType.FIELD,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface jmx {
	
	boolean read() default true;
	boolean write() default true;
	boolean is() default false;
	
	
	/*
	 * commentaire
	 */
	String value() default "";
	
	/*
	 * Tableau contenant le nom des paramètres
	 */
	String[] paramName() default "";

}
