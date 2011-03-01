package org.jLib.Pool;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.jLib.Pool.factory.IPoolFactory;




/**
 * La classe Pool<T> g�re un pool d'�l�ment.<br/>
 * Le nombre d'�l�ment du pool est d�finie dans le fichier de propri�t� PROPERTIES_POOLPROXYSERVICE<br/>
 * Dans le cas ou le nombre d'�l�ment n'est pas suffisant, de nouveau �l�ment sont cr�� tant que cela est n�cessaire
 * et d�truit apr�s utilisation de fa�on � conserver le nombre initial d'�l�ment.
 *  
 * @author s�bastien Bettinger
 *
 * @param <T>
 */
public final class Pool<T> {

	/**
	 * Logger
	 */
	private static final Logger logger = Logger.getLogger(Pool.class.getName());
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

	/**
	 * Taille initiale du pool d'objet
	 */
	private Integer PoolSize = 0;

	/**
	 * Nombre d'objet en cours dans le pool<br/>
	 * Ce nombre peut �tre sup�rieur � la taille initial, mais pas inf�rieur
	 */
	private Integer nbObject = 0;

	/**
	 * Factory de cr�ation des objets du pool
	 */
	private IPoolFactory<T> factory;

	/**
	 * Singleton du pool
	 */
	private static Pool<?> instance;


	@SuppressWarnings("unchecked")
	public static synchronized  <T> Pool<T> getInstance(){
		if (instance==null)					
		{
			try {
				/**
				 * On instancie alors le pool avec 10 �l�ments par d�faut
				 */
				Object poolFactory = Class.forName(Constants.poolFactory).newInstance();
				poolFactory = Class.forName((String)Constants.poolFactory).newInstance();
				instance=new Pool<T>((IPoolFactory<T>) poolFactory, Constants.PoolSize);	
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return (Pool<T>)instance;					
	}    


	/**
	 * Constructeur du pool
	 * @param factory
	 * @param poolSize
	 */
	private Pool(IPoolFactory<T> factory, Integer poolSize ) {
		logger.debug("Initialisation du pool avec " + poolSize + " objet ("+factory+")");
		this.factory = factory;
		this.PoolSize = poolSize;
		this.nbObject = poolSize;

		List<T> objects = new LinkedList<T>();
		for (int i=0; i< poolSize; i++)
			objects.add(factory.create());
		this.objects = new ConcurrentLinkedQueue<T>(objects);
	}

	/**
	 * Emprunte un objet au pool
	 * @return
	 * @throws InterruptedException
	 */
	synchronized public T borrow() throws Exception {
		T obj = null;
		if (this.objects.size()==0) {
			/* on limite le nombre d'objet dans le pool */
			if (this.nbObject >= Constants.MaxPoolSize)
				throw new Exception("MaxPool atteind");
			this.nbObject++;
			obj = factory.create();
			logger.warn("Pool vide => creation de l'objet " + obj.toString());
		} else {
			obj =  this.objects.poll();
			logger.debug("Pool => emprunt de l'objet " + obj.toString());
		}
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
		} else
			if (this.objects.size() < PoolSize) {
				this.objects.add(object);
				logger.debug("Pool => restitution de l'objet " + object.toString());
			} else { 
				this.nbObject--;
				logger.warn("Pool plein => destruction de l'objet " + object.toString());
			}
	}

	/**
	 * Renvoie la taille du pool
	 * @return
	 */
	public Integer getPoolSize() {
		logger.debug("Pool => Taille du pool : " + this.PoolSize);
		return this.PoolSize;
	}

	/**
	 * Renvoie le nombre d'objet dans
	 * le pool<br/>
	 * Au minimum, �gale � getPoolSize()<br/>
	 * indique si le PoolSize est d�pass�
	 * @return
	 */
	public Integer getNbObject() {
		logger.debug("Pool => Nbre d'objet : " + this.nbObject);
		return this.nbObject;
	}

	/**
	 * Assigne null a l'instance pour forcer un nouvel init du pool
	 */
	public static synchronized void razPool() {
		instance = null;
	}
}
