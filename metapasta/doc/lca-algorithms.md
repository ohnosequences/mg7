
### Problem
Let $T$ to be a rooted tree and $S = \{ n_1, n_2, \ldots, n_k\}$ a set of nodes of $T$. The *lowest common ancestor* of $S$ (denoted as 
$lca(n_1, n_2, \ldots, n_k)$) is node in $T$ such that:

1.  $lca(n_1, n_2, \ldots, n_k) \preccurlyeq n_i$ for every $n_i \in S$
2.  $lca(n_1, n_2, \ldots, n_k)$ is a maximal node  that satisfy (1).

>  $\preccurlyeq$ is a partial order on nodes induced by depth in tree.

### Algorithms for LCA

#### Lineage intersection
Every node $n \in T$ can be connected by unique path to root that length coincides with depth of $n$. This path is called *lineage* of $n$.
 
There is a very naive algorithm that calculate *LCA* by intersecting lineages. Indeed LCA is a maximal node that contains in all lineages. The complexity of this algorithm is $O(\sum_{n \in S} depth(n))$.

One simple observation can significantly improve performance of this algorithm. Instead calculating all lineages we can calculate it only for the first node $n_1$ and then calculate lineages for rest nodes only partially (until node that is the current intersection). This algorithm is described in Bio4j blog -- http://bio4j.com/blog/2012/02/finding-the-lowest-common-ancestor-of-a-set-of-ncbi-taxonomy-nodes-with-bio4j. 
It's quite hard to estimate complexity of this improved version of the algorithm, because it significantly depends on order of nodes.

#### Optimal complexity
The *LCA* of $S$ can be defined as a root of minimal rooted subtree in $T$ that contains all nodes from $S$. 

> **TODO** prove that LCA that for fining LCA we should traverse this tree
 
> **TODO** describe (on examples?) why the algorithm above is not optimal (it traverse several nodes in this tree twice)

> **TODO** describe algorithm that will mark visited nodes in order to reach optimal performance

> **TODO** describe algorithm that will precalculate depths for all nodes.

  
