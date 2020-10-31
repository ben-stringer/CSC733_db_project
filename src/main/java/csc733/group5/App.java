/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package csc733.group5;

import org.neo4j.driver.*;

public class App {



    public static void main(String[] args) {
        System.out.println("Hello CSC733 World");

        final RandomDataGenerator rdg = new RandomDataGenerator(42);

        final Driver driver = GraphDatabase.driver("bolt://localhost:7687",
                AuthTokens.basic("neo4j", "secret"));

        final StringBuilder cypher = new StringBuilder();
        // create a warehouse
        cypher.append(String.format("create (w:Warehouse %s)\n", rdg.randomWarehouse(1)));
        // create 10 districts for the warehouse
        for (int i = 0; i < 10; i++) {
            cypher.append(String.format("create (w)-[:SERVICES]->(d%d:District %s)\n", i, rdg.randomDistrict(i)));
        }
        // create 100k items, not necessarily in stock
        for (int i = 0; i < 200; i++) {
            if (rdg.nextBoolean()) {
                // If item is in stock
                cypher.append(String.format(
                        "create (w)-[:IN_STOCK %s]->(i%d:Item %s)\n",
                        rdg.randomItemQuantity(),
                        i,
                        rdg.randomItem(i)));
            } else {
                // Item is not in stock
                // TODO: Do we want to include a link anyway and set quantity to 0?
                cypher.append(String.format("create (i%d:Item %s)\n", i, rdg.randomItem(i)));
            }
        }
        final String cypherText = cypher.toString();
        System.out.println("Submitting the following cypher:");
        System.out.println("********************************************************************************");
        System.out.println(cypherText);
        System.out.println("********************************************************************************");

        try (final Session session = driver.session()) {
            final Transaction tx = session.beginTransaction();
            // Clear everything already in the database
            tx.run("match (n) detach delete n");

            // Now run the creation query
            tx.run(cypherText);

            tx.commit();
        }

        driver.close();
    }
}
