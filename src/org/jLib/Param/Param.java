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

package org.jLib.Param;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Properties;


/**
 * La classe Param<br/>
 * Permet de mettre à jour automatiquement les variables d'une classe par rapport à celles
 * stockées dans un fichier poperties.
 *
 * @author  Sebastien_Bettinger
 */
public class Param {
    /**
     * Fichier propertie contenant les valeurs a surcharger
     */
    private static String Propertie = "/config.properties";

    /**
     * Flag pour l'utilisation ou non du nom complet de la classe
     * dans le fichier properties.<br/>
     * Dans le cas ou UseClassName = true, jLibParam lit le fichier properties en préfixant les
     * valeur du nom de la classe
     * Par exemple :<br/>
     * <li>fr.sbe.jLibParam.UseClassName=true<br/></li>
     * ou<br/>
     * <li>UseClassName=true<br/></li>
     */
    private static Boolean UseClassName = false;

    /**
     * 
     * Méthode init.<br>
     * Rôle : Permet d'initialiser la classe pour les valeurs statiques<br/>
     * s'emploie tel que, par exemple :<br/>
     * <ul>
     * <i>static {init(Annuaire.class);}</i><br/>
     * </ul>
     * <b><u>/!\</u> il faut placer cette déclaration en dernière dans la classe, les variables déclarées avant de sont pas prise en compte par ce traitement</b>
     *
     * @param clazz
     */
    public static void init(Class clazz) {
        try {
            readPropertie();
            initParam(clazz,null);
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }

    /**
     * 
     * Méthode init.<br>
     * Rôle : Permet d'initialiser la classe pour les valeurs statiques<br/>
     * s'emploie tel que, par exemple :<br/>
     * <ul>
     * <i>static {init();}</i><br/>
     * </ul>
     * Cette méthode tente de trouver par elle-même le nom de la classe à initialiser.<p/>
     * <b><u>/!\</u> il faut placer cette déclaration en dernière dans la classe, les variables déclarées avant de sont pas prise en compte par ce traitement</b>
     *
     */
    public static void init() {
        try {
            /*On devine la classe*/
            Throwable t = new Throwable();
            t.fillInStackTrace();
            StackTraceElement[] stack = t.getStackTrace();
            init( Class.forName(stack[1].getClassName()) );
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }

    public static void init(Boolean UseClazzName) {
        try {
        	UseClassName = UseClazzName;
            /*On devine la classe*/
            Throwable t = new Throwable();
            t.fillInStackTrace();
            StackTraceElement[] stack = t.getStackTrace();
            init( Class.forName(stack[1].getClassName()) );
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
    
    /**
     * Fichier de propriétés contenant les paramètres à surcharger
     */
    static private Properties properties = new Properties();

    /**
     * 
     * Méthode initParam.<br>
     * Rôle : On lui passe un tableau contenant le nom des variables a initialiser<br/>
     * Si ces variables statiques existent dans la classe clazz, on modifie la valeur de ce variable
     * a partir du fichier properties.
     * Si param est null, on tente de lire toutes les variables du fichier properties
     *
     * @param clazz : Classe dont les valeurs sont à initialiser.
     * @param param : tableau des paramètres à initialiser, ou null pour initialiser toutes les variables
     * @throws Exception
     */
    static public void initParam(Class clazz, String[] param) throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
        /**
         * On charge la classe passée en paramètre
         */
        try {
            String ClassName = clazz.getName();
            Class c = Class.forName(ClassName);

            String ClazzName = "";
            if (UseClassName) ClazzName = ClassName + ".";

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
                    /* Dans l'initialisation, on ne s'intéresse qu'aux champs statiques */
                    if (Modifier.isStatic(field.getModifiers())) {
                        field.setAccessible(true);
                        if (properties.containsKey(ClazzName + field.getName())) {
                            String sProp = properties.getProperty(ClazzName + field.getName());
                            field.set(clazz,  ObjectConverter.convert(sProp, field.getType()) );
                        }
                    }
                }
                catch (NoSuchFieldException Field_e) {
                    Field_e.printStackTrace();
                    /*
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
     * @param param : la liste des attributs à modifier (ou null si tous les attributs sont susceptible d'être modifiés)<br/>     
     * @param force : force le remplacement des valeurs, même pour les attributs non null
     */
    static public void updateParam(Object o, String[] param, boolean force) {
        /**
         * On charge la classe passée en paramètre
         */
        try {
            String ClassName = o.getClass().getName();
            Class c = Class.forName(ClassName);

            String ClazzName = "";
            if (UseClassName) ClazzName = ClassName + ".";

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
                    //                    Object objAnnuaire = classe.getDeclaredField(param[i]).get(null);
                    Object objAnnuaire = properties.get(ClazzName + param[i]);
                    if ( ((field.get(o) == null) || force) && (objAnnuaire != null) ) {
                        field.set(o,  ObjectConverter.convert(objAnnuaire, field.getType()) );
                    }
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
     * Méthode setPropertieFile.<br>
     * Rôle : Force le fichier propertie utilisé pour lire les valeurs par défaut
     *
     * @param newPropertieFile : Fichier propertie contenant les couples <i>Paramètre=Valeur</i> à utiliser
     * @throws Exception 
     */
    public static void setPropertieFile(String newPropertieFile) throws Exception {
        /*
         * On sauvegarde l'objet properties afin de pouvoir le remettre dans le cas ou une
         * exception est levé par la lecture du fichier.
         */
        Propertie = newPropertieFile;
        Properties properties_old = (Properties) properties.clone();
        properties = new Properties();
        try {
            readPropertie();
        } catch (Exception e) {
            properties = properties_old;
            throw new Exception("Fichier inexistant");
        }
    }

    /**
     * 
     * Méthode readPropertie.<br>
     * Rôle : permet de charger le fichier properties <i>config.properties</i> situé dans le
     * classpath.
     * Ce fichier contient les couples <i>attribut=valeur</i> a modifier dans la classe.
     * @throws Exception 
     *
     */
    private static void readPropertie() throws Exception {
        InputStream is = Param.class.getResourceAsStream(Propertie);
        if (is == null) {throw new Exception("Fichier inexistant");}
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

    public static void useClassName(boolean BooleanValue) {
        UseClassName = BooleanValue;
    }
}