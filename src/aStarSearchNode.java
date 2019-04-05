public class aStarSearchNode {
    private Node currentNode;
    private Node previousNode;
    private boolean isVisited = false;
    private double distanceTraveled;
    private double remainingDistanceHeuristic;

    public aStarSearchNode(Node cNode, Node pNode, double distanceTraveled, double remainingDistanceHeuristic) {
        currentNode = cNode;
        previousNode = pNode;
        this.remainingDistanceHeuristic = remainingDistanceHeuristic;
        this.distanceTraveled = distanceTraveled;
    }

    /**
     * Returns the current node
     * @return
     */
     public Node getCurrentNode(){
        return currentNode;
     }
    /**
     * Gets the current node's distance traveled
     * @return
     */
    public double getDistanceTraveled() {
        return distanceTraveled;
    }

    /**
     * Gets the remaining distance heuristic of the current node
     * @return
     */
    public double getremainingDistanceHeuristic(){
        return remainingDistanceHeuristic;
    }

    /**
     *  Sets the distance traveled to the supplied double
     * @param distanceTraveled the double that distanceTraveled to
     */
    public void setDistanceTraveled(double distanceTraveled){
        this.distanceTraveled = distanceTraveled;
    }

    /**
     *  Sets the remaining distance heuristic of the current node
     * @param remainingDistanceHeuristic the double that remainingDistanceHeuristic is set to
     */
    public void setRemainingDistanceHeuristic(double remainingDistanceHeuristic){
        this.remainingDistanceHeuristic = remainingDistanceHeuristic;
    }

    public void visitNode(){
        isVisited = true;
    }
}
