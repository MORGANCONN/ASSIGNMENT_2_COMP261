import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
    private TrieNode roadNameRoot;
    private double scale = 8;


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
                    Roads.put(currentRoadId, new Road(currentRoadId, Integer.parseInt(splittedCurLine[1]), splittedCurLine[2], splittedCurLine[3], Boolean.parseBoolean(splittedCurLine[4]), Integer.parseInt(splittedCurLine[5]), Integer.parseInt(splittedCurLine[6]), Boolean.parseBoolean(splittedCurLine[7]), Boolean.parseBoolean(splittedCurLine[8]), Boolean.parseBoolean(splittedCurLine[9])));
                }
                Roads.get(currentRoadId).Type = Integer.parseInt(splittedCurLine[1]);
                Roads.get(currentRoadId).StreetName = splittedCurLine[2];
                Roads.get(currentRoadId).City = splittedCurLine[3];
                Roads.get(currentRoadId).Oneway = Boolean.parseBoolean(splittedCurLine[4]);
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
        for (String r : Nodes.keySet()) {
            Nodes.get(r).redraw(g, origin, scale);
        }
    }

    @Override
    protected void onPress(MouseEvent e) {
        pressedMouse = new Point(e.getX(), e.getY());
        for (String s : Nodes.keySet()) {
            if (Math.sqrt(Math.pow(Nodes.get(s).nodePoints.x - e.getX(), 2) + Math.pow(Nodes.get(s).nodePoints.y - e.getY(), 2)) <= 2) {
                dehighlightStreets();
                Nodes.get(s).isSelected ^= true;
                Set<String> roadIDs = new HashSet<>();
                String intersectionText = "Node ID: " + Nodes.get(s).nodeID;
                intersectionText += "\nRoads:";
                for (Segment seg : Nodes.get(s).outgoingEdges) {
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
                for (String n : Nodes.keySet()) {
                    if (n != s && Nodes.get(n).isSelected) {
                        Nodes.get(n).isSelected ^= true;
                    }
                }
            }
        }
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


    public static void main(String[] args) {
        new AucklandMapping();
    }
}

