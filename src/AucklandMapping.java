import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;


public class AucklandMapping extends GUI {
    private boolean isMapGenerated = false;
    private static final double ZOOM_FACTOR = 1.1;
    private Location origin, topLeft, topRight, bottomLeft, bottomRight;
    private Point pressedMouse;
    private Point releasedMouse;
    private Map<String, Road> Roads = new HashMap<String, Road>();
    private Map<String, Node> Nodes = new HashMap<>();
    private ArrayList<Node> unvisitedNodes = new ArrayList<>();
    private HashSet<Segment> rootNeighbors = new HashSet<>();
    private TrieNode roadNameRoot;
    private double scale = 8;
    private Queue<Node> selectedNodes = new ArrayDeque<>();
    private ArrayList<Node> selectedRouteNodes = new ArrayList<>();
    private HashSet<Node> articulationPoints = new HashSet<>();


    public AucklandMapping() {

    }

    private void processData(File node, File roadInfo, File segmentInfo) {
        origin = Location.newFromLatLon(-36.847622, 174.763444);
        recalculateDrawingEdges();
        try {
            BufferedReader fileScanner = new BufferedReader(new FileReader(node));
            while (fileScanner.ready()) {
                String currentLine = fileScanner.readLine();
                String[] splittedCurLine = currentLine.split("\t");
                Location temp = Location.newFromLatLon(Double.parseDouble(splittedCurLine[1]), Double.parseDouble(splittedCurLine[2]));
                Point tempPoint = temp.asPoint(origin, scale);
                Nodes.put(splittedCurLine[0], new Node(splittedCurLine[0], tempPoint, temp));
            }
            fileScanner.close();
        } catch (IOException e) {
            System.out.println("IO Exception Occurred");
        }
        ArrayList<Segment> unsortedSegments = new ArrayList<>();
        // Loads Segments
        try {
            BufferedReader fileScanner = new BufferedReader(new FileReader(segmentInfo));
            fileScanner.readLine();
            while (fileScanner.ready()) {
                ArrayList<Location> Locations = new ArrayList<>();
                String currentLine = fileScanner.readLine();
                String[] splittedCurLine = currentLine.split("\t");
                String currentRoadId = splittedCurLine[0];
                double Length = Double.parseDouble(splittedCurLine[1]);
                String StartNode = splittedCurLine[2];
                String EndNode = splittedCurLine[3];
                for (int i = 4; i < splittedCurLine.length - 1; i = i + 2) {
                    Location tempLocation = Location.newFromLatLon(Double.parseDouble(splittedCurLine[i]), Double.parseDouble(splittedCurLine[i + 1]));
                    Locations.add(tempLocation);
                }
                unsortedSegments.add(new Segment(currentRoadId, Length, Nodes.get(StartNode), Nodes.get(EndNode), Locations));
                Roads.put(currentRoadId, new Road(currentRoadId, Length, Integer.parseInt(StartNode), Integer.parseInt(EndNode)));
            }
            fileScanner.close();

        } catch (IOException e) {
            System.out.println("IO Exception Occurred");
        }
        // Loads Other Road Information
        try {
            BufferedReader fileScanner = new BufferedReader(new FileReader(roadInfo));
            fileScanner.readLine();
            while (fileScanner.ready()) {
                String currentLine = fileScanner.readLine();
                String[] splittedCurLine = currentLine.split("\t");
                String currentRoadId = splittedCurLine[0];
                if (!Roads.keySet().contains(currentRoadId)) {
                    Roads.put(currentRoadId, new Road(currentRoadId, Integer.parseInt(splittedCurLine[1]), splittedCurLine[2], splittedCurLine[3], Integer.parseInt(splittedCurLine[4]), Integer.parseInt(splittedCurLine[5]), Integer.parseInt(splittedCurLine[6]), Boolean.parseBoolean(splittedCurLine[7]), Boolean.parseBoolean(splittedCurLine[8]), Boolean.parseBoolean(splittedCurLine[9])));
                }
                Roads.get(currentRoadId).Type = Integer.parseInt(splittedCurLine[1]);
                Roads.get(currentRoadId).StreetName = splittedCurLine[2];
                Roads.get(currentRoadId).City = splittedCurLine[3];
                Roads.get(currentRoadId).Oneway = Integer.parseInt(splittedCurLine[4]);
                Roads.get(currentRoadId).Speed = Integer.parseInt(splittedCurLine[5]);
                Roads.get(currentRoadId).RoadClass = Integer.parseInt(splittedCurLine[6]);
                Roads.get(currentRoadId).NotForCar = Boolean.parseBoolean(splittedCurLine[7]);
                Roads.get(currentRoadId).NotForPedestrain = Boolean.parseBoolean(splittedCurLine[8]);
                Roads.get(currentRoadId).NotForBicycle = Boolean.parseBoolean(splittedCurLine[9]);
            }
            fileScanner.close();
        } catch (IOException e) {
            System.out.println("IO Exception Occurred");
        }
        // Assigns Segments To Correct Nodes
        for (Segment s : unsortedSegments) {
            if (Nodes.get(s.startNode.nodeID) != null) {
                Nodes.get(s.startNode.nodeID).outgoingEdges.add(s);
                Roads.get(s.roadId).roadNodes.add(Nodes.get(s.startNode.nodeID));
            }
            if (Nodes.get(s.endNode.nodeID) != null) {
                Nodes.get(s.endNode.nodeID).incomingEdges.add(s);
                Roads.get(s.roadId).roadNodes.add(Nodes.get(s.endNode.nodeID));
            }
        }
        // Assigns Segments To Road Objects
        for (Segment s : unsortedSegments) {
            if (Roads.get(s.roadId) != null) {
                Roads.get(s.roadId).addRoadSegments(s);
            }
        }
        // Generate Trie
        roadNameRoot = new TrieNode();
        for (String road : Roads.keySet()) {
            if (Roads.get(road) != null) {
                roadNameRoot.add(Roads.get(road).StreetName, Roads.get(road), roadNameRoot);
            }
        }
        isMapGenerated = true;
    }

