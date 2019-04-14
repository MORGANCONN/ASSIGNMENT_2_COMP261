import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;

public class Node {
    String nodeID;
    boolean isSelected, routeSelected, isArticulationPoint;
    Point nodePoints;
    Location nodeOriginLocation;
    ArrayList<Segment> incomingEdges = new ArrayList<>();
    ArrayList<Segment> outgoingEdges = new ArrayList<>();
    double g, f, h;
    int nodeDepth;
    Node previousNode;
    int numberOfSubTrees = 0;

    public Node(String Id, Point nodePoints, Location nodeOriginLocation) {
        nodeID = Id;
        this.nodePoints = nodePoints;
        this.nodeOriginLocation = nodeOriginLocation;
    }

    /**
     * Calls draw method on all of the outgoing edges and draws the node with the color depending on if the node is selected or not
     */
    public void redraw(Graphics g, Location origin, double scale) {
        for (Segment s : outgoingEdges) {
            s.draw(g, origin, scale);
        }
        if (isSelected || routeSelected) {
            g.setColor(Color.red);
        } else if (isArticulationPoint) {
            g.setColor(Color.GREEN);
        } else {
            g.setColor(Color.blue);
        }
        g.fillOval(nodePoints.x - 2, nodePoints.y - 2, 4, 4);
    }

    /**
     * Scales the point of the node depending on supplied scale and origin
     */
    public void scalePoints(double scale, Location origin) {
        nodePoints = nodeOriginLocation.asPoint(origin, scale);
    }

    public double getG() {
        return g;
    }

    /**
     * Gets the remaining distance heuristic of the current node
     *
     * @return
     */
    public double getF() {
        return f;
    }

    /**
     * Sets the distance traveled to the supplied double
     *
     * @param distanceTraveled the double that distanceTraveled to
     */
    public void setG(double distanceTraveled) {
        this.f = distanceTraveled;
    }

    /**
     * Sets the remaining distance heuristic of the current node
     *
     * @param g the double that remainingDistanceHeuristic is set to
     */
    public void setF(double g) {
        this.g = g;
    }

    public void setPreviousNode(Node p) {
        this.previousNode = p;
    }


    public void deselectRouteRoads() {
        if (routeSelected) {
            routeSelected = false;
            for (Segment S : outgoingEdges) {
                if (S.routeSelected) {
                    S.routeSelected = false;
                }
            }
        }
    }

}
