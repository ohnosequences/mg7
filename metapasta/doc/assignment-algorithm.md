### MEGAN-like taxonomy node assignment algorithm


For each read:

1. filter all hits with bitscore below the threshold **s_0**
2. find the best bitscore value S (in the set of BLAST HSPs corresponding to hits of the read)
3. filter all reads with bitscore below p * S (where **p** is fixed coefficient, e.g. 0.9)
4. now there are two cases:
    * The rest of the hits (their taxa) form a line in the taxonomy tree. In this case we should choose the most specific tax id as the final assignment for that read
    * in the other cases we should calculate (sensu stricto) Lowest Common Ancestor (LCA)
    
5. (not for now, questionable) discard leafs with small assignments. 


In this approach the value used for evaluating the similarity is the **bitscore** that is a value that increases when similarity is higher and depends a lot on the length of the HSP
