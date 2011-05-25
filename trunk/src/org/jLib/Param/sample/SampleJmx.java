package org.jLib.Param.sample;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
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

import javax.management.ObjectName;


/**
 * Cette classe illustre l'utilisation de jmx avec jLibParam
 * @author Sébastien Bettinger
 *
 */
public class SampleJmx {

	public static void main(String[] args) {
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			ObjectName name = new ObjectName("jlibparam.jmx:type=test");
			Config config = new Config();
			mbs.registerMBean(config, name);

			System.out.println("Lancement ...");
			while (true) {
				Thread.sleep(2000);
				System.out.println(Config.INTEGER);
			}
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (InstanceAlreadyExistsException e) {
			e.printStackTrace();
		} catch (MBeanRegistrationException e) {
			e.printStackTrace();
		} catch (NotCompliantMBeanException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
		}
	}
}