    /**
     * Adjusts the scaling of all of the nodes and segments so they can display correctly when drawn
     */
    private void adjustScaling() {
        for (String s : Nodes.keySet()) {
            Nodes.get(s).scalePoints(scale, origin);
        }
    }

    @Override
    protected void redraw(Graphics g) {
        for (Node n : articulationPoints) {
            n.isArticulationPoint = true;
        }
        for (String r : Nodes.keySet()) {
            Nodes.get(r).redraw(g, origin, scale);
        }
    }

    @Override
    protected void onPress(MouseEvent e) {
        pressedMouse = new Point(e.getX(), e.getY());
        for (String s : Nodes.keySet()) {
            if (Math.sqrt(Math.pow(Nodes.get(s).nodePoints.x - e.getX(), 2) + Math.pow(Nodes.get(s).nodePoints.y - e.getY(), 2)) <= 4) {
//                dehighlightStreets();
                Nodes.get(s).isSelected = true;
                printStreets(s);
                selectedNodes.offer(Nodes.get(s));
                if (selectedNodes.size() == 2) {
                    ArrayList<Node> sNodes = new ArrayList<>(selectedNodes);
                    aStarRouteSearch(sNodes.get(0), sNodes.get(1));
                } else if (selectedNodes.size() > 2) {
                    selectedNodes.poll();
                }
                for (String n : Nodes.keySet()) {
                    if (!selectedNodes.contains(Nodes.get(n)) && Nodes.get(n).isSelected && !Nodes.get(n).routeSelected) {
                        Nodes.get(n).isSelected = false;
                    }
                }
                break;
            }
        }
    }

    /**
     * Prints all of the information for the supplied node
     */

    public void printStreets(String selectedNode) {
        Set<String> roadIDs = new HashSet<>();
        String intersectionText = "Node ID: " + Nodes.get(selectedNode).nodeID;
        intersectionText += "\nRoads:";
        for (Segment seg : Nodes.get(selectedNode).outgoingEdges) {
            roadIDs.add(seg.roadId);
        }
        Set<String> roadNames = new HashSet<>();
        for (String id : roadIDs) {
            if (Roads.get(id).StreetName != null) {
                roadNames.add(Roads.get(id).StreetName);
            }
        }
        for (String streetName : roadNames) {
            intersectionText += "\n -" + streetName;
        }
        getTextOutputArea().setText(intersectionText);
    }

