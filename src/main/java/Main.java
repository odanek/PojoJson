public class Main {
    static class Person {
        private final int id;
        private final String name;
        private final Person child;

        public Person(int id, String name, Person child) {
            this.id = id;
            this.name = name;
            this.child = child;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Person getChild() {
            return child;
        }
    }

    public static void main(String[] args) {
        Person originalPerson = new Person(1, "Ondrej", new Person(2, "Danek", null));
        String json = Json.stringify(originalPerson, true);
        System.out.println(json);
        Person deserializedPerson = Json.parse(Person.class, json);
    }
}
