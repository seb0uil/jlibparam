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
 *  The Original Code was written by S�bastien Bettinger <seb0uil@gmail.com>
 *  
 */

package org.jLib.Param;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Properties;


/**
 * La classe Param<br/>
 * Permet de mettre � jour automatiquement les variables d'une classe par rapport � celles
 * stock�es dans un fichier poperties.
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
	 * Dans le cas ou UseClassName = true, jLibParam lit le fichier properties en pr�fixant les
	 * valeur du nom de la classe
	 * Par exemple :<br/>
	 * <li>fr.sbe.jLibParam.UseClassName=true<br/></li>
	 * ou<br/>
	 * <li>UseClassName=true<br/></li>
	 */
	private static Boolean UseClassName = false;

	private static Class clazz = null;

	/**
	 * 
	 * M�thode init.<br>
	 * R�le : Permet d'initialiser la classe pour les valeurs statiques<br/>
	 * s'emploie tel que, par exemple :<br/>
	 * <ul>
	 * <i>static {init(Annuaire.class);}</i><br/>
	 * </ul>
	 * <b><u>/!\</u> il faut placer cette d�claration en derni�re dans la classe, les variables d�clar�es avant de sont pas prise en compte par ce traitement</b>
	 *
	 * @param clazz
	 */
	public static void init(Class clazz_) {
		try {
			clazz = clazz_;
			useClassName(true);
			init();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	/**
	 * 
	 * M�thode init.<br>
	 * R�le : Permet d'initialiser la classe pour les valeurs statiques<br/>
	 * s'emploie tel que, par exemple :<br/>
	 * <ul>
	 * <i>static {init();}</i><br/>
	 * </ul>
	 * Cette m�thode tente de trouver par elle-m�me le nom de la classe � initialiser.<p/>
	 * <b><u>/!\</u> il faut placer cette d�claration en derni�re dans la classe, les variables d�clar�es avant de sont pas prise en compte par ce traitement</b>
	 *
	 */
	public static void init() {
		try {
			readPropertie();
			if (clazz==null) clazz = guessClass();
			updateParam(Class.forName(clazz.getName()), null, true);			
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	/**
	 * M�thode guessClass<br/>
	 * R�le : Devine la classe appelante la librairie
	 * @return class appelante
	 * @throws ClassNotFoundException
	 */
	public static Class guessClass() throws ClassNotFoundException {
		/*On devine la classe*/
		Throwable t = new Throwable();
		t.fillInStackTrace();
		StackTraceElement[] stack = t.getStackTrace();
		return Class.forName(stack[2].getClassName());
	}
	
	/**
	 * 
	 * M�thode init.<br>
	 * R�le : Permet d'initialiser la classe pour les valeurs statiques<br/>
	 * s'emploie tel que, par exemple :<br/>
	 * <ul>
	 * <i>static {init();}</i><br/>
	 * </ul>
	 * Cette m�thode tente de trouver par elle-m�me le nom de la classe � initialiser.<p/>
	 * <b><u>/!\</u> il faut placer cette d�claration en derni�re dans la classe, les variables d�clar�es avant de sont pas prise en compte par ce traitement</b>
	 *
	 * @param UseClazzName : Flag indiquant si les param�tres sont pr�fix�s ou non
	 *  du nom de la classe
	 */
	public static void init(Boolean UseClazzName) {
		try {
			UseClassName = UseClazzName;
			/*
			 * Si l'on sp�cifie le nom de la classe, on recup�re celui-ci
			 */
			if (UseClassName) clazz = guessClass();
			init();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	/**
	 * Fichier de propri�t�s contenant les param�tres � surcharger
	 */
	static private Properties properties = new Properties();

	/**
	 * 
	 * M�thode initParam.<br>
	 * R�le : On lui passe un tableau contenant le nom des variables a initialiser<br/>
	 * Si ces variables statiques existent dans la classe clazz, on modifie la valeur de ce variable
	 * a partir du fichier properties.
	 * Si param est null, on tente de lire toutes les variables du fichier properties
	 *
	 * @param clazz : Classe dont les valeurs sont � initialiser.
	 * @param param : tableau des param�tres � initialiser, ou null pour initialiser toutes les variables
	 * @throws Exception
	 */
	static public void initParam(String[] param) throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
		updateParam(clazz, param, false);
	}

	/**
	 * 
	 * @param allField
	 * @return
	 */
	static private String[] FieldToArray(HashMap<String, Field> allField) {
		String[] param = new String[allField.size()];
		int i=0;
		for (Field field : allField.values()) {
			param[i++] = field.getName();
		}
		return param;
	}
	
	/**
	 * 
	 * M�thode updateParam.<br>
	 * R�le : remplace les valeurs des attributs de la classe appelante par celle de la classe �tendant Param.<br/>
	 * Par d�faut, ne remplace la valeur que des param�tres ayant une valeur =null 
	 *
	 * @param o : la classe appelante (typiquement <i>this</i>)
	 * 
	 */
	static public void updateParam(Class o) {
		updateParam(o,null,false);
	}
	
	
	static public void updateParam(Object o) {
		updateParamInstance(o,null,false);
	}
	
	/**
	 * 
	 * @param o
	 * @return La classe retrouvé à partir du nom
	 */
	static private Class getClassFromName (Object o) {
		try {
			return Class.forName(o.getClass().getName());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * M�thode updateParam.<br>
	 * R�le : remplace les valeurs des attributs de la classe appelante par celle de la classe �tendant Param.<br/>
	 *
	 * @param o : la classe appelante (typiquement <i>this</i>)
	 * @param force : force le remplacement des valeurs, m�me pour les attributs non null
	 */
	static public void updateParam(Class o, boolean force) {
		updateParam(o,null,force);
	}

	
	/**
	 * 
	 * M�thode updateParam.<br>
	 * R�le : remplace les valeurs des attributs de la classe appelante par celle de la classe �tendant Param.<br/>
	 *
	 * @param o : la classe appelante (typiquement <i>this</i>)
	 * @param force : force le remplacement des valeurs, m�me pour les attributs non null
	 */
	static public void updateParam(Object o, boolean force) {
		updateParamInstance(o,null,force);
	}	
	
	/**
	 * 
	 * M�thode updateParam.<br>
	 * R�le : remplace les valeurs des attributs de la classe appelante par celle de la classe �tendant Param.<br/>
	 * Par d�faut, ne remplace la valeur que des param�tres ayant une valeur =null 
	 *
	 * @param o : la classe appelante (typiquement <i>this</i>)
	 * @param param : la liste des attributs � modifier (ou null si tous les attributs sont susceptible d'�tre modifi�s)<br/>
	 * 
	 */
	static public void updateParam(Class o, String[] param) {
		updateParam(o,param,false);
	}
	
	/**
	 * 
	 * M�thode updateParam.<br>
	 * R�le : remplace les valeurs des attributs de la classe appelante par celle de la classe �tendant Param.<br/>
	 * Par d�faut, ne remplace la valeur que des param�tres ayant une valeur =null 
	 *
	 * @param o : la classe appelante (typiquement <i>this</i>)
	 * @param param : la liste des attributs � modifier (ou null si tous les attributs sont susceptible d'�tre modifi�s)<br/>
	 * 
	 */
	static public void updateParam(Object o, String[] param) {
		Class c = getClassFromName(o);
		updateParam(c,param,false);
	}	

	/**
	 * 
	 * M�thode updateParam.<br>
	 * R�le : remplace les valeurs des attributs de la classe appelante par celle de la classe �tendant Param.<br/>
	 * Par d�faut, ne remplace la valeur que des param�tres ayant une valeur =null 
	 *
	 * @param o : la classe appelante (typiquement <i>this</i>)
	 * @param param : la liste des attributs � modifier (ou null si tous les attributs sont susceptible d'�tre modifi�s)<br/>     
	 * @param force : force le remplacement des valeurs, m�me pour les attributs non null
	 */
	static public void updateParam(Class o, String[] param, boolean force) {
		/**
		 * On charge la classe pass�e en param�tre
		 */
		try {
			String ClassName = o.getName();
			

			String ClazzName = "";
			if (UseClassName) ClazzName = ClassName + ".";

			/**
			 * Si param est nul, on prend tous les champs de l'objet
			 */
			if (param == null) {
				Field[] Fieldparam = o.getDeclaredFields();
				param = new String[Fieldparam.length];
				for (int i=0; i<Fieldparam.length; i++) {
					param[i] = Fieldparam[i].getName();
				}
			}
			for (int i=0; i<param.length; i++) {
				try {
					Field field = o.getDeclaredField(param[i]);
					field.setAccessible(true);
					Object objAnnuaire = properties.get(ClazzName + param[i]);
					if ( ((field.get(o) == null) || force) && (objAnnuaire != null) ) {
						field.set(o,  ObjectConverter.convert(objAnnuaire, field.getType()) );
					}
				}   catch (NoSuchFieldException Field_e) {
					/**
					 * Le champ passÃ© en paramÃ¨tre n'est pas prÃ©sent dans la classe
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
	 * M�thode updateParam.<br>
	 * R�le : remplace les valeurs des attributs de la classe appelante par celle de la classe �tendant Param.<br/>
	 * Par d�faut, ne remplace la valeur que des param�tres ayant une valeur =null 
	 *
	 * @param o : la classe appelante (typiquement <i>this</i>)
	 * @param param : la liste des attributs � modifier (ou null si tous les attributs sont susceptible d'�tre modifi�s)<br/>     
	 * @param force : force le remplacement des valeurs, m�me pour les attributs non null
	 */
	static public void updateParamInstance(Object o, String[] param, boolean force) {
		/**
		 * On charge la classe pass�e en param�tre
		 */
		try {
			String ClassName = o.getClass().getName();
			Class c = Class.forName(ClassName);
			

			String ClazzName = "";
			if (UseClassName) ClazzName = ClassName + ".";

			HashMap<String, Field> allField = getAllFieldsRec(c);			
			/**
			 * Si param est nul, on prend tous les champs de l'objet
			 */
			if (param == null) {
				param = FieldToArray(allField);
			}
			
			for (int i=0; i<param.length; i++) {
				try {
					Field field = c.getDeclaredField(param[i]);
					field.setAccessible(true);
					Object objAnnuaire = properties.get(ClazzName + param[i]);
					if ( ((field.get(o) == null) || force) && (objAnnuaire != null) ) {
						field.set(o,  ObjectConverter.convert(objAnnuaire, field.getType()) );
					}
				}   catch (NoSuchFieldException Field_e) {
					/**
					 * Le champ passÃ© en paramÃ¨tre n'est pas prÃ©sent dans la classe
					 * On laisse passer l'erreur sans en tenir compte
					 */
				}
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	

	/**
	 * M�thode setParam<br/>
	 * R�le : Met a jour dynamiquement la valeur d'une variable de la classe
	 * @param Name
	 * @param Value
	 * @throws Exception
	 */
	public static void setParam(String Name, String Value) throws Exception {
		Field field = clazz.getField(Name); 
		field.set(clazz, ObjectConverter.convert(Value, field.getType()));
	}

	
	/**
	 * Retourne tous les champs de la classe pass�e en param�tre (y compris ceux de la classe m�re)
	 * @param clazz Nom de la classe
	 * @return une map contenant tous les champs de la classe 
	 */
	public static  HashMap<String, Field> getAllFieldsRec(Class clazz) {
		HashMap<String, Field> hFields = new HashMap<String, Field>();
		Class superClazz = clazz.getSuperclass();
	    if(superClazz != null){
	        HashMap<String, Field> hSuper = getAllFieldsRec(superClazz);
	        hFields.putAll(hSuper);
	    }
	    
	    Field[] fields = clazz.getDeclaredFields();
	    for (Field field : fields) {
	    	hFields.put(field.getName(), field);
	    }
	    return hFields;
	}
	
	/**
	 * 
	 * M�thode setPropertieFile.<br>
	 * R�le : Force le fichier propertie utilis� pour lire les valeurs par d�faut
	 *
	 * @param newPropertieFile : Fichier propertie contenant les couples <i>Param�tre=Valeur</i> � utiliser
	 * @throws Exception 
	 */
	public static void setPropertieFile(String newPropertieFile) throws Exception {
		/*
		 * On sauvegarde l'objet properties afin de pouvoir le remettre dans le cas ou une
		 * exception est lev� par la lecture du fichier.
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
	 * M�thode readPropertie.<br>
	 * R�le : permet de charger le fichier properties <i>config.properties</i> situ� dans le
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