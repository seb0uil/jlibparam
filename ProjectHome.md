In order to customize value in a class, we usually read these value from a property file, and update value in java code.
This lib does this for you :p

# About this Lib #
### Easily load values from properties file ###
this lib start from a simple need, I just want to easily read value from property file and assign these value to a class.
For example :
```
public class Config extends Param {
	public static String MyVar = "private string from sample";
	static {init(Config.class);}
}
```
will just change MyVar value if the line
_MyVar=New Value_
is present in the property file.

### Manage a pool ###
Have you ever need a object pooling system, jLibParam can do this

### Usual pooling system with factory ###
```
Pool<Integer> poolInt = Pool.getInstance(new IntegerFactory());
[...]
Integer iPool1 = poolInt.borrow();
[...]
poolString.giveBack(iPool1);
```

### Pooling system with objectHolder ###
Like a map where each key value manage it's own list of object with lifecycle and dead line for each object.
It can both limit number of object inside the pool, with capacity to deliver more object when needed..

```
PoolHolder.put("key", new Object());
[...]
PoolHolder.get(key);
```


---

# A quoi ca sert #
## Facilite le chargement de valeur depuis un fichier properties ##
Cette librairie est partie d'un besoin simple, faciliter la lecture de variable depuis un fichier properties, et simplifier la prise en compte de ces valeurs dans les classes.

Par exemple :
```
public class Config extends Param {
	public static String MyVar = "private string from sample";
	static {init(Config.class);}
}
```
Va permettre simplement de modifier la valeur de MyVar simplement en déclarant sa nouvelle valeur dans un fichier propriété.


## Permet de gérer un pool d'objet ##
Qui n'a jamais eu besoin de gérer un pool d'objet divers & variés en Java, jLibParam est là pour ca ..

### Un pool classique avec une Factory d'objet ###
```
Pool<Integer> poolInt = Pool.getInstance(new IntegerFactory());
[...]
Integer iPool1 = poolInt.borrow();
[...]
poolString.giveBack(iPool1);
```

### Un pool plus souple avec un objectHolder ###
On associe un objet à une durée de vie, et s'il est présent dans le pool trop longtemps, on l'élimine.
Dès qu'un objet est demandé, le pool le sert ou en crée un nouveau si nécessaire.
On limite ainsi le nombre d'objet présent sur la durée, tout en absorbant les montées en charge.

```
PoolHolder.put("key", new Object());
[...]
PoolHolder.get(key);
```
It use ObjectConverter class from http://balusc.blogspot.com/2007/08/generic-object-converter.html