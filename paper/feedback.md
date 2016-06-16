Feedback to handle
==================

- Reviewer 1:
  - Go over the beginning of sec. 4 again: the explanation is a bit fuzzy
  - Conclusion section lacks ideas to solve the problem O-MCTS has in domains with
    many different sprites
  - 
- Reviewer 2:
  - DQN paragraph can be replaced by either one of these:
    - A. McGovern and R. Sutton, “Macro-actions in reinforcement learning: an
      empirical analysis,” The University of Massachusetts - Amherst, Tech.
      Rep., 1998.
    - R. Balla and A. Fern, “UCT for Tactical Assault Planning in RealTime
      Strategy Games,” in Proceedings of the 21st International Joint Conference
      on Artificial Intelligence., 2009, pp. 40–45.
    - B. Childs, J. Brodeur, and L. Kocsis, “Transpositions and Move Groups in
      Monte Carlo Tree Search,” in Proceedings of IEEE Symposium on
      Computational Intelligence and Games, 2008, pp. 389–395
  - Towards the end of section IV, you mention "the algorithm uses
    interruption." Could you please describe what do you mean with that?
  - Also, just after that, I understand what you mean with a reduced branching
    factor. But I don't think Figure 2 shows that difference.
  - I would highly recommend to re-structure the text explaining the options
    before the experiments section. I felt a bit lost during most of the paper
    without knowing how were the actions in the options taken. Different types
    of options, A* and the mechanism to define if the avatar can move through
    which sprites would be better explained earlier. I also understand that not
    all options are based on A\*, how do the others work? I'd recommend to
    clearly include a list of all options used, and also how are they actions
    calculated internally, in a section before the experimental setup.
  - Something that is not clear to me is how options (and A\*) deal with the
    inherent stochasticity of the games. Or they just ignore this?
  - Also, I understand different options have different lengths, and therefore
    different iterations of O/OL-MCTS could end up at a different depth. But
    you're limiting the depth, so some states are evaluated at a state in the
    middle of an option. If this is right, how does that affect the performance?
    Specially in OL-MCTS, where you'll use the reward to update the value of the
    option. Please, clarify this point.
  - When discussing the results, you say that "In Overload, a sword has to be
    picked up before the avatar can finish the game". This is actually not true,
    the avatar can reach the end going through the marsh as long as it's not
    carrying more than a determined amount of resource.
  - Page 7, end of second column, you consider an hypothesis for MCTS performing
    better than O-MCTS based on having a "very big number" of different sprites.
    I don't think this is the case for all those games. I can see this happening
    in Pacman, and maybe in Plaque Attack, but others like whack-a-mole and jaws
    are in the same range of sprite types that the other games you considered
    before (like overload). Furthermore, the "test" of this hypothesis seems a
    bit inconclusive to me. How many times have the games been run in this 120ms
    setting? As it stands, with different "better" algorithms in those two
    games, it feels it's just noise to me. 
- Reviewer 3:
  - No idea how to improve from his comments
- Reviewer 4:
  - Figure 1 needs a better caption that would explain the stages in detail
    (yes, there is a whole paragraph on it). At least explain what do the
    circles and arrows represent.
  - Figure 3 (and 4) are not really clear. It might be better to show the ratio
    or a difference instead of the two bars (3a, which needs to be labeled as
    subfigure 'a').
  - Saying that O-MCTS outperforms MCTS in most of the games is a bold claim.
    Reading the figure the algorithms have the same ratio in 5 cases, O-MCTS is
    better in 13 games, while MCTS is better in 10 games, so saying most is
    technically still fine, but the difference is very little. For saying almost
    always performs better I would expect it to loose in 1 or 2 games.