    /**
     * Recalculates location objects for the 4 corners of the display
     */
    private void recalculateDrawingEdges() {
        topLeft = Location.newFromPoint(new Point(0, 0), origin, scale);
        topRight = Location.newFromPoint(new Point(800, 0), origin, scale);
        bottomLeft = Location.newFromPoint(new Point(0, 800), origin, scale);
        bottomRight = Location.newFromPoint(new Point(800, 800), origin, scale);
    }


    private void prefixSearch() {
        String roadName = getSearchBox().getText();
        List<Road> roadList = roadNameRoot.getAll(roadName, roadNameRoot);
        prefixSearchTextBox(roadList, roadName);
        if (roadList != null) {
            highlightStreets(roadList);
        }
    }

    private void prefixSearchTextBox(List<Road> roads, String roadName) {
        String output = "Matching Roads: ";
        Set<String> roadNames = new HashSet<>();
        if (roads != null) {
            boolean roadFound = false;
            for (Road r : roads) {
                if (r.StreetName.equals(roadName)) {
                    roadNames.add(r.StreetName);
                    roadFound = true;
                }
            }
            if (!roadFound) {
                if (roads.size() > 0) {
                    for (Road r : roads) {
                        roadNames.add(r.StreetName);
                    }
                }
            }
            for (String s : roadNames) {
                output = output + "\n -" + s;
            }
        }
        if (roads == null) {
            output += "\n No Roads Found";
        }
        getTextOutputArea().setText(output);
    }

    /**
     * Highlights the segments and nodes belonging to the supplied roads
     *
     * @param roadList list of road objects that are searched for
     */
    private void highlightStreets(List<Road> roadList) {
        dehighlightStreets();
        for (Road r : roadList) {
            for (Node n : r.roadNodes) {
                for (Segment s : r.roadSegments) {
                    if (Nodes.get(n.nodeID).outgoingEdges.contains(s)) {
                        Nodes.get(n.nodeID).outgoingEdges.get(Nodes.get(n.nodeID).outgoingEdges.indexOf(s)).isSelected = true;
                        Nodes.get(n.nodeID).isSelected = true;
                    }
                }
            }

        }
    }

    /**
     * If the segments and nodes in the graph isSelected field is set to true they are set to false
     */
    private void dehighlightStreets() {
        for (String n : Nodes.keySet()) {
            for (Segment o : Nodes.get(n).outgoingEdges) {
                o.isSelected = false;
                Nodes.get(n).isSelected = false;
            }
        }
    }

    @Override
    protected void onRelease(MouseEvent e) {
        releasedMouse = new Point(e.getX(), e.getY());
        origin = origin.moveBy((pressedMouse.x - releasedMouse.x) / scale, -(pressedMouse.y - releasedMouse.y) / scale);
        adjustScaling();
    }

    @Override
    protected void onLoad(File nodes, File roads, File segments, File polygons) {
        processData(nodes, roads, segments);
    }

    @Override
    protected void onSearch() {
        if (isMapGenerated) {
            prefixSearch();
        }
    }

