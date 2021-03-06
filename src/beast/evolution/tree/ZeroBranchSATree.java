package beast.evolution.tree;

import beast.core.StateNodeInitialiser;

import java.io.PrintStream;
import java.util.List;

/**
 *@author Alexandra Gavryushkina
 */

/**
 * @deprecated Use Tree instead. Starting from v.1.1.1 Tree class supports sampled ancestors and
 * all the classes that contain ZeroBranch in their names (and some others) are replaced by similar classes.
 */
@Deprecated
public class ZeroBranchSATree extends Tree {

    public boolean logWithZeroBranches=false;

    @Override
    public void initAndValidate() {
        if (m_initial.get() != null && !(this instanceof StateNodeInitialiser)) {
            final Tree other = m_initial.get();
            root = other.root.copy();
            nodeCount = other.nodeCount;
            internalNodeCount = other.internalNodeCount;
            leafNodeCount = other.leafNodeCount;
        }

        if (nodeCount < 0) {
            if (m_taxonset.get() != null) {
                // make a caterpillar
                final List<String> sTaxa = m_taxonset.get().asStringList();
                Node left = newNode();
                left.labelNr = 0;
                left.height = 0;
                left.setID(sTaxa.get(0));
                for (int i = 1; i < sTaxa.size(); i++) {
                    Node right = newNode();
                    right.labelNr = i;
                    right.height = 0;
                    right.setID(sTaxa.get(i));
                    Node parent = newNode();
                    parent.labelNr = sTaxa.size() + i - 1;
                    parent.height = i;
                    left.parent = parent;
                    parent.setLeft(left);
                    right.parent = parent;
                    parent.setRight(right);
                    left = parent;
                }
                root = left;
                leafNodeCount = sTaxa.size();
                nodeCount = leafNodeCount * 2 - 1;
                internalNodeCount = leafNodeCount - 1;

            } else {
                // make dummy tree with a single root node
                root = newNode();
                root.labelNr = 0;
                root.height = 0;
                root.m_tree = this;
                nodeCount = 1;
                internalNodeCount = 0;
                leafNodeCount = 1;
            }
        }

        if (nodeCount >= 0) {
            initArrays();
        }

        if (leafNodeCount < 0) {
            leafNodeCount = getLeafNodeCount();
        }
        if (internalNodeCount < 0) {
            internalNodeCount = getInternalNodeCount();
        }


        processTraits(m_traitList.get());

        // Ensure tree is compatible with time trait.
        if (timeTraitSet != null)
            adjustTreeNodeHeights(root);


    }

    @Override
    public int scale(double fScale) {
        return scale(fScale,false);
    }

    public int scale(double fScale, boolean scaleSNodes) {
        ((ZeroBranchSANode)root).scale(fScale, scaleSNodes);
        if (scaleSNodes) {
            return getNodeCount() - getDirectAncestorNodeCount();
        } else {
            return getInternalNodeCount() - getDirectAncestorNodeCount();
        }
    }

    public int getDirectAncestorNodeCount() {
        int directAncestorNodeCount = 0;
        for (int i = 0; i < leafNodeCount; i++) {
            if (((ZeroBranchSANode)this.getNode(i)).isDirectAncestor()) {
                directAncestorNodeCount += 1;
            }
        }
        return directAncestorNodeCount;
    }

    @Override
    public void log(long nSample, PrintStream out) {
        ZeroBranchSATree tree = (ZeroBranchSATree) getCurrent();
        out.print("tree STATE_" + nSample + " = ");
        final int[] dummy = new int[1];
        final String sNewick;
        if (logWithZeroBranches) {
           sNewick = ((ZeroBranchSANode)tree.getRoot()).toSortedNewickWithZeroBranches(dummy);
        }  else {
           sNewick = tree.getRoot().toSortedNewick(dummy);
        }
        out.print(sNewick);
        out.print(";");
    }

    public double scaleAndSlide(double scale) throws Exception {
        int[] splitCount = new int[]{0};
        ((ZeroBranchSANode)root).scaleAndSlide(scale, splitCount);
        System.out.println("tree becomes " + root.toShortNewick(false));
        double scaleContribution = (getInternalNodeCount() - getDirectAncestorNodeCount())* Math.log(scale);
        if (scale > 1.0) {
            return scaleContribution - splitCount[0]*Math.log(2.0);
        } else {
            return scaleContribution + splitCount[0]*Math.log(2.0);
        }
    }

}
