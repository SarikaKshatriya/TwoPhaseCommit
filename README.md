# TwoPhaseCommit
Coordinator-Participant Using JavaSwing

A 2PC protocol System has two java classes one for server side socket connections and another for client side. All the clients are getting registered before starting. When a user sends a message then it sends the message in HTTP format, the server then broadcasts the message to all the online users using GET method.  All users gets notified when new user logs on or logs off. The date is in HTTP format and the application is independent of the browser. One of the client works as a Coordinator and other 3 works as a Participants. Coordinator sends a arbitrary string to the participants and the participants votes abort or commit. The communication and timeout between users is according to 2PC protocol.

Following are the steps to execute programs:
1.Run twoPCServer. It display’s name of the application “2PC Protocol”, Start button  and Stop button ,one textarea to display messages with GET/POST and another to display online users. 

2.Click on Start button, it starts the server and allows other clients to send and receive requests to it. The Server window displays message server started with current date. This helps to keep track of messages according to date. 

3.Run twoPCClient. The Client window shows 6 buttons connect, disconnect, VoteRequest, Vote, Abort and Commit button to connect to the chat room, disconnect the chat room, VoteRequest to participants, Vote by the Coordinator, Abort and commit the request respectively. This also asks to enter username. It accepts only alphabets/characters as a username. If any name except characters is entered, it shows error message in textarea and asks to enter valid username containing only characters. 

4. If username is valid then after clicking on “Connect” button. The client gets connected to 2PC System. It display’s connected message in servers and clients both windows. The user’s name is now a title for that client window. If user is Coordinator it disables abort and commit boutton,else it considers it’s a participant and disables voteRequest and Vote button.
The participant also shows a arbitrary String saved in a text file. When participant receives GLOBAL_COMMIT, it saves the arbitrary string in to a text file. 3 text files are used as we have 3 participants. 
If its Coordinator then the LOCAL_LOG is set to START2PC,else INIT.

5.To connect more than one client, in this lab the requirement is of four users then run twoPCClient to get more users. If four users (A, B,C,D) are registered and connected. 
The Server window shows:
a. Number of threads running (in title).
b. Number of users online/offline in Online Users textarea.
c. All the message from all the users and connection messages after connection. Also, disconnect messages after disconnecting the client.
d. The messages are in HTTP format.
e. Current date in HTTP date format.
The Client windows shows:
a. Name of the user on title.
b. LOCAL_LOG state.
c. Connected/disconnected messages. And displays messages from all users including self.
The expected output is in the following screenshot:
6.The participant waits for 20 seconds to get vote request from user. If it didn’t get voteRequest  in 20 seconds from coordinator, then it sets LOCAL_LOG to LOCAL_ABORT

7. If the Coordinator sends a VoteRequest then it waits for 10 seconds for participants to wait, if participants votes in 10 seconds then Coordinator can vote. If all participants votes commit, then it sends GLOBAL_COMMIT or sends GLOBAL_ABORT message.  The window also shows the remaining time.
 
8.If any participant did not vote in 10 seconds then coordinator sends GLOBAL_ABORT and all participant changes the LOCAL_LOG accordingly.
 
9.If participant votes commit but did not receive any message from coordinator then it sends decision request to all other participants. If LOCAL_LOG of any participant is GLOBAL_COMMIT then it sends GLOBAL_COMMIT to all else GLOBAL_ABORT. 
 
10.At any point Coordinator crashes then, it sends message to all users and LOCALO_LOG sets to LOCAL_ABORT.
The server shows all the messages in HTTP format.

 




