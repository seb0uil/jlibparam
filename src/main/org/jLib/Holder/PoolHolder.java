package org.jLib.Holder;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PoolHolder {
	
	/*
	 * Gestion des softReferences SrOfObjectAvailable
	 */
	private static SoftReference<Map<String, ArrayList<ObjectHolder>>> SrOfObjectAvailable = new SoftReference<Map<String, ArrayList<ObjectHolder>>>(null);
	synchronized private static Map<String, ArrayList<ObjectHolder>> getObjectAvailable()  { 
		Map<String, ArrayList<ObjectHolder>> ObjectAvailable = SrOfObjectAvailable.get(); 
		if (ObjectAvailable == null) {ObjectAvailable = new HashMap<String, ArrayList<ObjectHolder>>();}
		return ObjectAvailable; 
	}
	synchronized private static void setObjectAvailable(Map<String, ArrayList<ObjectHolder>> objectAvailable)  {
		SrOfObjectAvailable =	new SoftReference<Map<String, ArrayList<ObjectHolder>>>(objectAvailable);
	}
	
	/*
	 * Gestion des softReferences SrOfObjectInUsed
	 */
	private static SoftReference<Map<String, ArrayList<ObjectHolder>>> SrOfObjectInUsed = new SoftReference<Map<String, ArrayList<ObjectHolder>>>(null);
	synchronized private static Map<String, ArrayList<ObjectHolder>> getObjectInUsed()  { 
		Map<String, ArrayList<ObjectHolder>> ObjectInUsed = SrOfObjectInUsed.get(); 
		if (ObjectInUsed == null) {ObjectInUsed = new HashMap<String, ArrayList<ObjectHolder>>();}
		return ObjectInUsed; 
	}
	synchronized private static void setObjectInUsed(Map<String, ArrayList<ObjectHolder>> objectInUsed)  {
		SrOfObjectInUsed =	new SoftReference<Map<String, ArrayList<ObjectHolder>>>(objectInUsed);
	}
	
	/**
	 * mapOfClass permet de conserver le type de classe stock� pour chaque cl�,
	 * par la suite, cela permet de v�rifier lorsque l'on ajoute un nouvel �l�ment
	 * dans la liste qu'il est de la bonne classe
	 */
	private static Map<String, Class<?>> mapOfClass = new HashMap<String, Class<?>>();
	
	/**
	 * Retourne le nombre d'objet disponible dans le pool pour la cl� indiqu�e
	 * @param key
	 * @return
	 */
	synchronized public static int getAvailableSize(String key) {
		return getObjectAvailable().get(key).size();
	}
	
	/**
	 * Retourne le nombre d'objet utilis� dans le pool pour la cl� indiqu�e
	 * @param key
	 * @return
	 */
	synchronized public static int getInusedSize(String key) {
		return getObjectInUsed().get(key).size();
	}
	
	/**
	 * �limine les objets morts associ� � la cl� pass�e en param�tre
	 * @param key
	 */
	synchronized public static void checkDead(String key) {
		Map<String, ArrayList<ObjectHolder>> mapOfObjectAvailable = getObjectAvailable();
		Map<String, ArrayList<ObjectHolder>> mapOfObjectInUsed = getObjectInUsed();
		
		ArrayList<ObjectHolder> ListOfObjectAvailable = mapOfObjectAvailable.get(key);
		ArrayList<ObjectHolder> ListOfObjectInUsed = mapOfObjectInUsed.get(key);
		
		@SuppressWarnings("unchecked")
		ArrayList<ObjectHolder> cloneInUsed = (ArrayList<ObjectHolder>) ListOfObjectInUsed.clone();
		@SuppressWarnings("unchecked")
		ArrayList<ObjectHolder> cloneAvailable = (ArrayList<ObjectHolder>) ListOfObjectAvailable.clone();
		
		for (ObjectHolder objectHolder : cloneAvailable) {
			if (objectHolder.isDead()) {
				/*
				 * Si l'objet est mort, il n'a plus lieu d'�tre conserv�, on le supprime tout simplement
				 */
				ListOfObjectAvailable.remove(objectHolder);
			}
		}
		
		for (ObjectHolder objectHolder : cloneInUsed) {
			if (objectHolder.isDead()) {
				/*
				 * Si l'objet est mort, il n'a plus lieu d'�tre conserv�, on le supprime tout simplement
				 */
				ListOfObjectAvailable.remove(objectHolder);
			}
		}
		/*
		 * On remet ensuite les SoftReference a jour avec les Map
		 */
		setObjectAvailable(mapOfObjectAvailable);
		setObjectInUsed(mapOfObjectInUsed);
	}
	
	/**
	 * Retourne un objet associ� � la cl�.
	 * Dans le cas ou aucun objet n'est disponible, ou qu'il n'en existe
	 * aucun avec la cl� associ�e, la m�thode renvoie null
	 * @param key
	 * @return
	 */
	synchronized public static Object get(String key) {
		Map<String, ArrayList<ObjectHolder>> mapOfObjectAvailable = getObjectAvailable();
		Map<String, ArrayList<ObjectHolder>> mapOfObjectInUsed = getObjectInUsed();
		
		if (!mapOfObjectAvailable.containsKey(key)) return null;

		Object objectToReturn = null;
		ArrayList<ObjectHolder> ListOfObjectAvailable = mapOfObjectAvailable.get(key);
		ArrayList<ObjectHolder> ListOfObjectInUsed = mapOfObjectInUsed.get(key);
		
		@SuppressWarnings("unchecked")
		ArrayList<ObjectHolder> cloneInUsed = (ArrayList<ObjectHolder>) ListOfObjectInUsed.clone();
		@SuppressWarnings("unchecked")
		ArrayList<ObjectHolder> cloneAvailable = (ArrayList<ObjectHolder>) ListOfObjectAvailable.clone();
		
		for (ObjectHolder objectHolder : cloneInUsed) {

			if (objectHolder.isExpired()) {
				/*
				 * Si l'objet est p�rim�, on le repasse dans la liste des objets disponible.
				 * C'est la gestion du timeout
				 */
				ListOfObjectInUsed.remove(objectHolder);
				ListOfObjectAvailable.add(objectHolder);
				objectToReturn = objectHolder.get(); 	// on recupere l'objet
				objectHolder.Touch();	//on remet a jour le timestamp sur la date de la derniere utilisation
			} else if (objectHolder.isDead()) {
				/*
				 * Si l'objet est mort, il n'a plus lieu d'�tre conserv�, on le supprime tout simplement
				 */
				ListOfObjectInUsed.remove(objectHolder);
			}
		}

		if (ListOfObjectAvailable.size() > 0) {
			ObjectHolder objectHolder = ListOfObjectAvailable.get(0);
			ListOfObjectAvailable.remove(objectHolder);
			ListOfObjectInUsed.add(objectHolder);
			objectToReturn= objectHolder.get();
			
			for (ObjectHolder objectHolderAvailable : cloneAvailable) {
				if (objectHolderAvailable.isDead()) {
					/*
					 * Si l'objet est mort, il n'a plus lieu d'�tre conserv�, on le supprime tout simplement
					 */
					ListOfObjectAvailable.remove(objectHolderAvailable);
				}
			}
		}
		/*
		 * On remet ensuite les SoftReference a jour avec les Map
		 */
		setObjectAvailable(mapOfObjectAvailable);
		setObjectInUsed(mapOfObjectInUsed);
		return objectToReturn;
	}
	
	/**
	 * Ajoute un objet au pool, associ� avec la cl� pass�e en param�tre.
	 * Dans le cas ou des objets sont d�j� pr�sent dans le pool, pour la cl� donn�e,
	 * la classe est v�rif�e. En cas d'incoh�rence, une BadClassException est lev�e
	 * @param key cl� � associer � l'objet � stocker
	 * @param o objet � stocker dans le pool
	 * @throws BadClassException
	 */
	synchronized public static void put(String key, Object o) throws BadClassException {
		Map<String, ArrayList<ObjectHolder>> mapOfObjectAvailable = getObjectAvailable();
		Map<String, ArrayList<ObjectHolder>> mapOfObjectInUsed = getObjectInUsed();
		
		Class<?> clazz = o.getClass();
		
		if (!mapOfObjectAvailable.containsKey(key)) {
			ObjectHolder holder = new ObjectHolder(o);
			ArrayList<ObjectHolder> ListOfObjectAvailable = new ArrayList<ObjectHolder>();
			ListOfObjectAvailable.add(holder);
			mapOfObjectAvailable.put(key, ListOfObjectAvailable);
			mapOfObjectInUsed.put(key, new ArrayList<ObjectHolder>());
			mapOfClass.put(key, clazz);
		} else {
			if (!clazz.isAssignableFrom(mapOfClass.get(key))) {
				throw new BadClassException("Classe of " + o.toString() + "("+ clazz.getName() +") is different from " + mapOfClass.get(key).getName());
			}
			
			/*
			 * On regarde si l'objet retourn� est l'un de ceux emprunt�
			 */
			ArrayList<ObjectHolder> ListOfObjectInUsed = mapOfObjectInUsed.get(key);
			ArrayList<ObjectHolder> ListOfObjectAvailable = mapOfObjectAvailable.get(key);
			
			@SuppressWarnings("unchecked")
			ArrayList<ObjectHolder> cloneInUsed = (ArrayList<ObjectHolder>) ListOfObjectInUsed.clone();
			@SuppressWarnings("unchecked")
			ArrayList<ObjectHolder> cloneAvailable = (ArrayList<ObjectHolder>) ListOfObjectAvailable.clone();
			
			boolean objectAlreadyInList = false;
			for (ObjectHolder objectHolder : cloneAvailable) {
				if (objectHolder.get() == o) {  //on utilise == pour v�rifier qu'il s'agit bien de la m�me instance d'objet !!
					ListOfObjectInUsed.remove(objectHolder);
//					ListOfObjectAvailable.add(objectHolder);
					objectAlreadyInList = true;
					break;
				}
			}
			
			for (ObjectHolder objectHolder : cloneInUsed) {
				if (objectHolder.get() == o) {  //on utilise == pour v�rifier qu'il s'agit bien de la m�me instance d'objet !!
					ListOfObjectInUsed.remove(objectHolder);
					ListOfObjectAvailable.add(0,objectHolder);
					objectAlreadyInList = true;
					break;
				}
			}
			
			if (!objectAlreadyInList) {
				ObjectHolder holder = new ObjectHolder(o);
				ListOfObjectAvailable.add(holder);				
			}
		}
		/*
		 * On remet ensuite les SoftReference a jour avec les Map
		 */
		setObjectAvailable(mapOfObjectAvailable);
		setObjectInUsed(mapOfObjectInUsed);
	}
	
	/**
	 * Ajoute un objet au pool, associ� avec la cl� pass�e en param�tre.<br/>
	 * L'objet est plac� dans la liste des objets utilis�s.
	 * Dans le cas ou des objets sont d�j� pr�sent dans le pool, pour la cl� donn�e,
	 * la classe est v�rif�e. En cas d'incoh�rence, une BadClassException est lev�e.<br/>
	 * Dans le cas ou l'objet est d�j� pr�sent dans la liste des objets utilis�s, une AlreadyInUsedException est lev�
	 * @param key cl� � associer � l'objet � stocker
	 * @param o objet � stocker dans le pool
	 * @throws BadClassException
	 * @throws AlreadyInUsedException 
	 */
	synchronized public static void putInUsed(String key, Object o) throws BadClassException, AlreadyInUsedException {
		Map<String, ArrayList<ObjectHolder>> mapOfObjectAvailable = getObjectAvailable();
		Map<String, ArrayList<ObjectHolder>> mapOfObjectInUsed = getObjectInUsed();
		
		Class<?> clazz = o.getClass();
		
		if (!mapOfObjectAvailable.containsKey(key)) {
			ObjectHolder holder = new ObjectHolder(o);
			ArrayList<ObjectHolder> ListOfObjectInUsed = new ArrayList<ObjectHolder>();
			ListOfObjectInUsed.add(holder);
			mapOfObjectInUsed.put(key, ListOfObjectInUsed);
			mapOfObjectAvailable.put(key, new ArrayList<ObjectHolder>());
			mapOfClass.put(key, clazz);
		} else {
			if (!clazz.isAssignableFrom(mapOfClass.get(key))) {
				throw new BadClassException("Classe of " + o.toString() + "("+ clazz.getName() +") is different from " + mapOfClass.get(key).getName());
			}
			
			/*
			 * On regarde si l'objet retourn� est l'un de ceux emprunt�
			 */
			ArrayList<ObjectHolder> ListOfObjectInUsed = mapOfObjectInUsed.get(key);
			ArrayList<ObjectHolder> ListOfObjectAvailable = mapOfObjectAvailable.get(key);
			
			@SuppressWarnings("unchecked")
			ArrayList<ObjectHolder> cloneInUsed = (ArrayList<ObjectHolder>) ListOfObjectInUsed.clone();
			@SuppressWarnings("unchecked")
			ArrayList<ObjectHolder> cloneAvailable = (ArrayList<ObjectHolder>) ListOfObjectAvailable.clone();
			
			boolean objectAlreadyInList = false;
			/*
			 * On parcourt la liste des objets disponibles pour d�placer l'objet � ajouter vers
			 * les objets utilis�s si n�cessaire
			 */
			for (ObjectHolder objectHolder : cloneAvailable) {
				if (objectHolder.get() == o) {  //on utilise == pour v�rifier qu'il s'agit bien de la m�me instance d'objet !!
					ListOfObjectInUsed.add(objectHolder);
					ListOfObjectAvailable.remove(objectHolder);
					objectAlreadyInList = true;
					break;
				}
			}
			
			/*
			 * On v�rifie la liste des objets utilis�s, si l'objet est d�j� pr�sent, on l�ve une exception
			 */
			for (ObjectHolder objectHolder : cloneInUsed) {
				if (objectHolder.get() == o) {  //on utilise == pour v�rifier qu'il s'agit bien de la m�me instance d'objet !!
					throw new AlreadyInUsedException();
				}
			}
			
			if (!objectAlreadyInList) {
				ObjectHolder holder = new ObjectHolder(o);
				ListOfObjectInUsed.add(holder);				
			}
		}
		/*
		 * On remet ensuite les SoftReference a jour avec les Map
		 */
		setObjectAvailable(mapOfObjectAvailable);
		setObjectInUsed(mapOfObjectInUsed);
	}	
}
