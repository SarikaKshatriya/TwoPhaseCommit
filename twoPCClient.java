/*
 * This is 2 Phase Commit Protocol Client Program
 * one client works as a Coordinator
 * and other 3 clients works as a Participants*/
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class twoPCClient extends JFrame 
{
	/*Declare a timer variable to display countdown */
	javax.swing.Timer counter,counterVR;
	//set count down counter as 10 for participant to vote
	int countDown=10;
	int countDownVR=20;
	/*thread instance of a thread class*/
	 Thread thread = new Thread(new ListenServer());
	 /*declare a local_log variable which keeps track of a current state of a participant*/
	 String local_log="";
	 /*declare a voteCount variable which keeps track of a number of  participants 
	  * who voted either abort or commit*/
	 int voteCount=0;
	 /*declare a voteCount variable which keeps track of a number of  participants 
	  * who voted abort */
	 int voteAbort=0;
	 /*declare a voteCount variable which keeps track of a number of  participants 
	  * who voted commit */
	 int voteCommit=0;
	/*declare names of files for 3 participants which will store a arbitary string to the 
	 * file
	 * file1 is for participant1
	 * file2 is for participant2
	 * and file3 is for participant3*/
	 String file1="participant1.txt";
	 String file2="participant2.txt";
	 String file3="participant3.txt";
	 Boolean participantTimeout=false;
	 /*declare globalCommitFlag varible to indicate coordinator has voted global_commit
	   * to all other participants*/ 
	 Boolean globalCommitFlag=false;
	 /*declare toolkit variable
	  * if coordinator/participants time outs 
	  * system sounds beep*/
	 Toolkit toolkit;
	  /*declare isCoordinatorVote varible to indicate coordinator has voted 
	   * to all other participants*/ 
	  Boolean isCoordinatorVote=false;
	  /*declare voteRequest varible to indicate coordinator has sent a vote request
	   * to all other participants*/
	  Boolean voteRequest=false;
	/*declare timer variables
	 * timer for coordinators use
	 * timer1 for participants use
	 * and timerp for participants decision use*/
	  Timer timer;
	  Timer timer1;
	  Timer timerp;
	 static long endTime=System.currentTimeMillis()+90000;
	 static long startTime=0;
	 /*declare a variable filename to save filename and use it to display string/file
	  * content when a participant connects to server*/
	 String filename="participant1.txt";
	// Create the textarea for message display and state area for local_log display 
	
	static JTextArea textarea=new JTextArea();
	//state textarea displays the state of a client as a Local_log
	static JTextArea state=new JTextArea();
	//declare a string username to save a name of a current user
    String username;
    //declare a variable to save arbString which will save later to 3 files
    String arbString="";
    ArrayList participants=new ArrayList();
    ArrayList<String> users = new ArrayList();
    Boolean isConnected = false;//to check whether the user is connected or not
    long difference=0; //to keep time difference
    
    
    SimpleDateFormat dispTime=new SimpleDateFormat("mm:ss");
    /*create instances for socket,bufferreader,printwriter and date*/
    Socket socket;
    BufferedReader in;
    PrintWriter out;
    Date date1,date2;
    JFrame frame = new JFrame("Participant");
    /*declare a label to display countdown*/
    JLabel counterLabel=new JLabel("        ");//label for message
    /*read data from the servers */
  public class ListenServer implements Runnable
      {
	    @Override
        public void run() 
        {
     	   /* line is a string array to take input line and usern is a variable to store name of a client
     	    * who has sent the message*/
            String[] line;
            String msg="";
            String[] splitMsg;
            String voteMsg="";
           try 
            {
                String time1=dispTime.format(System.currentTimeMillis());
                //parse the time to the date format 
                date1=dispTime.parse(time1);
                /* check for the whole message line and split it with : the first element is username
            	 * the second is connect/disconnect message and if the client clicks on connect button it sends connect message 
            	 * and if client hits disconnect button it sends disconnect message
            	 * all the messages are splitted with :,if the message is to broadcast all current users then :Chat is used at the end
            	 * read the inputstream*/
                while ((msg = in.readLine()) != null) 
                {
                	//split the line with :
                	line = msg.split(":");
                	String usern=line[1];//get username
                	/* if the request is voteRequest then check if its coming from coordinator 
                	 * if so,then take the arbitary string into voteMsg variable
                	 * to use for writing string to file later*/
                	 if (line[11].equals("VoteRequest"))
                	 { 
                		 voteRequest=true;
                		 if(usern.equalsIgnoreCase("Coordinator"))
                		 {
                			 arbString=line[2];
                			 //the voterequest msg comes with arbitary string,split it and save in a voteMsg String
                			 splitMsg=arbString.split("-");
                			 voteMsg=splitMsg[0];
                       	}
                	 }
                      if (line[11].equals("Connect"))
                     {
                          /*if the request is connect then add the username to arraylist users 
                           */
                       	 users.add(usern);
                       //	 textarea.append(usern + "  has connected.\n");
                       	
                     } 
                     else if (line[11].equals("Disconnect")) 
                     {
                       	 //textarea.append(usern + "  has disconnected.\n");
                     } 
                                      
                      if (line[11].equals("Timeout"))
                      {
                     	 /*if the request is timeout,that is coordinator is timeout
                     	  *  then change local_log as GLOBAL_ABORT*/
                       		local_log="GLOBAL_ABORT";
                       		state.setText("LOCAL_LOG:GLOBAL_ABORT");
                       	
                      } 
                      if (line[11].equals("Crashed"))
                      {
                    	  /*if the coordinator or participant crashes 
                      	  *  then change local_log as LOCAL_ABORT*/
                    	  
                       		local_log="LOCAL_ABORT";
                       		state.setText("LOCAL_LOG:LOCAL_ABORT");
                       		textarea.append(usern+" - LOCAL_ABORT");
                       	
                      }
                      if (line[11].equals("voteRequestTimeout"))
                      {
                    	  /*if the request is timeout,that is participant is waiting for a coordinator to send voterequest
                      	  *  then change local_log as LOCAL_ABORT*/
                    	  
                       		local_log="LOCAL_ABORT";
                       		state.setText("LOCAL_LOG:LOCAL_ABORT");
                       		textarea.append(usern+" - LOCAL_ABORT");
                       	
                      }
                      if (line[11].equals("VoteRequest"))
                      {
                    	  /*if the request is voteRequest,it means coordinator has sent a voterequest
                       	  *  and started countdown
                       	  *  also set coordinatorVote to false as this can be a new arbitary string coming from 
                       	  *  coordinator */
                            	  
                    	  isCoordinatorVote=false;
                    	  textarea.append("Vote in 10 Seconds\n");
                    	  countDown=9;
                    	  counter = new javax.swing.Timer(1000, new ActionListener() {
                              @Override
                              public void actionPerformed(ActionEvent e) {
                                  counterLabel.setText("Vote in "+String.valueOf(countDown)+" Sec.");
                                  countDown--;
                                  if (countDown == -1) {
                                      //timer.removeActionListener(this);
                                	  counter.stop();
                                  }
                              }
                          });
                    	  counter.start();
                    	//set voteRequest to true indicating coordinator has send a vote request
	    	    			voteRequest=true;
                      }
                      if (line[11].equals("DECISION_REQUEST"))
                      {
                    	/*when a participant sends a decision request to other participants then 
                    	 * give 10 seconds time to all the participants to response to a decision request or
                    	 * send a decision*/
                    	
                    		  
                          	 timerp = new Timer();
     	                     TimerTask tt=new decisionTimeOut();
     	                     timerp.schedule(tt, 10 * 1000);
                         	               	
                      }
                      /*if the last element is Abort then increment voteAbort and voteCount
                       * if voteCount is 3 or more that is all 3 participant voted as abort then
                       * set local_log as global_abort,as coordinator sends global_abort
                       * that is 4th vote is coming from coordinator which is abort
                       * also set local_log to global_abort and display so in state 
                       * textarea also*/
                     if (line[11].equals("Abort"))
                     {
                    	 voteAbort++;
                      	 voteCount++;
                      	 
                       } 
                     /*if coordinator aborts then change local log to GLOBAL_ABORT
                      * it means coordinator voted so make isCoordinatorVote flag true*/
                     if (line[11].equals("CoordinatorAbort"))
                     {
                    	 isCoordinatorVote=true;
                      		local_log="GLOBAL_ABORT";
                      		state.setText("LOCAL_LOG:GLOBAL_ABORT");
                      	
                       }
                     /*if coordinator aborts then change local log to GLOBAL_COMMIT
                      * it means coordinator voted so make isCoordinatorVote flag true*/
                     if (line[11].equals("Commit"))
                     {
                    	 voteCommit++;
                       	 voteCount++;
                      	 
                      }
                     if (line[11].equals("TimeoutAbort"))
                     {
                    	 isCoordinatorVote=true;
                      		local_log="GLOBAL_ABORT";
                      		state.setText("LOCAL_LOG:GLOBAL_ABORT");
                      	
                       }
                     /*if the last element is commit then increment votecommit and voteCount
                      * if voteCount is 3 or more that is all 3 participant voted as commit then
                      * set local_log as global_commit,as coordinator sends global_abort
                      * that is 4th vote is coming from coordinator which is commit
                      * when globalCommitFlag is true means coordinator send a vote
                      * also set local_log to global_commit and display so in state 
                      * textarea also*/
                     if (line[11].equals("CoordinatorCommit"))
                     {
                    	 isCoordinatorVote=true;
                          		local_log="GLOBAL_COMMIT";
                           		state.setText("LOCAL_LOG:GLOBAL_COMMIT");
                          		/*save the arbitary string in a non-volatile memory that is file
                          		 * as we have 3 participant indicating 3 different machines then save the arbitary string to 
                          		 * 3 different files*/
                          		File fp1=new File(file1);
                          		File fp2=new File(file2);
                          		File fp3=new File(file3);
                          		try{
                          			/*open filewriter instance to write on a file,it saves all the arbitary string into the file  
                          			 * */
                          			FileWriter f1 = new FileWriter(file1);
                          			f1.flush();
                          			FileWriter f2 = new FileWriter(file2);
                          			f2.flush();
                          			FileWriter f3 = new FileWriter(file3);
                          			f3.flush();
                          			
                          	        f1.write(voteMsg);
                          	        f2.write(voteMsg);
                          	        f3.write(voteMsg);
                          	        //close the filewriter
                          	        f1.close();
                          	        f2.close();
                          	        f3.close();
                          		    }
                          		    catch(Exception e){	e.printStackTrace();}
                          	
								
                          
                     }//if
                     else
                     {
                    	/*the message coming from server is in HTTP format
                    	 * then extract the message only and display it on users window*/
                    	 textarea.append(line[1]+line[2]+"\n");
                    	 textarea.setCaretPosition(textarea.getDocument().getLength());
                    	 
                     }
                }//while
               }//try
               catch(Exception e) {e.printStackTrace(); }
            }//run
    }

//create the GUI and display it
    private void createGUI() {
		/*Set the layout as null as using setbounds to set complonents
		 * set size and close the frame when clicked on exit*/
		 frame.setLayout(null);
         	 frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 
        	JLabel userlabel=new JLabel("Enter Username");//label for username input
		JLabel inputlabel=new JLabel("Enter Message");//label for message
		
		/*Create the  buttons
		 * Send button to send messegs 
		 * connect to connect to chat room
		 * disconnect to logout from the chat room
		 * abort to vote abort
		 * commit to vote commit
		 * vote is only for the coordinator who votes after all 3 participants votes */
		JButton send=new JButton("VoteRequest");
		JButton connect=new JButton("Connect");
		JButton disconnect=new JButton("Disconnect");
		JButton abort=new JButton("Abort");
		JButton commit=new JButton("Commit");
		JButton vote=new JButton("Vote");
		//usertext is to input username from user and inputfield is to input messages
	    JTextField usertext=new JTextField();
	    //set textfield as editable
	    usertext.setEditable(true);
	    JTextField inputfield=new JTextField();
	    //set textfield as editable
	    inputfield.setEditable(true);
	    //set all elements in a frame
		
	    counterLabel.setBounds(10,10,100,10);
		userlabel.setBounds(10,40,100,20);
		usertext.setBounds(120,40,120,30);
		state.setBounds(250,40,230,50);
        inputlabel.setBounds(10,70,150,20);
        inputfield.setBounds(120,70,120,30); 
        textarea.setBounds(10,110,480,270);
		
        abort.setBounds(10,390,150,30);
        commit.setBounds(180,390,150,30);
        vote.setBounds(350,390,150,30);
		send.setBounds(10, 430, 150, 30);
        connect.setBounds(180, 430, 150, 30);
        disconnect.setBounds(350, 430, 150, 30);
		//provide scrolling capability to textarea
		JScrollPane scrollPane=new JScrollPane(textarea);
		//set textareas as not editable
	    textarea.setEditable(false);
	    state.setEditable(false);
	    //Create textfield to write message
	    
	    //exit on close
	   frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    //put created components in the frame.
	   frame.add(counterLabel);
	    frame.add(userlabel);//add userlabel to the frame
	    frame.add(usertext);//add usertext to the frame
	    frame.add(state);
	    frame.add(inputlabel);//add inputlabel to the frame
	    frame.add(inputfield);//add inputfield to the frame
	    frame.add(textarea);//add textarea to the frame
	   // scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	    frame.add(abort);//add abort button to the frame
	    frame.add(commit);//add commit button to the frame
	    frame.add(vote);//add vote button to the frame
	    frame.add(send);//add send button to the frame
	    frame.add(connect);//add connect button to the frame
	    frame.add(disconnect);//add disconnect button to the frame
	    
 	   /* add listeners
	    set action to start button
	     
	     When user clicks on send button after adding message then client converts that message to HTTP format */
	    send.addActionListener(new ActionListener(){
	    	public void actionPerformed(ActionEvent action)
	    	{
	    		
	    		try {
		    		
	                	/* user-agent displays name of the browsers,this list shows this application is independent 
	                	 * of a browser.
	                	 *  display the host name in this case host is  "localhost"*/
	                	String useragent=" User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36";
	                	String host=" Host:"+socket.getInetAddress().getHostName();//get host name
	        
	                	/*get length of the original messsage*/
	                	String ContentLength=" Content-length:"+inputfield.getText().length();
	        
	                	 * as this is simple chat room system,independent of the browser the content type is text/plain*/
	                	String contentType=" Conent-Type:text/plain";
	    	
	    	            /* To print current date in the HTTP date format explicitly adding the time zone to the formatter*/
	    	            Instant i = Instant.now();
	    	     	   String dateFormatted = DateTimeFormatter.RFC_1123_DATE_TIME
	    	     	           .withZone(ZoneOffset.UTC)
	    	     	           .format(i);
	    	     	   String currentDate="Date:"+dateFormatted;
	                	/*To get time difference between to messages and converting it to the string format*/
	                	String time2=dispTime.format(System.currentTimeMillis());
	                	date2=dispTime.parse(time2);
	                	//get difference between previous message and recent message
	                	 difference=date2.getTime()-date1.getTime();
	                	 int min=(int)((difference/(1000*60))%60);//calculate minute difference
	                	 int sec=(int)((difference/1000)%60);////calculate seconds difference
	                	 /*the format of the time is two digit numbers so concevert minutes and seconds to 
	                	  * 2 digits
	                	  * https://stackoverflow.com/questions/12421444/how-to-format-a-number-0-9-to-display-with-2-digits-its-not-a-date*/
	                	 String m=String.format("%02d", min);
	                	   String s=String.format("%02d", sec);
	                	   String time="("+m+"."+s+") - ";
	                	// String mindiff="("+min+"min"+sec+"sec) - ";//append minutes and seconds
	                  	 date1=date2;
		    			//append useragent,host,ContentLength,contentType to a String m
	                	String httpmsg=useragent+host+ContentLength+contentType+currentDate;
	                	//append timedifference to the username
	                	String timetrack=username+time;
		    			/*append all the strings useragent,host,ContentLength,contentType,
		    			 * timedifference to the username
		    			 * for HTTP format in the main message entered
		    			 * server reads this whole message */
	                	String wholeMsg="POST:"+timetrack + ":" + inputfield.getText() + ":" + httpmsg+":Chat";
	                	/*if the participated client is a coordinator then it sends a arbitary string to all other participants*/
	                	if (username.equalsIgnoreCase("Coordinator"))
	    	    		{
	    	    			String voteReq="POST:"+username + ":  " + inputfield.getText()+" - VOTE_REQUEST"+ ":" + httpmsg+":VoteRequest";
	    	    			//set voteRequest to true indicating coordinator has send a vote request
	    	    			voteRequest=true;
	    	    			out.println(voteReq);
	    	    			arbString=inputfield.getText();
	    	    			startTime=System.currentTimeMillis();
	    	    			endTime=startTime+10000;
	    	    			//set local_log as a vote_request
	    	    			local_log="Vote_request";
	    	    			state.setText("LOCAL_LOG:VOTE_REQUEST");
	    	    			
	    	    			/* when coordinator sends a voterequest then start a timer then create a timertask which
	    	    			 * calls participantTimeout method 
	    	    			 * coordinator waits for a participant to be voted for 10 seconds if participant 
	    	    			 * does not vote in 10 seconds then coordinator sends globalo_abort to all participants*/
	                        toolkit = Toolkit.getDefaultToolkit();
	                        timer = new Timer();
	                        TimerTask rt=new patricipantTimeOut();
	                        timer.schedule(rt, 10 * 1000);
	                       
	        	           	    	    	    	    			
	    	    		}
	                	/*if the client is not a coordinator then it can send a regular messages to all other clients*/
	    	    		else{
	                   	out.println(wholeMsg);//send whole message in HTTP format to output stream
	                    
	                   	}
		                out.flush(); // flushes the buffer
		             } catch (Exception e) {
	            	 e.printStackTrace();
	             }
	    		
	    		/*After sending message to output stream clear the textfield and set focus to inputfield
	    		 * to take messages input*/
	    		inputfield.setText("");
	    		inputfield.requestFocus();
	    		
	    		
	    		}
	    	
	    });
	    /*add listner to connect button
	     * after entering username if user clicks on connect button then it checks if the username is valid(ie.only charachters)
	     * then its creates a new thread with setting username in title */
	    connect.addActionListener(new ActionListener(){
	    	public void actionPerformed(ActionEvent action)
	    	{
	    		if (isConnected == false) 
	    	        {
	    			
	    			//take username 
	    			username = usertext.getText();
	    			
	    			//check if the user name is valid that is contains only letters
		    		if(username.matches("[a-zA-Z]*"))
		    		{    
	    			   usertext.setEditable(false);
    	            try 
    	            {
    	            //server is at localhost and port 7879 so use same port number here
    	            socket = new Socket("localhost",7879);
    	            //create inputstream and outputstream
	                InputStreamReader streamreader = new InputStreamReader(socket.getInputStream());
	                in = new BufferedReader(streamreader);
	                out = new PrintWriter(socket.getOutputStream());
	                out.println("POST:"+username + ": has connected.:::::::::Connect");
	                out.flush(); 
	                isConnected = true; //connection is established so this value is true
	                connect.setEnabled(false);//disable the connect button
	                /*if its a coordinator then set the local_log to start2pc and disable abort and commit 
	                 * buttons and set a frame titile as a Coordinator*/
	               if(username.equalsIgnoreCase("Coordinator"))
	                {
	                	vote.setEnabled(true);
	                	local_log="START2PC";
	                	state.setText("LOCAL_LOG:START2PC");
	                	frame.setTitle(username);
	    				abort.setEnabled(false);
	    				commit.setEnabled(false);
	                }
	               /*if its a not coordinator then set the local_log to init and disable a vote 
	                 * button and set a titile as a Participant with its name*/
	                else
	                	
	                {
	                	
	                	vote.setEnabled(false);
	                	send.setEnabled(false);
	                	local_log="INIT";
	                	state.setText("LOCAL_LOG:INIT");
	                	//set frame title as a participnt name
	                	 frame.setTitle("Participant "+username);
	                	 
	                	 /* if the client is a participant then it waits for a Coordinator to send a vote request
	     	             * if participant did not get vote request from coordinator in 20 seconds then
	     	             * it sets local_abort*/
	                    	
	                    		if(voteRequest==false)//if coordinator not sent any request
	                    		{

	                             toolkit = Toolkit.getDefaultToolkit();
	                             timer1 = new Timer();
	                             TimerTask timert=new voteRequestTimeOut();
	                             timer1.schedule(timert, 20 * 1000);	
	                            
	                    		}
	                    	
	                    			
	                try{
	        			/*create a new filereader with the filename as a participant1 as declared 
	        			 * before.This file contaions arbitary string saved by the participant
	        			 * also a create bufferreader to read content of the file
	        			 */
	     	    		FileReader reader=new FileReader(filename);
	     	    		BufferedReader br=new BufferedReader(reader);
	     	    		//read the content of the  file and display it on server window 
	     	    		
	     	    		textarea.read(br,null);
	     	    		textarea.append("\n");
	     	    		textarea.append("is the arbitary String from file.\n");
	     	    		//close bufferreader
	     	    		br.close();
	     	    		//focus the textaerea as soon as it loads the previous messages
	     	    		textarea.requestFocus();
	     	    		}catch(Exception e){
	     	    			e.printStackTrace();
	     	    		}
    	            }//else
	            } //try
    	            catch(Exception e)
	    		{
	    		e.printStackTrace();}
    	           
	            thread.start();
	           /*if the user is coordinator then he sends a string and a vote request
	            * if it's participant then it waits for a string from coordinator*/
	           
	            
	    	        }//if
		    		//if user enters invalid username then  give message to enter valid user name 
		    		else
			    	{
		    			textarea.append("Username is not valid.");
		    			textarea.append("\nPlease enter only Charachters.");
		    			textarea.append("");
			    	   			
			    	}//else
		    		
	    	}//if
	    			
	    	}	
	    });
	    vote.addActionListener(new ActionListener(){
	    	public void actionPerformed(ActionEvent action)
	    	{  
	    		isCoordinatorVote=true;
	    		
	    		/* user-agent displays name of the browsers,this list shows this application is independent 
            	 * of a browser.
            	 *  display the host name in this case host is  "localhost"*/
	    
            	String useragent=" User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36";
            	String host=" Host:"+socket.getInetAddress().getHostName();//get host name
            	
            	/*get length of the original messsage*/
            	String ga="GLOBAL_ABORT";
            	String ContentLengthAbort=" Content-length:"+ga.length();
            	String gc="GLOBAL_COMMIT";
            	String ContentLengthCommit=" Content-length:"+gc.length();
            	String contentType=" Conent-Type:text/plain";
	            
	            /* To print current date in the HTTP date format explicitly adding the time zone to the formatter*/
	            Instant i = Instant.now();
	     	   String dateFormatted = DateTimeFormatter.RFC_1123_DATE_TIME
	     	           .withZone(ZoneOffset.UTC)
	     	           .format(i);
	     	   String currentDate="Date:"+dateFormatted;
	     	   String httpmsgAbort=useragent+host+ContentLengthAbort+contentType+currentDate;
	     	  String httpmsgCommit=useragent+host+ContentLengthCommit+contentType+currentDate; 
	    		String abort = "POST:"+username+ ": - GLOBAL_ABORT:"+ httpmsgAbort + ":CoordinatorAbort";
	    		String abortTimeout = "POST:"+username+ ": - GLOBAL_ABORT:"+ httpmsgAbort + ":Timeout";
	    		String commit = "POST:"+username+ ": - GLOBAL_COMMIT:"+ httpmsgCommit + ":CoordinatorCommit";
	    		/*if all other participants voted it means count is 3,then 
	    		 * check whether any participant voted abort
	    		 * if voted then send global_abort otherwise check if all 3 participants voted commit
	    		 * if so then send global_commit to all other participants*/
	    		String cvote="";
	    		if(voteCount==3)
	    		{
	    			if(voteAbort>0)
	    			{
	    				cvote=abort;
	    				local_log="GLOBAL_ABORT";
	    				state.setText("LOCAL_LOG:GLOBAL_ABORT");
	    				 
	    				 globalCommitFlag=false;
	    			}
	    			if(voteCommit==3)
	    			{
	    				cvote=commit;
	    				local_log="GLOBAL_COMMIT";
	    				state.setText("LOCAL_LOG:GLOBAL_COMMIT");
	    				
	    				 globalCommitFlag=true;
	    			}
	    		}
	    		/*if voteCount is less than 3 means all participants have not voted and
	    		 * one of the participant timed out
	    		 * then send global_abort to all participants */
	    		else if(voteCount<3)
	    		{
	    			cvote=abortTimeout;
    				local_log="GLOBAL_ABORT";
    				state.setText("LOCAL_LOG:GLOBAL_ABORT");
    				textarea.append("\n One of the participant not voted in 10 seconds\n");
	    		}
	    	
	    		 
	    	    try 
	    	        {
	    	        /*set output stream 
	    	         * disable disconnect and send buttons */
	                out.println(cvote); 
	                out.flush();
	                

	    	        } catch(Exception e) {
	    	        	e.printStackTrace();
	    	        }
	    	        
	    	    }
	    });
	    /*add listner to disconnect button
	     * this button logs off the user from chat room
	     *  */
	    disconnect.addActionListener(new ActionListener(){
	    	public void actionPerformed(ActionEvent action)
	    	{       
	    		String dc = ("POST:"+username + ": has disconnected.::::::::::Disconnect");
	    		 
	    	    try 
	    	        {
	    	        /*set output stream 
	    	         * disable disconnect and send buttons */
	                out.println(dc); 
	                out.flush();
	                disconnect.setEnabled(false);
	                send.setEnabled(false);
	    	       // socket.close();

	    	        } catch(Exception e) {
	    	        	e.printStackTrace();
	    	        }
	    	        isConnected = false;
	    	    }
	    });
	    abort.addActionListener(new ActionListener(){
	    	public void actionPerformed(ActionEvent action)
	    	{       
	    		/* user-agent displays name of the browsers,this list shows this application is independent 
            	 * of a browser.
            	 *  display the host name in this case host is  "localhost"*/
            	String useragent=" User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36";
            	String host=" Host:"+socket.getInetAddress().getHostName();//get host name
            	
            	/*get length of the original messsage*/
            	String ga="LOCAL_ABORT";
            	String ContentLength=" Content-length:"+ga.length();
            	/*
            	 * as this is simple chat room system,independent of the browser the content type is text/plain*/
            	String contentType=" Conent-Type:text/plain";
	          
	            /* To print current date in the HTTP date format explicitly adding the time zone to the formatter*/
	            Instant i = Instant.now();
	     	   String dateFormatted = DateTimeFormatter.RFC_1123_DATE_TIME
	     	           .withZone(ZoneOffset.UTC)
	     	           .format(i);
	     	   String currentDate="Date:"+dateFormatted;
	     	   String httpmsg=useragent+host+ContentLength+contentType+currentDate;
	    		String ab = "POST:"+username+ ": - LOCAL_ABORT:"+ httpmsg + ":Abort";
	    			    		
	    	    try 
	    	        {
	    	        /*set output stream 
	    	         * disable disconnect and send buttons */
	                out.println(ab); 
	                out.flush();
	    	        } catch(Exception e) {
	    	        	e.printStackTrace();
	    	        }
	    	    local_log="READY";
	    	    state.setText("LOCAL_LOG:READY"); //change local_log to ready
	    	    }
	    });
	    commit.addActionListener(new ActionListener(){
	    	public void actionPerformed(ActionEvent action)
	    	{       
	    		
	    		/* user-agent displays name of the browsers,this list shows this application is independent 
            	 * of a browser.
            	 *  display the host name in this case host is  "localhost"*/
            	String useragent=" User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36";
            	String host=" Host:"+socket.getInetAddress().getHostName();//get host name
            	
            	/*get length of the original messsage*/
            	String lc="LOCAL_COMMIT";
            	String gc="GLOBAL_COMMIT";
            	String ContentLengthl=" Content-length:"+gc.length();
            	String ContentLengthg=" Content-length:"+gc.length();
            	
            	 * as this is simple chat room system,independent of the browser the content type is text/plain*/
            	String contentType=" Conent-Type:text/plain";
	        
	            /* To print current date in the HTTP date format explicitly adding the time zone to the formatter*/
	            Instant i = Instant.now();
	     	   String dateFormatted = DateTimeFormatter.RFC_1123_DATE_TIME
	     	           .withZone(ZoneOffset.UTC)
	     	           .format(i);
	     	   String currentDate="Date:"+dateFormatted;
	     	   String httpmsglocal=useragent+host+ContentLengthl+contentType+currentDate;
	    		String lcWhole = "POST:"+username+ ":-LOCAL_COMMIT:"+ httpmsglocal + ":Commit";
	    		
	    		
	    			  try 
		    	        {
		    	        /*set output stream 
		    	         * disable disconnect and send buttons */
	    				 
	    				  out.println(lcWhole);
	    				  
	    				  
	    				  out.flush();
		    	        } catch(Exception e) {
		    	        	e.printStackTrace();
		    	        }
	    			  
                      toolkit = Toolkit.getDefaultToolkit();
                      timer = new Timer();
                      TimerTask rt=new DecisionRequest();
                      timer.schedule(rt, 20 * 1000);
                      /*set local_log to ready*/
	    			    local_log="READY";
	    			    state.setText("LOCAL_LOG:READY");
	    			    			
	    		//}
	    	       
	    	       
	    	    }
	    });
	    /*if the coordinator crashes then it sets the local_log to abort and sends the message to 
	     * all other participants
	     * and if participant crashes set local_log to Local_abort and msg to all*/
	    frame.addWindowListener(new WindowAdapter()
		{
	    	@Override
	    	public void windowClosing(WindowEvent we)
	    	{
	    		if (username.equalsIgnoreCase("Coordinator"))
	    		{
	    			/* user-agent displays name of the browsers,this list shows this application is independent 
	            	 * of a browser.
	            	 *  display the host name in this case host is  "localhost"*/
	            	String useragent=" User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36";
	            	String host=" Host:"+socket.getInetAddress().getHostName();//get host name
	        
	            	/*get length of the original messsage*/
	            	String ga="GLOBAL_ABORT";
	            	String ContentLength=" Content-length:"+ga.length();
	        
	            	 * as this is simple chat room system,independent of the browser the content type is text/plain*/
	            	String contentType=" Conent-Type:text/plain";
		
		            /* To print current date in the HTTP date format explicitly adding the time zone to the formatter*/
		            Instant i = Instant.now();
		     	   String dateFormatted = DateTimeFormatter.RFC_1123_DATE_TIME
		     	           .withZone(ZoneOffset.UTC)
		     	           .format(i);
		     	   String currentDate="Date:"+dateFormatted;
		     	   String httpmsg=useragent+host+ContentLength+contentType+currentDate;
		    		String coordinatorCrash = "POST:"+username+ ": Coordinator Crashed:"+ httpmsg + ":Crashed";
		    		try 
	  	    	        {
	  	    	        /*set output stream 
	  	    	         * disable disconnect and send buttons */
	  	                out.println(coordinatorCrash); 
	  	                out.flush();
	  	    	        } catch(Exception e) {
	  	    	        	e.printStackTrace();
	  	    	        }
	    			
	    		}
	    		else
	    		{
	    			/* user-agent displays name of the browsers,this list shows this application is independent 
	            	 * of a browser.
	            	 *  display the host name in this case host is  "localhost"*/
	            	String useragent=" User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36";
	            	String host=" Host:"+socket.getInetAddress().getHostName();//get host name
	        
	            	/*get length of the original messsage*/
	            	String pc="LOCAL_ABORT";
	            	String ContentLength=" Content-length:"+pc.length();
	        
	            	 * as this is simple chat room system,independent of the browser the content type is text/plain*/
	            	String contentType=" Conent-Type:text/plain";
		
		            /* To print current date in the HTTP date format explicitly adding the time zone to the formatter*/
		            Instant i = Instant.now();
		     	   String dateFormatted = DateTimeFormatter.RFC_1123_DATE_TIME
		     	           .withZone(ZoneOffset.UTC)
		     	           .format(i);
		     	   String currentDate="Date:"+dateFormatted;
		     	   String httpmsg=useragent+host+ContentLength+contentType+currentDate;
		    		String participantCrash = "POST:"+username+ ": has Crashed:"+ httpmsg + ":Crashed";
		    		try 
  	    	        {
  	    	        /*set output stream 
  	    	         * disable disconnect and send buttons */
  	                out.println(participantCrash); 
  	                out.flush();
  	    	        } catch(Exception e) {
  	    	        	e.printStackTrace();
  	    	        }
	    		}
	    	}
	    	public void windowClosed(WindowEvent we)
	    	{
	    		setVisible(false);
	    	}
		});
	    //setting frame as visible 
	    frame.setVisible(true);
	    frame.setSize(520,500);
	    frame.setResizable(false);

	}
    
    public static void main(String args[])
    {
    	//create a new instance of the client class
    	twoPCClient c=new twoPCClient();
    	c.createGUI();//call createGUI() method
      
    	
    }
    /* when coordinator sends a voterequest then start a timer then create a timertask which
	 * calls participantTimeout method 
	 * coordinator waits for a participant to be voted for 10 seconds if participant 
	 * does not vote in 10 seconds then coordinator sends globalo_abort to all participants*/
    public class patricipantTimeOut extends TimerTask
    {
    	 public void run() {
    		 /* user-agent displays name of the browsers,this list shows this application is independent 
         	 * of a browser.
         	 *  display the host name in this case host is  "localhost"*/
         	String useragent=" User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36";
         	String host=" Host:"+socket.getInetAddress().getHostName();//get host name

         	/*get length of the original messsage*/
         	String ga="LOCAL_ABORT";
         	String ContentLength=" Content-length:"+ga.length();

         	
         	String contentType=" Conent-Type:text/plain";
	        
	            /* To print current date in the HTTP date format explicitly adding the time zone to the formatter*/
	            Instant i = Instant.now();
	     	   String dateFormatted = DateTimeFormatter.RFC_1123_DATE_TIME
	     	           .withZone(ZoneOffset.UTC)
	     	           .format(i);
	     	   String currentDate="Date:"+dateFormatted;
	     	   String httpmsg=useragent+host+ContentLength+contentType+currentDate;
	    		String gAbort = "POST:"+username+ ": - GLOBAL_ABORT:"+ httpmsg + ":TimeoutAbort";
	    			    		
	    	    
	    	    
    	      //checks voteCount is 3 or not
    		 //if its less than 3 it means all participant have not voted.
    	      if(voteCount<3)
    	      {
    	    //display timeout at coordinators frame
    	      textarea.append("\n One of the participant not voted in time...\n ");
    	      //set participantTimeout to true
    	      participantTimeout=true;
    	      //if participant does not vote in 10 seconds then coordinator beeps indicating 
    	      //one of the participant timed out
    	      try 
  	        {
  	        /*set output stream 
  	         * disable disconnect and send buttons */
              out.println(gAbort); 
              out.flush();
  	        } catch(Exception e) {
  	        	e.printStackTrace();
  	        }
    	      toolkit.beep();
    	      timer.cancel(); 
    	      }//if
    	      else
    	      {
    	    	  textarea.append("\n All participants voted within time(10 Seconds) !");  
    	    	  
    	      }
    	  }//run
    }
    public class decisionTimeOut extends TimerTask
    {
    	 public void run() {
    	   
    		 /* user-agent displays name of the browsers,this list shows this application is independent 
         	 * of a browser.
         	 *  display the host name in this case host is  "localhost"*/
         	String useragent=" User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36";
         	String host=" Host:"+socket.getInetAddress().getHostName();//get host name
         	
         	/*get length of the original messsage*/
         	String ga="GLOBAL_ABORT";
         	String ContentLengtha=" Content-length:"+ga.length();
         	String gc="GLOBAL_ABORT";
         	String ContentLengthc=" Content-length:"+gc.length();
         	
         	 * as this is simple chat room system,independent of the browser the content type is text/plain*/
         	String contentType=" Conent-Type:text/plain";
	            
	            /* To print current date in the HTTP date format explicitly adding the time zone to the formatter*/
	            Instant i = Instant.now();
	     	   String dateFormatted = DateTimeFormatter.RFC_1123_DATE_TIME
	     	           .withZone(ZoneOffset.UTC)
	     	           .format(i);
	     	   String currentDate="Date:"+dateFormatted;
	     	   String httpmsgda=useragent+host+ContentLengtha+contentType+currentDate;
	     	  String httpmsgdc=useragent+host+ContentLengthc+contentType+currentDate;
	    		String decisionabort = "POST:"+username+ ": - LOCAL_ABORT:"+ httpmsgda + ":Abort";
	    		String decisionCommit = "POST:"+username+ ": - GLOBAL_COMMIT:"+ httpmsgdc + ":Commit";
	    		String decision ="";
    	     if(local_log=="GLOBAL_COMMIT")
    	     {
    	    	 local_log="GLOBAL_COMMIT";
            	 state.setText("LOCAL_LOG: GLOBAL_COMMIT"); 
            	 decision=decisionCommit;
    	     }
    	     else
    	     {
    		  local_log="LOCAL_ABORT";
         	  state.setText("LOCAL_LOG: LOCAL_ABORT");
         	
         	decision=decisionabort;
    	     }
    	     if(username.equalsIgnoreCase("Coordinator"))
    	     {
    	    	 try 
    	   	        {
    	   	        /*set output stream 
    	   	         * disable disconnect and send buttons */
    	               out.println(""); 
    	               out.flush();
    	   	        } catch(Exception e) {
    	   	        	e.printStackTrace();
    	   	        }//catch
    	     }
    	     else
    	     {
    	     try 
   	        {
   	        /*set output stream 
   	         * disable disconnect and send buttons */
               out.println(decision); 
               out.flush();
   	        } catch(Exception e) {
   	        	e.printStackTrace();
   	        }//catch
    	     }
         	 timerp.cancel();
    	      
    	  }//run
    }
    public class DecisionRequest extends TimerTask
    {
    	 public void run() {
    	      
    	      if(isCoordinatorVote==false)
    	      {
    	    	  System.out.println(isCoordinatorVote);
    	    	  /*if participant is waiting for the coordinator to wait and times out then
              	   * set local_log to DECISION*/
              	  local_log=" DECISION_REQUEST";
               	  state.setText("LOCAL_LOG: DECISION");
               	  
    	    	  /* user-agent displays name of the browsers,this list shows this application is independent 
	            	 * of a browser.
	            	 *  display the host name in this case host is  "localhost"*/
	            	String useragent=" User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36";
	            	String host=" Host:"+socket.getInetAddress().getHostName();//get host name
	            	
	            	/*get length of the original messsage*/
	            	String ga="GLOBAL_ABORT";
	            	String ContentLength=" Content-length:"+ga.length();
	            	
	            	     	String contentType=" Conent-Type:text/plain";
		        
		            /* To print current date in the HTTP date format explicitly adding the time zone to the formatter*/
		            Instant i = Instant.now();
		     	   String dateFormatted = DateTimeFormatter.RFC_1123_DATE_TIME
		     	           .withZone(ZoneOffset.UTC)
		     	           .format(i);
		     	   String currentDate="Date:"+dateFormatted;
		     	   String httpmsg=useragent+host+ContentLength+contentType+currentDate;
		    		String decisionRequest = "POST:"+username+ ": - DECISION_REQUEST:"+ httpmsg + ":DECISION_REQUEST";
		    			
    	      System.out.println("coordinator Time's up!");
    	      
    	      
	    	    try 
	    	        {
	    	        /*set output stream 
	    	         * disable disconnect and send buttons */
	                out.println(decisionRequest); 
	                out.flush();
	    	        } catch(Exception e) {
	    	        	e.printStackTrace();
	    	        }
    	      
    	      toolkit.beep();
    	      timer.cancel(); 
    	     
    	      }//if
    	      else
    	      {
    	    	  System.out.println("Coordinated voted in 20 seconds!");  
    	    	  
    	      }
    	  }//run
    }
    public class voteRequestTimeOut extends TimerTask
    {
    	 public void run() {
    	 	  if(voteRequest==false)
    	 	  {
    	 	  textarea.append("\nNot recieved any vote request in 20 Seconds.\n");
    	 	  local_log="LOCAL_ABORT";
			  state.setText("LOCAL_LOG:LOCAL_ABORT");
			  
    	      toolkit.beep();
    	      /* user-agent displays name of the browsers,this list shows this application is independent 
          	 * of a browser.
          	 *  display the host name in this case host is  "localhost"*/
          	String useragent=" User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36";
          	String host=" Host:"+socket.getInetAddress().getHostName();//get host name
        
          	/*get length of the original messsage*/
          	String locala="GLOBAL_ABORT";
          	String ContentLength=" Content-length:"+locala.length();
          	
          	 
          	String contentType=" Conent-Type:text/plain";
	            
	            /* To print current date in the HTTP date format explicitly adding the time zone to the formatter*/
	            Instant i = Instant.now();
	     	   String dateFormatted = DateTimeFormatter.RFC_1123_DATE_TIME
	     	           .withZone(ZoneOffset.UTC)
	     	           .format(i);
	     	   String currentDate="Date:"+dateFormatted;
	     	   String httpmsg=useragent+host+ContentLength+contentType+currentDate;
	    		String voteRequestTimeout = "POST:"+username+ ": - LOCAL_ABORT:"+ httpmsg + ":voteRequestTimeout";
	    	
    	      try 
  	        {
  	        /*set output stream 
  	         * disable disconnect and send buttons */
              out.println(voteRequestTimeout); 
              out.flush();
  	        } catch(Exception e) {
  	        	e.printStackTrace();
  	        }
    	      timer.cancel(); 
    	 	  }  
    	  }//run
    }
    
}


