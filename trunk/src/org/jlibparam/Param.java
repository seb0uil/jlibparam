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
  *  
  */

package org.jlibparam;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * La classe Param
 *
 * @author  Sebastien_Bettinger
 */
public class Param {
    
    /**
     * 
     * Méthode init.<br>
     * Rôle : Permet d'initialiser la classe pour les valeurs statique<br/>
     * s'emploie tel que, par exemple :<br/>
     * <ul>
     * <i>@SuppressWarnings("unused")<br/>
     * private static Object init = Annuaire.init(Annuaire.class);</i><br/>
     * </ul>
     * <b><u>/!\</u> il faut placer cette déclaration en dernière dans la classe, les variables déclarées avant de sont pas prise en compte par ce traitement</b>
     *
     * @param clazz
     * @return
     */
    public static Object init(Class clazz) {
        try {
            readPropertie();
            initParam(clazz,null);
        } catch (Exception e) {
            e.printStackTrace();
        } 
        return null;
    }

    static private Class classe = null;
    /**
     * Fichier de propriétés contenant les paramètres à surcharger
     */
    static private Properties properties = new Properties();

    /**
     * 
     * Méthode initParam.<br>
     * Rôle : On lui passe un tableau contenant le nom des variables a initialiser<br/>
     * Si ces variables existent dans la classe Annuaire, on modifie la valeur de ce variable
     * a partir du fichier properties.
     * Si param est null, on tente de lire toutes les variables du fichier properties
     *
     * @param o
     * @param param
     * @throws Exception
     */
    static public void initParam(Class clazz, String[] param) throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
        /**
         * On precise la classe appelante
         */
        classe = clazz;
        
        /**
         * On charge la classe passée en paramètre
         */
        try {
            String ClassName = clazz.getName();
            Class c = Class.forName(ClassName);

            /**
             * Si param est nul, on prend tous les champs de l'objet
             */
            if (param == null) {
                Field[] Fieldparam = c.getDeclaredFields();
                param = new String[Fieldparam.length];
                for (int i=0; i<Fieldparam.length; i++) {
                    param[i] = Fieldparam[i].getName();
                }
            }

            for (int i=0; i<param.length; i++) {
                try {                
                    Field field = c.getDeclaredField(param[i]);
                    field.setAccessible(true);
                    if (properties.containsKey(field.getName())) {
                        String sProp = properties.getProperty(field.getName());
                        field.set(clazz,  ObjectConverter.convert(sProp, field.getType()) );
                    }
                }
                catch (NoSuchFieldException Field_e) {
                    Field_e.printStackTrace();
                    /**
                     * Le champ passé en paramètre n'est pas présent dans la classe
                     * On laisse passer l'erreur sans en tenir compte
                     */
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * Méthode updateParam.<br>
     * Rôle : remplace les valeurs des attributs de la classe appelante par celle de la classe étendant Param.<br/>
     * Par défaut, ne remplace la valeur que des paramètres ayant une valeur =null 
     *
     * @param o : la classe appelante (typiquement <i>this</i>)
     * 
     */
    static public void updateParam(Object o) {
        updateParam(o,null,false);
    }
    
    /**
     * 
     * Méthode updateParam.<br>
     * Rôle : remplace les valeurs des attributs de la classe appelante par celle de la classe étendant Param.<br/>
     *
     * @param o : la classe appelante (typiquement <i>this</i>)
     * @param force : force le remplacement des valeurs, même pour les attributs non null
     */
    static public void updateParam(Object o, boolean force) {
        updateParam(o,null,force);
    }
    
    /**
     * 
     * Méthode updateParam.<br>
     * Rôle : remplace les valeurs des attributs de la classe appelante par celle de la classe étendant Param.<br/>
     * Par défaut, ne remplace la valeur que des paramètres ayant une valeur =null 
     *
     * @param o : la classe appelante (typiquement <i>this</i>)
     * @param param : la liste des attributs à modifier (ou null si tous les attributs sont susceptible d'être modifiés)<br/>
     * 
     */
    static public void updateParam(Object o, String[] param) {
        updateParam(o,param,false);
    }
    
    /**
     * 
     * Méthode updateParam.<br>
     * Rôle : remplace les valeurs des attributs de la classe appelante par celle de la classe étendant Param.<br/>
     * Par défaut, ne remplace la valeur que des paramètres ayant une valeur =null 
     *
     * @param o : la classe appelante (typiquement <i>this</i>)
     * @param param : la liste des attributs à modifier (ou null si tous les attributs sont susceptible d'être modifies)<br/>     
     * @param force : force le remplacement des valeurs, même pour les attributs non null
     */
    static public void updateParam(Object o, String[] param, boolean force) {
        /**
         * On charge la classe passée en paramètre
         */
        try {
            String ClassName = o.getClass().getName();
            Class c = Class.forName(ClassName);
            /**
             * Si param est nul, on prend tous les champs de l'objet
             */
            if (param == null) {
                Field[] Fieldparam = c.getDeclaredFields();
                param = new String[Fieldparam.length];
                for (int i=0; i<Fieldparam.length; i++) {
                    param[i] = Fieldparam[i].getName();
                }
            }
            for (int i=0; i<param.length; i++) {
                try {
                    Field field = c.getDeclaredField(param[i]);
                    field.setAccessible(true);
                    Object objAnnuaire = classe.getDeclaredField(param[i]).get(null);
                    if ((field.get(o) == null) || force)
                        field.set(o,  objAnnuaire );
                }   catch (NoSuchFieldException Field_e) {
                    /**
                     * Le champ passé en paramètre n'est pas présent dans la classe
                     * On laisse passer l'erreur sans en tenir compte
                     */
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * Méthode readPropertie.<br>
     * Rôle : permet de charger le fichier properties <i>config.properties</i> situé dans le
     * classpath.
     * Ce fichier contient les couples <i>attribut=valeur</i> a modifier dans la classe.
     *
     */
    private static void readPropertie() {
        InputStream is = Param.class.getResourceAsStream("/config.properties");
        try{
            properties.load(is);

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally{
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
