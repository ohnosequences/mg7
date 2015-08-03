#OUTDATED!!!!!!!

###See:
- https://github.com/ohnosequences/metapasta/blob/master/doc/assignment-algorithm.md


________________________________________________________________________________________________________________________
### problem
for given read **r** and set of **hits** of that read to 16S database find a **taxonomy node** such that:
* more than **percent** GIs of hits maps to children (direct or not) nodes of **node**
* **node** must have maximal hits percentage and minimal rank

### algorithm proposal

The algorithm in sense does following: from sets of all ancestors of hitted nodes it find lowest one that have accumulated assignment ratio more than fixed **percent**.

1. for every hit:
  * retrieve tax id from gi (drop it if there is no such gi in the database)
  * terive the list of ancestors og this id  
  * to every tax id from the list assign 1 (or increment assigned value by one)
2. find set of taxonomy nodes with minimal assigned value that greater than `size(Hits) / 100 * p` in a lot of cases (for example if all hits refer to the same node) this set will contain several elements
3. find the lowest (in terms of the taxonomy tree) node from set that we formed in `2`

**proposition** if `p > 50` then we will always get unique node in step `3`