    private void aStarRouteSearch(Node startNode, Node endNode) {
        // Sets all nodes heuristic
        for (String s : Nodes.keySet()) {
            Nodes.get(s).routeSelected = false;
            Nodes.get(s).deselectRouteRoads();
            Nodes.get(s).h = (Nodes.get(s).nodeOriginLocation.distance(endNode.nodeOriginLocation));
        }
        HashSet<Node> visited = new HashSet<>();
        PriorityQueue<Node> fringe = new PriorityQueue<>(new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                if (o1.getF() > o2.getF()) {
                    return 1;
                } else if (o1.getF() < o2.getF()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        startNode.setG(0);
        boolean endNodeFound = false;
        fringe.add(startNode);
        Node current = startNode;
        while (!fringe.isEmpty() && !endNodeFound) {
            current = fringe.poll();
            visited.add(current);
            if (current == endNode) {
                endNodeFound = true;
            }
            ArrayList<Segment> combinedEdges = new ArrayList<>(current.outgoingEdges);
            combinedEdges.addAll(current.incomingEdges);
            for (Segment S : combinedEdges) {
                Node road = S.endNode;
                if (Roads.get(S.roadId).Oneway > 0) {
                    if (road == current) {
                        continue;
                    }
                }
                if (current == S.endNode) {
                    road = S.startNode;
                } else {
                    road = S.endNode;
                }
                double roadLength = S.segmentLength;
                double g = current.getG() + roadLength;
                double f = g + road.h;
                if ((visited.contains(road)) && (f >= road.getF())) {
                    continue;
                } else if ((!fringe.contains(road)) || (f < road.getF())) {
                    road.setPreviousNode(current);
                    road.setG(g);
                    road.setF(f);
                    if (fringe.contains(road)) {
                        fringe.remove(road);
                    }
                    fringe.offer(road);
                }
            }
        }
        if (current == endNode) {
            String currentRoadName = null;
            double currentRoadDistance = 0;
            ArrayList<String> roadNames = new ArrayList<>();
            ArrayList<Double> distances = new ArrayList<>();
            while (current.previousNode != null) {
                current.routeSelected = true;
                HashSet<Segment> neighbours = new HashSet<>(current.outgoingEdges);
                neighbours.addAll(current.incomingEdges);
                for (Segment s : neighbours) {
                    Node previousNode = s.endNode;
                    if (previousNode == current.previousNode || s.startNode == current.previousNode) {
                        s.routeSelected = true;
                        if (previousNode == current) {
                            previousNode = s.startNode;
                        }
                        if (currentRoadName == null) {
                            currentRoadName = Roads.get(s.roadId).StreetName;
                        }
                        if (!(Roads.get(s.roadId).StreetName.equals(currentRoadName))) {
                            if (currentRoadName != null) {
                                roadNames.add(currentRoadName);
                                distances.add(currentRoadDistance);
                                currentRoadDistance = 0;
                            }
                            currentRoadName = Roads.get(s.roadId).StreetName;
                            currentRoadDistance += s.segmentLength;
                        } else {
                            currentRoadDistance += s.segmentLength;
                        }
                        current = previousNode;
                        break;
                    }
                }
            }
            // If there is no roads from the route to be printed it adds the current road and current distance to be printed (Fixed issues with single street routes)
            if (!roadNames.contains(currentRoadName) && roadNames.isEmpty()) {
                roadNames.add(currentRoadName);
                distances.add(currentRoadDistance);
            }
            String routeTaken = "Calculated Route:";
            // Creates a decimal format to round distances to three decimal places
            DecimalFormat round = new DecimalFormat("#.###");
            round.setRoundingMode(RoundingMode.CEILING);
            for (int i = 0; i < roadNames.size(); i++) {
                if (distances.get(i) < 1) {
                    // If Distance traveled on this road is lower than 1 kilometer the distance unit is changed to meters
                    routeTaken = routeTaken + "\n" + (roadNames.get(i).substring(0, 1).toUpperCase() + roadNames.get(i).substring(1).toLowerCase()) + " Distance: " + round.format(distances.get(i) * 100) + "m";
                } else {
                    // If distance traveled on this road is greater than 1 kilometer it stays as a kilometer
                    routeTaken = routeTaken + "\n" + (roadNames.get(i).substring(0, 1).toUpperCase() + roadNames.get(i).substring(1).toLowerCase()) + " Distance: " + round.format(distances.get(i)) + "km";
                }
            }
            double roadDistanceTotal = 0;
            // Gets sum of all road distances
            for (Double d : distances) {
                roadDistanceTotal += d;
            }
            if (roadDistanceTotal < 1) {
                routeTaken += "\nTotal Distance Traveled: " + round.format(roadDistanceTotal * 100) + "m";
            } else {
                routeTaken += "\nTotal Distance Traveled: " + round.format(roadDistanceTotal) + "km";
            }
            getTextOutputArea().setText(routeTaken);
        }

    }

    /**
     * Depending on m the map either moves to the west, east, north, south and zoom in or out
     *
     * @param m
     */
    @Override
    protected void onMove(Move m) {
        if (m == Move.ZOOM_IN || m == Move.ZOOM_OUT) {
            if (m == Move.ZOOM_IN) {
                scale *= ZOOM_FACTOR;
                double width = topRight.x - topLeft.x;
                double height = bottomLeft.y - topLeft.y;
                double dx = (width - (width / ZOOM_FACTOR)) / 2;
                double dy = (height - (height / ZOOM_FACTOR)) / 2;
                origin = origin.moveBy(dx, dy);
            } else {
                scale /= ZOOM_FACTOR;
                double width = topRight.x - topLeft.x;
                double height = bottomLeft.y - topLeft.y;
                double dx = (width - (width * ZOOM_FACTOR)) / 2;
                double dy = (height - (height * ZOOM_FACTOR)) / 2;
                origin = origin.moveBy(dx, dy);
            }
            adjustScaling();
            redraw();
            recalculateDrawingEdges();
        } else if (m == Move.EAST) {
            origin = origin.moveBy(10 / scale, 0);
            adjustScaling();
        } else if (m == Move.WEST) {
            origin = origin.moveBy(-10 / scale, 0);
            adjustScaling();
        } else if (m == Move.NORTH) {
            origin = origin.moveBy(0, 10 / scale);
            adjustScaling();
        } else if (m == Move.SOUTH) {
            origin = origin.moveBy(0, -10 / scale);
            adjustScaling();
        }

    }

    /**
     * Zooms the map in and out depending on the direction of the scroll wheel
     */
    @Override
    protected void onWheelMovement(MouseWheelEvent e) {
        if (e.getWheelRotation() >= 1) {
            onMove(Move.ZOOM_OUT);
        } else if (e.getWheelRotation() <= -1) {
            onMove(Move.ZOOM_IN);
        }
    }

    /**
     * Deselects all nodes and roads
     */
    protected void deselectAll() {
        for (Node n : selectedRouteNodes) {
            n.deselectRouteRoads();
        }
        for (int i = 0; i < selectedNodes.size() + 1; i++) {
            Node deselect = selectedNodes.poll();
            if (deselect != null) {
                deselect.isSelected = false;
            }
        }
        selectedNodes.clear();
    }

    /**
     * Finds All Articulation Points In The Graph
     */
    public void findAllArticulationPoints() {
        unvisitedNodes = new ArrayList<>(Nodes.values());
        for (Node n : Nodes.values()) {
            n.nodeDepth = Integer.MAX_VALUE;
            n.isArticulationPoint = false;
            n.numberOfSubTrees = 0;
        }
        articulationPoints = new HashSet<>();
        while (!unvisitedNodes.isEmpty()) {
            Random randomIndex = new Random();
            Node root = unvisitedNodes.get(randomIndex.nextInt(unvisitedNodes.size()));
            HashSet<Segment> neighbours = new HashSet<>(root.outgoingEdges);
            neighbours.addAll(root.incomingEdges);
            rootNeighbors = new HashSet<>(neighbours);
            unvisitedNodes.remove(root);
            for (Segment s : neighbours) {
                Node neighbour = s.endNode;
                if (neighbour == root) {
                    neighbour = s.startNode;
                }
                unvisitedNodes.remove(neighbour);
                if (neighbour.nodeDepth == Integer.MAX_VALUE) {
                    recursiveFindArticulationPoints(neighbour, 1, root);
                    neighbour.numberOfSubTrees++;
                }
                if (neighbour.numberOfSubTrees > 0) {
                    articulationPoints.add(neighbour);
                }
            }
        }
        System.out.println(articulationPoints.size());

    }

    public int recursiveFindArticulationPoints(Node node, int depth, Node parent) {
        node.nodeDepth = depth;
        int reachBack = depth;
        HashSet<Segment> neighbours = new HashSet<>(node.outgoingEdges);
        neighbours.addAll(node.incomingEdges);
        for (Segment s : neighbours) {
            Node neighbour = s.endNode;
            if (neighbour == node) {
                neighbour = s.startNode;
            }
            if (neighbour == parent) {
                continue;
            }
            unvisitedNodes.remove(neighbour);
            if (neighbour.nodeDepth < Integer.MAX_VALUE) {
                reachBack = Math.min(neighbour.nodeDepth, reachBack);
            }
            // case 2: indirect alternative path: neighbour is an unvisited child in the same sub-tree
            else {
                // calculate alternative paths of the child, which can also be reached by itself
                int childReach = recursiveFindArticulationPoints(neighbour, depth + 1, node);
                reachBack = Math.min(childReach, reachBack);
                // no alternative path from neighbour to any parent
                if (childReach >= depth) {
                    articulationPoints.add(node);
                }// then add node into APs;
            }
        }
        return reachBack;
    }


    public static void main(String[] args) {
        new AucklandMapping();
    }
}

