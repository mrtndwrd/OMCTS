## 3.2 Crazy Stone algorithm
$\sigma^2$ is calculated by a special formula when $t < T$ (Where $T$ is a
special threshold for how many times a node is visited, and $t$ is the number of
times a node has been visited.

$$\sigma^2 = \frac{\Sigma\_2 - S\mu^2 + 4P^2}{S + 1},$$
Where:
$P = $ number of points on the board (useless for me)

$\Sigma\_2 = $ the sum of squared values of this node

$\Sigma = $ the sum of the values of this node

$S = $ the number of simulations (total or in this node?)

$\mu = $ the mean value of this node

Which seems correct, except for the $P$, which is not a normal value in the
variance calculation (of course)
