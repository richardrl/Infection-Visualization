package com.company;
import javafx.scene.paint.Color;
import org.graphstream.algorithm.DynamicAlgorithm;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.SingleGraph;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    private HashMap<User, HashSet<User>> userGraph;
    private HashSet<User> infected = new HashSet<User>();
    private float curVersion = 0f;

    public void init(Graph g) {

    }

    // Run BFS
    public void totalInfect(User infectedUser, float newVersion) {
        Queue<User> q = new LinkedList<User>();
        q.add(infectedUser);
        infectedUser.setVersion(newVersion);

        while (!q.isEmpty()) {
            User transmitter = (User) q.remove();
            HashSet<User> neighbors = userGraph.get(transmitter); // Neighbors is the union of infectedUser's coaches and coachees
            for (User neighbor : neighbors) {
                System.out.println("Checking neighbor:" + neighbor.getUserID() + " of " + transmitter.getUserID());
                System.out.println("Neighbor version: " +  neighbor.getVersion());
                if (neighbor.getVersion() != newVersion) {
                    System.out.println("Infect edge: " + transmitter.getUserID() + "|" + neighbor.getUserID());
                    neighbor.setVersion(newVersion);
                    q.add(neighbor);
                }
            }
        }
    }

    // Finds coach with largest number of coachees under target.
    public User findMaxEdgeUser(User[] userArr, int targetNo) {
        User startCoach = (User) userArr[0];
        for (int i = 0; i < userArr.length; i++) {
            User coach = (User) userArr[i];
            if (userGraph.get(coach).size() > userGraph.get(startCoach).size() && userGraph.get(coach).size() <= targetNo) {
                startCoach = coach;
            }
        }
        return startCoach;
    }

    public HashSet<User> symmetricDiff(HashSet<User> set1, HashSet<User> set2) {
        // Union
        HashSet<User> union = new HashSet<User>(set1);
        union.addAll(set2);

        // Remove intersection
        set1.retainAll(set2);
        union.removeAll(set1);
        return union;
    }

    // Finds coach with largest number of coachees under target.
    public User findCoacheeWithMaxUniqueNeighbors(User coach, User[] userArr, int targetNo, int infectedNo) {
        User startCoach = (User) userArr[0];
        int startUniques = symmetricDiff(userGraph.get(coach), userGraph.get(startCoach)).size();


        for (int i = 0; i < userArr.length; i++) {
            User coachee = (User) userArr[i];
            int coacheeUniques = symmetricDiff(userGraph.get(coach), userGraph.get(coachee)).size();
            if (coacheeUniques > startUniques && (infectedNo + userGraph.get(coachee).size()) <= targetNo) {
                startCoach = coachee;
                startUniques = symmetricDiff(userGraph.get(coach), userGraph.get(coachee)).size();
            }
        }
        return startCoach;
    }

    // Goal: minimize number of coachees that have different version than coach
    public void limitedInfect(int targetNo, float newVersion) {
        int infectedNo = 0;
        // Start by finding coach with largest number of users under the target.
        Object[] userArr = userGraph.keySet().toArray();
        User startCoach = findMaxEdgeUser(Arrays.copyOf(userArr, userArr.length, User[].class), targetNo);

        Queue<User> q = new LinkedList<User>();
        q.add(startCoach);
        startCoach.setVersion(newVersion);
        infectedNo += 1; // For infected start coach

        while (!q.isEmpty()) {
            User transmitter = (User) q.remove();
            HashSet<User> neighbors = userGraph.get(transmitter); // Neighbors is the union of infectedUser's coaches and coachees

            for (User neighbor : neighbors) {
                if (neighbor.getVersion() != newVersion) {
                    System.out.println("Infect edge: " + transmitter.getUserID() + "|" + neighbor.getUserID());
                    neighbor.setVersion(newVersion);
                    infectedNo += 1;
                }
            }
            if (neighbors.size() > 0) {
                User chosenCoachee = findCoacheeWithMaxUniqueNeighbors(transmitter, Arrays.copyOf(neighbors.toArray(), neighbors.toArray().length, User[].class), targetNo, infectedNo);
                if ((userGraph.get(chosenCoachee).size() + infectedNo) <= targetNo) q.add(chosenCoachee);
            }
        }
    }


    public void generateTestData(int numUsers, float baseVersion, int maxCoachees) {
        HashMap<User, HashSet<User>> testGraph = new HashMap<User, HashSet<User>>();
        HashSet<User> users = new HashSet<User>();
        HashSet<User> coachNeighbors = new HashSet<User>(); // Use this hashset to hold randomly generated coachees for each student in the tempGraph

        for (int z = 0; z < numUsers; z++) {
            users.add(new User(baseVersion, UUID.randomUUID()));
        }
        for (User coach : users) {
            int numCoachees; // Number of coachees for a given coach.
            if (maxCoachees == 0) {
                numCoachees = ThreadLocalRandom.current().nextInt(0, numUsers + 1);
            } else {
                numCoachees = ThreadLocalRandom.current().nextInt(0, maxCoachees + 1);
            }
            for (User coachee : users) { // Generate coachees for 'user'
                double p = Math.random();
                if (p <= ((double) numCoachees/numUsers) && coach != coachee) {
                    coachNeighbors.add(coachee);
                    System.out.println("Gen edge: " + coach.getUserID() + "|" + coachee.getUserID());
                    // Each user in the hashmap userGraph is mapped to a set of Neighbors. Since this set includes coachees and coaches, every time you add a coachee to the neighbor set of coach X, you must add coach X to the coachee's neighbor set. Essentially, this is an undirected graph, and we accomplish that by having every edge propagate its complement.
                    if (testGraph.get(coachee) != null) {
                        testGraph.get(coachee).add(coach);
                        System.out.println("Gen edge1: " + coachee.getUserID() + "|" + coach.getUserID());
                    } else {
                        HashSet<User> coacheeNeighbors = new HashSet<User>();
                        coacheeNeighbors.add(coach);
                        testGraph.put(coachee, coacheeNeighbors);
                        System.out.println("Gen edge2: " + coachee.getUserID() + "|" + coach.getUserID());
                        // The above is not working.
                    }
                }
            }
            if (testGraph.get(coach) == null) {
                testGraph.put(coach, coachNeighbors);
            } else {
                coachNeighbors.addAll(testGraph.get(coach));
                testGraph.put(coach, coachNeighbors);
            }
            coachNeighbors = new HashSet<User>(); // Empty hashset
        }
        userGraph = testGraph;
    }

    public void generateCase1() {
        HashMap<User, HashSet<User>> testGraph = new HashMap<User, HashSet<User>>();
        User a1 = new User(0f, UUID.randomUUID());
        User a2 = new User(0f, UUID.randomUUID());
        User a3 = new User(0f, UUID.randomUUID());
        User a4 = new User(0f, UUID.randomUUID());
        User a5 = new User(0f, UUID.randomUUID());
        User a6 = new User(0f, UUID.randomUUID());

        testGraph.put(a1, new HashSet<User>(Arrays.asList(a2)));
        testGraph.put(a2, new HashSet<User>(Arrays.asList(a3)));
        testGraph.put(a3, new HashSet<User>());
        testGraph.put(a4, new HashSet<User>(Arrays.asList(a5)));
        testGraph.put(a5, new HashSet<User>());
        userGraph = testGraph;
    }

    public void generateSpecialCases(int numUsers, float baseVersion) {
        HashMap<User, HashSet<User>> testGraph = new HashMap<User, HashSet<User>>();
        HashSet<User> users = new HashSet<User>();
        HashSet<User> tempNeighbors = new HashSet<User>(); // Use this hashset to hold randomly generated coachees for each student in the tempGraph

        for (int z = 0; z < numUsers; z++) {
            users.add(new User(baseVersion, UUID.randomUUID()));
        }
        for (User user : users) {
            int numCoachees = ThreadLocalRandom.current().nextInt(0, 4); // Number of coachees for this user
            for (User user1 : users) { // Generate coachees for 'user'
                double p = Math.random();
                if (p <= ((double) numCoachees/numUsers)) {
                    tempNeighbors.add(user1);
                }
            }
            testGraph.put(user, tempNeighbors);
            tempNeighbors = new HashSet<User>(); // Empty hashset
        }
        userGraph = testGraph;
    }

    public User getRandomUser() {
        Random rn = new Random();
        Object[] userArr = userGraph.keySet().toArray();
        return (User) userArr[rn.nextInt(userGraph.keySet().size())];
    }

    public static void main(String[] args) {
	// write your code here
        Graph graph = new SingleGraph("Infection Graph");

        Main test = new Main();
        test.generateTestData(50, 0f, 5);
//        test.generateSpecialCases(25, 0f);
//        test.generateCase1();

        User startingUser = test.getRandomUser();
        System.out.println("Starting from user: " + startingUser.getUserID());
//        test.totalInfect(startingUser, 1f);
        test.limitedInfect(20, 1f);

        // Draw nodes. Color in nodes that have been infected.
        for (User user : test.userGraph.keySet()) {
            String userId = user.getUserID().toString();
            Node temp = graph.addNode(userId);
            temp.addAttribute("ui.label", user.getUserID());
            if (user.getVersion() != test.curVersion) {
                temp.addAttribute("ui.style", "fill-color: rgb(50,205,50);");
            }
        }

        // Draw edges.
        for (User user : test.userGraph.keySet()) {
            String userId = user.getUserID().toString();
            for (User coachee : test.userGraph.get(user)) {
                System.out.println("Drawn edge: " + userId + "|" + coachee.getUserID().toString());
                if (graph.getEdge(userId + coachee.getUserID().toString()) == null && graph.getEdge(coachee.getUserID().toString() + "|" + userId) == null) {
                    try {
                        graph.addEdge(userId + "|" + coachee.getUserID().toString(), userId, coachee.getUserID().toString());
                    } catch (EdgeRejectedException e) {
                        System.out.println("ERE:" + e);
                    }
                }
            }
        }
        graph.display();
    }
}
