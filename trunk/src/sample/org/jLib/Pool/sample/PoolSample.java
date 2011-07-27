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

package org.jLib.Pool.sample;

import org.jLib.Pool.Pool;
import org.jLib.Pool.factory.IntegerFactory;
/**
 * This sample class show how the pool works
 * @author seb0uil
 *
 */
public class PoolSample {
	public static void main(String[] args) {
		/*
		 * We get the pool instance
		 */
		Pool<String> poolString = Pool.getInstance();
		
		Pool<Integer> poolInt = Pool.getInstance(new IntegerFactory());
		
		/*
		 * The first string is give from the pool
		 */
		 Integer intpool = null;
		try {
			intpool = poolInt.borrow();
			intpool = 1;
		} catch (Exception e) {
			System.err.println("Max. number of pool object raised\n");
		}
		poolInt.giveBack(intpool);
		
		

		/*
		 * The first string is give from the pool
		 */
		String sPool1 = null;
		try {
			sPool1 = poolString.borrow();
			sPool1 = "1";
		} catch (Exception e) {
			System.err.println("Max. number of pool object raised\n");
		}

		/*
		 * for the second string, the pool must create a new one
		 */
		String sPool2 = null;
		try {
			sPool2 = poolString.borrow();
			sPool2 = "2";
		} catch (Exception e) {
			System.err.println("Max. number of pool object raised\n");
		}

		/*
		 * We cant't get the 3rd one, we raise the max number of pool object
		 */
		String sPool3 = null;
		try {
			sPool3 = poolString.borrow();
			sPool3 = "3";
		} catch (Exception e) {
			System.err.println("Max. number of pool object raised\n");
		}		

		/*
		 * When we giveback object to the pool, there is different behaviour
		 */
		poolString.giveBack(sPool3); /* the pool test if the object is null, so it doesn't keep sPool3 */
		poolString.giveBack(sPool2); /* sPool2 is not null, it keep this one */
		poolString.giveBack(sPool1); /* The pool has just one string, so the other object are destroy */

		/*
		 * At the end, pool size is still 1, and the only String is "2",
		 * we can test it 
		 */
		try {
			int j = poolString.getNbObject();
			for (int i = 0; i< j; i++) {
				System.out.println("#" + i + " : " + poolString.borrow());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
