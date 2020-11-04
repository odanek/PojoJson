# PojoJson
Simple convertor between plain old Java objects and JSON

## Description
This is a very simple "library" for converting plain old Java objects to JSON format and back. It is limited, without ambitions, doesn't handle many edge cases but it is short and good enough for prototyping and similar scenarios.

The utility can handle primitive types, strings, characters and arrays. Nested Pojo's are supported as well.

## Usage
Having a plain old Java class like this:
```java
class Person {
	private final int id;
	private final String name;	

	public Person(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public  String getName() {
		return name;
	}
}
```
you can convert it to JSON and back using the `stringify` and `parse` methods:
```java
Person originalPerson = new Person(1, "Ondrej");
String json = Json.stringify(originalPerson); // => "{"id":1,"name":"Ondrej"}"
Person deserializedPerson = Json.parse(Person.class, json);
```

## Assumptions
- The utility currently assumes that there is only a single constructor that takes parameters corresponding to the declared fields (in the same order)