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


public class Sample {
	
	private String SAMPLE_PRIVATE_STRING = "private string from sample";
	public String  SAMPLE_PUBLIC_STRING = "public string from sample";
	public String NULL_STRING = null;
	
    /**
     * We can define an array to specify which value are override by
     * property file
     */
	private String[] param = new String[] {
            "SAMPLE_PRIVATE_STRING"
    }; 
    
	
    public static void main(String[] args) throws Exception {
        Sample t = new Sample();
        /**
         * On utilise la classe Config (extend Param) pour mettre a jour automatiquement
         * les variables présente dans la classe t
         */
        System.out.println("Initial value from Config class are updated by propertie file : ");
        System.out.println("Config.STRING :" + Config.STRING);
        System.out.println("Config.INTEGER :" + Config.INTEGER);
        System.out.println("Config.BOOLEAN :" + Config.BOOLEAN);
        
        /**
         * On met a jour les variables locales a partir de celle de la classe config
         * dans un premier temps, uniquement pour les variables nulles
         */
        Config.updateParam(t);
        System.out.println("Update just null value from this class : ");
        System.out.println("Sample.SAMPLE_PRIVATE_STRING :" + t.SAMPLE_PRIVATE_STRING);
        System.out.println("Sample.SAMPLE_PUBLIC_STRING :" + t.SAMPLE_PUBLIC_STRING);
        System.out.println("Sample.NULL_STRING :" + t.NULL_STRING);

        /**
         * Dans un second temps, on met également à jour les variables
         * non nulles
         */
        Config.updateParam(t,true);
        System.out.println("We can erase non null value : ");
        System.out.println("Sample.SAMPLE_PRIVATE_STRING :" + t.SAMPLE_PRIVATE_STRING);
        System.out.println("Sample.SAMPLE_PUBLIC_STRING :" + t.SAMPLE_PUBLIC_STRING);
        System.out.println("Sample.NULL_STRING :" + t.NULL_STRING);
        
        /**
         * On peut specifier le nom complet de la classe dans le
         * fichier propriété
         */
        Config.useClassName(true);
        Config.updateParam(t,true);
        System.out.println("We can specify package in property file : ");
        System.out.println("Sample.SAMPLE_PRIVATE_STRING :" + t.SAMPLE_PRIVATE_STRING);
        System.out.println("Sample.SAMPLE_PUBLIC_STRING :" + t.SAMPLE_PUBLIC_STRING);
        System.out.println("Sample.NULL_STRING :" + t.NULL_STRING);
    }
}
