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
     
Once you've compiled the source code, you can play against Godot with the command

     java -cp ./bin/gui BoardMain
     
If you want to run the bot, be sure to update the final variables `USERNAME` and `PASSWORD` in the file `./src/bot/GodotBot.java`. After having compiled, you can launch the bot by running

     java -cp ./bin/bot GodotBot
     
\* A note about compiling: Godot relies on Apache's ArrayUtils, and GodotBot relies on Selenium. If you're using Eclipse, you may need to add ArrayUtils and Selenium to your project's referenced libraries.

\** GodotBot is not meant to be for abuse. I put this code here for demonstration, not as a means to help you cheat.
