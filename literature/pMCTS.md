## 3.2 Progressive Pruning:
When the number of games $n\_p$ in a node $p$ equals the threshold $T$,
progressive unpruning "prunes" most of the children. The children which are not
pruned from the beginning are the $k\_{init}$ children with the highest heuristic
values (in my case: mean reward?). $k = 5$ for Mango
