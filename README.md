# Practica1SBD2

## Usando docker crear un contenedor con postgres en su version 17
```$ sudo docker run --name practica1_db2 -e POSTGRES_USER=alodev -e POSTGRES_DB=recurrency -e POSTGRES_PASSWORD=contra123 -p 5432:5432 -d postgres```

## Crear la tabla en la base de datos
```
CREATE TABLE movements (
    id SERIAL PRIMARY KEY,
    counter INTEGER
);
```

## Generar el jar utilizando maven
```mvn clean package```

## Ejecutar .jar
``` java -jar myproject.jar```
