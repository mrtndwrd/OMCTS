Prioritized Sweeping: Reinforcement Learning With Less Data and Less Time
=========================================================================

## 1: Temporal Differencing Algorithm:

### 1.1

- *alpha* = learning state parameter 0 `<` *alpha* `<` 1
- *lambda* = memory constant 0 `<=` *lambda* `<=` 1
- *X_i(i_j)* = 1 if i_j == i else 0

	for each visited non-terminal state:
		for each terminal state:
			[I don't know...]

### 1.2

- *succs(i)* = set of successor states of state *i*
- NONTERMS = set of non-terminal states

## 2 Prioritized Sweeping

There's a priority queue of states that need re-evaluation. If a state *i'* 's
absorption probabilities change `>` *Delta*, its predecessors *i* (recursively?)
gain priority *P* in the priority queue, where *P* = *q_i, i'* x *Delta*. 

After each observation i to j, all probability estimates *q_{i.}* are updated

