#How to configure to use JMX

# Introduction #
jLibParam can easyly expose attributs to jmx connector.
For each attribut, you can decide which one can be read/update by jmx connector


# Details #
You can add @jmx notation to attributes in order to expose them.

For example, in sample config class,
```
@jmx("La valeur de mon entier")
    public static Integer INTEGER = 1;
```

parameters :
|value | | description of the attribute|
|:-----|:|:----------------------------|
|read  | true/false  | is the attribute readable by jmx|
|write | true/false  | is the attribute writable by jmx|
|is    | true/false  | is the attribute a boolean|