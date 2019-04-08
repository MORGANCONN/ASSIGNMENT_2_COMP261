public class aStarSearchNode {
    private Node currentNode;
    private Node previousNode;
    private boolean isVisited = false;
    private double g;
    private double f;

    public aStarSearchNode(Node cNode, Node pNode, double g, double f) {
        currentNode = cNode;
        previousNode = pNode;
        this.f = f;
        this.g = g;
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
    public double getG() {
        return g;
    }

    /**
     * Gets the remaining distance heuristic of the current node
     * @return
     */
    public double getF(){
        return f;
    }

    /**
     *  Sets the distance traveled to the supplied double
     * @param distanceTraveled the double that distanceTraveled to
     */
    public void setDistanceTraveled(double distanceTraveled){
        this.g = distanceTraveled;
    }

    /**
     *  Sets the remaining distance heuristic of the current node
     * @param remainingDistanceHeuristic the double that remainingDistanceHeuristic is set to
     */
    public void setG(double remainingDistanceHeuristic){
        this.g = remainingDistanceHeuristic;
    }

    public void visitNode(){
        isVisited = true;
    }
}
