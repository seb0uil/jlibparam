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
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;


/**
 * La classe Param<br/>
 * Permet de mettre à jour automatiquement les variables d'une classe par rapport à celles
 * stockées dans un fichier poperties.
 *
 * @author  Sebastien_Bettinger
 */
public class Param implements DynamicMBean {
	/**
	 * Fichier de propriétés contenant les paramètres à surcharger
	 */
	static private Properties properties = new Properties();

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

	/**
	 * Classe appelante la librairie
	 */
	private static Class clazz = null;

	/**
	 * 
	 * Méthode init.<br>
	 * Rôle : Permet d'initialiser la classe pour les valeurs statiques<br/>
	 * s'emploie tel que, par exemple :<br/>
	 * <ul>
	 * <i>static {init(Annuaire.class);}</i><br/>
	 * </ul>
	 * <b><u>/!\</u> il faut placer cette déclaration en dernière dans la classe, les variables déclarées après ne sont pas prise en compte par ce traitement</b>
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
	 * Méthode init.<br>
	 * Rôle : Permet d'initialiser la classe pour les valeurs statiques<br/>
	 * s'emploie tel que, par exemple :<br/>
	 * <ul>
	 * <i>static {init();}</i><br/>
	 * </ul>
	 * Cette méthode tente de trouver par elle-même le nom de la classe à initialiser.<p/>
	 * <b><u>/!\</u> il faut placer cette déclaration en dernière dans la classe, les variables déclarées après ne sont pas prise en compte par ce traitement</b>
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
	 * 
	 * Méthode init.<br>
	 * R�ôe : Permet d'initialiser la classe pour les valeurs statiques<br/>
	 * s'emploie tel que, par exemple :<br/>
	 * <ul>
	 * <i>static {init();}</i><br/>
	 * </ul>
	 * Cette méthode tente de trouver par elle-même le nom de la classe à initialiser.<p/>
	 * <b><u>/!\</u> il faut placer cette déclaration en dernière dans la classe, les variables déclarées après ne sont pas prise en compte par ce traitement</b>
	 *
	 * @param UseClazzName : Flag indiquant si les paramêtres sont préfixés ou non
	 *  du nom de la classe
	 */
	public static void init(Boolean UseClazzName) {
		try {
			UseClassName = UseClazzName;
			/*
			 * Si l'on spécifie le nom de la classe, on recupère celui-ci
			 */
			if (UseClassName) clazz = guessClass();
			init();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

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
	static public void initParam(String[] param) throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
		updateParam(clazz, param, false);
	}	

	/**
	 * 
	 * Méthode updateParam.<br>
	 * Rôle : remplace les valeurs des attributs de la classe appelante par celle de la classe étendant Param.<br/>
	 * Par défaut, ne remplace la valeur que des paramètres ayant une valeur <b>=null</b> 
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
	 * Méthode updateParam.<br>
	 * Rôle : remplace les valeurs des attributs de la classe appelante par celle de la classe étendant Param.<br/>
	 *
	 * @param o : la classe appelante (typiquement <i>this</i>)
	 * @param force : force le remplacement des valeurs, même pour les attributs non null
	 */
	static public void updateParam(Class o, boolean force) {
		updateParam(o,null,force);
	}

	/**
	 * 
	 * Méthode updateParam.<br>
	 * Rôle : remplace les valeurs des attributs de la classe appelante par celle de la classe étendant Param.<br/>
	 *
	 * @param o : l'objet appelant
	 * @param force : force le remplacement des valeurs, même pour les attributs non null
	 */
	static public void updateParam(Object o, boolean force) {
		updateParamInstance(o,null,force);
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
	static public void updateParam(Class o, String[] param) {
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
	 * 
	 */
	static public void updateParam(Object o, String[] param) {
		Class c = getClassFromName(o);
		updateParam(c,param,false);
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
	 * Méthode updateParam.<br>
	 * Rôle : remplace les valeurs des attributs de la classe appelante par celle de la classe étendant Param.<br/>
	 * Par défaut, ne remplace la valeur que des paramètres ayant une valeur =null 
	 *
	 * @param o : l'objet appelant
	 * @param param : la liste des attributs à modifier (ou null si tous les attributs sont susceptible d'être modifiés)<br/>     
	 * @param force : force le remplacement des valeurs, même pour les attributs non null
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
				param = HashToArray(allField);
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
	 * Méthode setParam<br/>
	 * Rôle : Met a jour dynamiquement la valeur d'une variable de la classe
	 * @param Name
	 * @param Value
	 * @throws Exception
	 */
	public static void setParam(String Name, String Value) throws Exception {
		Field field = clazz.getField(Name); 
		field.set(clazz, ObjectConverter.convert(Value, field.getType()));
	}

	/**
	 * Méthode getAllFieldsRec<br/>
	 * Rôle : Retourne tous les champs de la classe passée en paramètre (y compris ceux de la classe mère)
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
	 * Méthode guessClass<br/>
	 * Rôle : Devine la classe appelante la librairie
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
	 * Méthode getClassFromName.<br>
	 * Rôle : retourne une classe à partir du nom de celle-ci
	 * @param o
	 * @return La classe retrouvée à partir du nom
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
	 * Méthode initParam.<br>
	 * @param allField
	 * @return
	 */
	static private String[] HashToArray(HashMap<String, Field> allField) {
		String[] param = new String[allField.size()];
		int i=0;
		for (Field field : allField.values()) {
			param[i++] = field.getName();
		}
		return param;
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

	/*
	 * Méthodes nécessaires pour l'utilisation JMX  
	 */

	/*
	 * (non-Javadoc)
	 * @see javax.management.DynamicMBean#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String attribute)
	throws AttributeNotFoundException, MBeanException,
	ReflectionException {
		HashMap<String, Field> allField = getAllFieldsRec(clazz);
		if (!allField.containsKey(attribute)) throw new AttributeNotFoundException();
		try {
			Field field = allField.get(attribute);
			field.setAccessible(true);
			return field.get(clazz);
		}catch (Exception e) {
			throw new ReflectionException(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see javax.management.DynamicMBean#setAttribute(javax.management.Attribute)
	 */
	public void setAttribute(Attribute attribute)
	throws AttributeNotFoundException, InvalidAttributeValueException,
	MBeanException, ReflectionException {
		try {
			setParam(attribute.getName(), attribute.getValue().toString());
		} catch (Exception e) {
			throw new AttributeNotFoundException();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.management.DynamicMBean#getAttributes(java.lang.String[])
	 */
	public AttributeList getAttributes(String[] attributes) {
		HashMap<String, Field> allField = getAllFieldsRec(clazz);
		AttributeList resultList = new AttributeList();
		for (String attribute : attributes) {
			if (allField.containsKey(attribute))
			{
				try {
					Field field = allField.get(attribute);
					field.setAccessible(true);
					Object o = field.get(clazz);
					resultList.add(new Attribute(attribute,o));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return resultList;
	}


	/*
	 * (non-Javadoc)
	 * @see javax.management.DynamicMBean#setAttributes(javax.management.AttributeList)
	 */
	public AttributeList setAttributes(AttributeList attributes) {

		// Check attributes is not null to avoid NullPointerException later on
		//
		if (attributes == null) {
			throw new RuntimeOperationsException(
					new IllegalArgumentException(
					"AttributeList attributes cannot be null"),
					"Cannot invoke a setter of " + clazz.getName());
		}
		AttributeList resultList = new AttributeList();

		// If attributeNames is empty, nothing more to do
		//
		if (attributes.isEmpty())
			return resultList;

		// For each attribute, try to set it and add to the result list if
		// successfull
		//
		for (Iterator i = attributes.iterator(); i.hasNext();) {
			Attribute attr = (Attribute) i.next();
			try {
				setAttribute(attr);
				String name = attr.getName();
				Object value = getAttribute(name); 
				resultList.add(new Attribute(name,value));
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return resultList;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.management.DynamicMBean#invoke(java.lang.String, java.lang.Object[], java.lang.String[])
	 */
	public Object invoke(String actionName, Object[] params, String[] signature)
	throws MBeanException, ReflectionException {
		try {
			Class[] paramTypes = null;
			if(params != null)
			{
				paramTypes = new Class[params.length];
				for(int i=0;i<params.length;++i)
				{
					paramTypes[i] = params[i].getClass();
				}
			}

			Method m = clazz.getMethod(actionName,paramTypes);
			return m.invoke(clazz,params);
		} catch (Exception e) {
			throw new ReflectionException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.management.DynamicMBean#getMBeanInfo()
	 */
	public MBeanInfo getMBeanInfo() {
		MBeanNotificationInfo[] dNotifications = new MBeanNotificationInfo[0];


		String dClassName = clazz.getName();
		String dDescription = dClassName + " dynamic MBean (by jLibParam)";

		/**
		 * On va parcourir les champs dispo, et pour chacun regarder si l'annotation
		 * contient la valeur passée en paramètre
		 */
		HashMap<String,Field> allField = getAllFieldsRec(clazz);
		List<Field> fields = new ArrayList<Field>();
		for (Field fieldParam : allField.values()) {
			jmx fieldAnnotation = fieldParam.getAnnotation(jmx.class);
			if (fieldAnnotation!=null) fields.add(fieldParam);
		}

		/*
		 * Attributes info
		 */
		MBeanAttributeInfo[] dAttributes = new MBeanAttributeInfo[fields.size()];
		int cpt = 0;
		for (Field field : fields){
			jmx fieldsAnnotation = field.getAnnotation(jmx.class);
			dAttributes[cpt++]  =
				new MBeanAttributeInfo(field.getName(),
						field.getType().getName(),
						(fieldsAnnotation.value()=="") ? field.getName() : fieldsAnnotation.value(),
								fieldsAnnotation.read(),
								fieldsAnnotation.write(),
								fieldsAnnotation.is());
		}

		/*
		 * Constructor info
		 */
		Constructor<?>[] constructors = clazz.getConstructors();
		MBeanConstructorInfo[] dConstructors = new MBeanConstructorInfo[constructors.length];
		for (int NbConstructor=0 ; NbConstructor < constructors.length; NbConstructor++) {
			dConstructors[NbConstructor] =
				new MBeanConstructorInfo("Constructs a " +
						"SimpleDynamic object",
						constructors[NbConstructor]);			  
		}


		/*
		 * Déclaration des méthodes à exposer à JMX
		 */
		Method[] methods = clazz.getDeclaredMethods();
		MBeanOperationInfo[] dOperations = new MBeanOperationInfo[methods.length];
		for (int NbMethod=0; NbMethod < methods.length; NbMethod++) {
			Method method = methods[NbMethod];
			jmx methodAnnotation = method.getAnnotation(jmx.class);
			if (methodAnnotation!=null) {
				if (methodAnnotation.paramName() != null) {  /* si l'on a declaré des annotations contenant le nom des paramètres */					
					/* on s'interesse aux paramètres de la methode */
					Class<?>[] parametersType = method.getParameterTypes();
					assert(methodAnnotation.paramName().length==parametersType.length); /* on a autant de nom de paramètre que de paramètre*/
					
					MBeanParameterInfo[] dParameters = new MBeanParameterInfo[parametersType.length];
					for (int NbParam=0; NbParam<parametersType.length; NbParam++) {
						dParameters[NbParam] =  new MBeanParameterInfo(methodAnnotation.paramName()[NbParam],parametersType[NbParam].getName(),"");
					}
					dOperations[NbMethod] = new MBeanOperationInfo(
							method.getName(),
							(methodAnnotation.value()=="") ? method.getName() : methodAnnotation.value(),
									dParameters,
									method.getReturnType().getName(),
									MBeanOperationInfo.UNKNOWN
					);
				} else { /* si l'on a pas declaré d'annotations contenant le nom des paramètres */
					dOperations[NbMethod] = new MBeanOperationInfo(
							(methodAnnotation.value()=="") ? method.getName() : methodAnnotation.value(),
									method
					);
				}
			}


		}


		/*
		 * 
		 */
		MBeanInfo dMBeanInfo = new MBeanInfo(dClassName,
				dDescription,
				dAttributes,
				dConstructors,
				dOperations,
				dNotifications);

		return dMBeanInfo;
	}
}