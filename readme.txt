
How to compile:
	"javac Master.java"

How to run:
	"java Master"

Description of approach:
	Master thread spawns a number of child threads according to the input file, passing a reference to a CyclicBarrier to each of them. Once each thread is started, they call await() on the CyclicBarrier. Master keeps track of how many threads are currently waiting on the barrier. Once the number of waiting threads matches the total number of threads, Master also calls await() on the CyclicBarrier, triggering a reset and allowing all threads to execute one iteration of the HS algorithm. Once they are done, they once again call await() on the CyclicBarrier and the entire process begins again. 

	Once each process has found out who the leader is, they increase the atomic counter in the Synchronizer object and terminate. Once the synchronizer object's counter reaches the number of threads, Master knows that all threads are complete and terminates.