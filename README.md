# IDTICT2
This is IDT team ICT-2's codebase. Here follows a description of our algorithms.

Permissions:
First, the tester starts another process which installs a security manager. This security manager keeps a list of the
java permissions of the jar and returns these to the tester. These are useful in determining if a jar under test may
be doing potentially dangerous things.

Exceptions:
The tester reads the standard error of the process and uses a regex to separate exceptions from other messages sent
to standard error. These standard error messages are then used to identify unique errors with the software.

We used the iterative parameter building approach supplied by IDT, and needed only to determine which tests would
best determine all possible errors that the software could run into. We guessed at this by implementing a Monte Carlo
decision tree. UCB1 was utilized to find optimal paths in the tree.
