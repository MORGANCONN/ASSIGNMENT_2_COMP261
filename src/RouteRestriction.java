public class RouteRestriction {
    public Segment road1, road2;
    public String nodeID1, nodeID2, intersection;
    public RouteRestriction(String nodeID1, Segment road1, String nodeID, Segment road2, String nodeID2 ){
        this.road1 = road1;
        this.road2 = road2;
        this.nodeID1 = nodeID1;
        this.nodeID2 = nodeID2;
        intersection = nodeID;
    }
}
