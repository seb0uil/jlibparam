package org.jLib.Pool;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.jLib.Pool.factory.IPoolFactory;

/**
 * La classe Pool<T> gère un pool d'élément.<br/>
 * Le nombre d'élément du pool est définie dans le fichier de propriété PROPERTIES_POOLPROXYSERVICE<br/>
 * Dans le cas ou le nombre d'élément n'est pas suffisant, de nouveau élément sont créé tant que cela est nécessaire
 * et détruit après utilisation de façon à conserver le nombre initial d'élément.
 *  
 * @author sébastien Bettinger
 *
 * @param <T>
 */
public final class Pool<T> {

	/**
	 * Logger
	 */
	private static final Logger logger = Logger.getLogger(Pool.class.getName());
	private static final long expirationTime = 1;
	;
	//	static {
	//
	//		URL log4jURL =  Loader.getResource(Constants.log4jFile);
	//		PropertyConfigurator.configure(log4jURL);
	//		logger = LogFactory.getLog( Pool.class.getName());
	//	}

	/**
	 * Pool d'objet
	 */
	private ConcurrentLinkedQueue<T> objects;

	private List<T> listObjects = new ArrayList<T>();

	/**
	 * Taille initiale du pool d'objet
	 */
	private Integer PoolSize = 0;

	/**
	 * Nombre d'objet en cours dans le pool<br/>
	 * Ce nombre peut être supérieur à la taille initial, mais pas inférieur
	 */
	//	private Integer nbObject = 0;

	/**
	 * Factory de création des objets du pool
	 */
	private IPoolFactory<T> factory;

	private Hashtable<T, Long> locked = new Hashtable<T, Long>();

	/**
	 * Singleton du pool
	 */
	private static HashMap<IPoolFactory<?>,Pool<?>> instance = new HashMap<IPoolFactory<?>,Pool<?>>();


	public static synchronized  <T> Pool<T> getInstance(){
		try {
			IPoolFactory<?> poolFactory = (IPoolFactory<?>)Class.forName(Constants.poolFactory).newInstance();
			return getInstance(poolFactory);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}    

	public static synchronized  <T> Pool<T> getInstance(IPoolFactory<?> poolFactory){
		try {
			/**
			 * On instancie alors le pool avec 10 éléments par défaut
			 */
			if (!instance.containsKey(poolFactory))					
			{			
				Pool<T> instanceFactory = new Pool<T>((IPoolFactory<T>) poolFactory, Constants.PoolSize);
				instance.put(poolFactory, instanceFactory);
				return (Pool<T>)instanceFactory;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Constructeur du pool
	 * @param factory
	 * @param poolSize
	 */
	private Pool(IPoolFactory<T> factory, Integer poolSize ) {
		if (logger.isDebugEnabled()) 
			logger.debug("Initialisation du pool avec " + poolSize + " objet ("+factory+")");
		this.factory = factory;
		this.PoolSize = poolSize;
		//		this.nbObject = poolSize;

		listObjects = new LinkedList<T>();
		List<T> objects = new LinkedList<T>();
		for (int i=0; i< poolSize; i++) {
			objects.add(factory.create());
			listObjects.add(factory.create());
		}
		this.objects = new ConcurrentLinkedQueue<T>(objects);
	}

	/**
	 * Emprunte un objet au pool
	 * @return
	 * @throws InterruptedException
	 */
	synchronized public T borrow() throws Exception {
		long now = System.currentTimeMillis();

		T obj = null;
		if (this.objects.size()==0) {
			/*
			 * Il n'y a plus d'objet de dispo dans le pool,
			 * on regarde donc dans ceux déjà sortie, si certains ont expiré et peuvent être réutilisé
			 */
			if (listObjects.size() >= Constants.MaxPoolSize) {
				Enumeration<T> e = locked.keys();
				while (e.hasMoreElements() && obj == null) {
					T lockObj = e.nextElement();

					/*
					 * Si l'objet a expiré
					 */
					if ((now - locked.get(lockObj)) > expirationTime) {
						// object has expired
						locked.remove(lockObj);
						factory.expire(lockObj);
						obj = lockObj;
						continue;
					}
				}
				if (obj == null) 
					throw new Exception("MaxPool atteind");
			} else {
				//				this.nbObject++;
				obj = factory.create();
				listObjects.add(obj);
				logger.warn("Pool vide => creation de l'objet " + obj.toString());
			}
		} else {
			obj =  this.objects.poll();
			if (logger.isDebugEnabled()) 
				logger.debug("Pool => emprunt de l'objet " + obj.toString());
		}

		/*
		 * On place dans la map locked l'objet avec sa horodatage de sortie
		 */
		locked.put(obj, now);
		return obj;
	}

	/**
	 * Retourne un objet au pool
	 * @param object
	 * @throws InterruptedException
	 */
	synchronized public void giveBack(T object)  {
		if (object == null) {
			logger.warn("Retour d'un objet null ");
		} else {
			if (this.objects.size() < PoolSize) {
				this.objects.add(object);
				if (logger.isDebugEnabled()) 
					logger.debug("Pool => restitution de l'objet " + object.toString());
			} else { 
				//				this.nbObject--;
				listObjects.remove(object);
				logger.warn("Pool plein => destruction de l'objet " + object.toString());
			}
			locked.remove(object);
		}
		/*
		 * On purge les objets périmés
		 */
		Enumeration<T> e = locked.keys();
		long now = System.currentTimeMillis();
		while (e.hasMoreElements()) {
			T lockObj = e.nextElement();
			/*
			 * Si l'objet a expiré
			 */
			if ((now - locked.get(lockObj)) > expirationTime) {
				// object has expired
				locked.remove(lockObj);
				factory.expire(lockObj);

				if (this.objects.size() < PoolSize) {
					this.objects.add(lockObj);
					if (logger.isDebugEnabled()) 
						logger.debug("Pool => restitution de l'objet " + lockObj.toString());
				} else { 
					//					this.nbObject--;
					listObjects.remove(lockObj);
					logger.warn("Pool plein => destruction de l'objet " + lockObj.toString());
				}
			}
		}
	}

	/**
	 * Renvoie la taille du pool
	 * @return
	 */
	public Integer getPoolSize() {
		if (logger.isDebugEnabled()) 
			logger.debug("Pool => Taille du pool : " + this.PoolSize);
		return this.PoolSize;
	}

	/**
	 * Renvoie le nombre d'objet dans
	 * le pool<br/>
	 * Au minimum, égale à getPoolSize()<br/>
	 * indique si le PoolSize est dépassé
	 * @return
	 */
	public Integer getNbObject() {
		if (logger.isDebugEnabled()) 
			logger.debug("Pool => Nbre d'objet : " + listObjects.size());
		return listObjects.size();
	}

	/**
	 * Assigne null a l'instance pour forcer un nouvel init du pool
	 */
	public static synchronized void razPool() {
		instance = null;
	}
}
