# About #

This is my simple project I've been working on called Godot. The name Godot comes from Samuel Beckett's play *En attendant Godot* (English: *Waiting For Godot*) because I find myself waiting indefinite periods of time for Godot to play a move.

I made Godot both to practice programming and for a science fair project, but most of all I made Godot to have fun.

A large amount of my code is based off of two sources: Stef Luijten's didactic *Winglet* (which you can find at [here](http://www.sluijten.com/winglet)) and Alberto Ruibal's *Carballo* (whose source is [here](https://github.com/albertoruibal/carballo)).

At the moment, Godot currently uses the following techniques:
* Bitboards (including Magic Bitboards)
* Zobrist hashing
* Null-move pruning
* Iterative deepening
* Quiescent searching
* Static exchange evaluation (SEE)
* Alpha-Beta
* Principal variation searching (PVS)
* History heuristics

# How to use it #

To download Godot, run

     git clone git://github.com/ulyssecarion/godot.git
     
You can then ask Godot to search a position by setting up a position, choosing a search technique, depth, and evaluator, and starting the search, i.e.

```java
Board b = new Board("8/5p1p/2p2K1k/2P3RB/6P1/8/8/8 w - - 0 1"); // the position in Forsyth–Edwards Notation ("FEN")
NullIterQSEE.setEvaluator(new CompleteEvaluator());             // the search technique and evaluator
NullIterQSEE.setDepth(6);                                       // the search depth
System.out.println(b);                                          // output the board in human-friendly form
NullIterQSEE.getBestMove(b);                                    // search (produces outputs if set to verbose)
```

Which, if the search has the `VERBOSE` setting set to true, outputs:

```
     a   b   c   d   e   f   g   h
   +---+---+---+---+---+---+---+---+
 8 |   |   |   |   |   |   |   |   | 8
   +---+---+---+---+---+---+---+---+
 7 |   |   |   |   |   | p |   | p | 7
   +---+---+---+---+---+---+---+---+
 6 |   |   | p |   |   | K |   | k | 6
   +---+---+---+---+---+---+---+---+
 5 |   |   | P |   |   |   | R | B | 5
   +---+---+---+---+---+---+---+---+
 4 |   |   |   |   |   |   | P |   | 4
   +---+---+---+---+---+---+---+---+
 3 |   |   |   |   |   |   |   |   | 3
   +---+---+---+---+---+---+---+---+
 2 |   |   |   |   |   |   |   |   | 2
   +---+---+---+---+---+---+---+---+
 1 |   |   |   |   |   |   |   |   | 1
   +---+---+---+---+---+---+---+---+
     a   b   c   d   e   f   g   h

White to move: true
White: O-O: false -- O-O-O: false
Black: O-O: false -- O-O-O: false
En Passant: -1 (-)
50 move rule: 0
Move number: 1

(1) 0.043s (Kf6-f5) -- 16 nodes evaluated.
(2) 0.047s (Kf6-f5) -- 51 nodes evaluated.
(3) 0.107s (Bh5-g6) -- 193 nodes evaluated.
(4) 0.114s (Bh5-g6) -- 347 nodes evaluated.
(5) 0.124s (Rg5-d5) -- 1235 nodes evaluated.
1235 positions evaluated.
```
     
\* A note about compiling: Godot relies on Apache's ArrayUtils, and GodotBot relies on Selenium. If you're using Eclipse, you may need to add ArrayUtils and Selenium to your project's referenced libraries.

\** GodotBot is not meant to be for abuse. I put this code here for demonstration, not as a means to help you cheat.
