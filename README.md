# calendar-poll
Doodle like calendar poll using a message-oriented middleware.

There are four java files and two xml files implementing the poll
system.  These are (1) ClassicAdmin.java, (2) Poll.java, (3)
PollClient.java, (4) ReceivingThread.java, (5) joramAdmin.xml, and (6)
build.xml.  There is also a configuration file config.txt that allows
the user to specify the participants in the system. The system
currently supports at most 7 poll clients. If you need to change this then you will have to update ClassicAdmin.java and joramAdmin.xml.

Place the java files and the joramAdmin.xml in the classic package of
joram-5.7.0.  For instance, on Mac, if you have installed Joram in the
/Applications folder, then the likely path to be entered here is
/Applications/joram-5.7.0/samples/src/joram/classic.

In the remainder of this readme, I will assume the above path.  If
your path is different, then use the appropriate path when using the
commands below.

Replace the build.xml in /Applications/joram-5.7.0/samples/src/joram
with the provided build.xml or add the following code to the existing
build.xml.

  <!-- Runs the Poll client -->
  <target name="pollclient" depends="simple_init"
    description="--> Starts a Poll client">
    <java classname="classic.PollClient" failonerror="no" fork="yes"
          dir="${run.dir}">
      <classpath path="${project.class.path}"/>
       <arg value="/Users/anindita/config.txt"/>
    </java>
  </target>

Replace the arg value in the above code to specify the path for
config.txt.


To start the poll clients, use the following commands:

1. Go to the  /Applications/joram-5.7.0/samples/src/joram directory.

2. Compile the samples
-- ant clean compile

3. Launch the Joram server
-- ant reset single_server

4. Run the administration code, creating ConnectionFactory, and Queue objects
-- ant classic_admin

5. Then start the pollclient
-- ant pollclient
(Note: To start another pollclient, open a new terminal and use the above command.)

The name of the participants is specified in config.txt.

The following options are provided by the PollClient:

Main Poll Menu for user <participantname>: 
  1. Create a New Poll.
  2. Display all Polls.
  3. Display all New Polls.
  4. Display all Responded Polls.
  5. Display all Closed Polls (with finalized times).
  6. Respond to an Open Poll.
  7. Close an Active Poll.

1. Create a New Poll. -- Allows a user to create a new poll. The user
is prompted to enter the poll name, specify the name of participants
from an available list of participants, and proposed meeting
times. For entering the sequence of proposed meeting times, the user
is prompted to enter date (in mm/dd/yyyy format) and time
(Ex. 11:30am-1:00pm). The user can add more than one proposed meeting
time.

2. Display all Polls. -- Displays all the polls that is created or
received by the user irrespective of their status (i.e. Active,
Responded, Closed).

3. Display all New Polls. -- Displays all the Active polls i.e. the
polls having status "Active".

4. Display all Responded Polls. -- Displays all the polls having
status "Responded".

5. Display all Closed Polls (with finalized times). -- Displays all
the polls having status "Closed" and their corresponding finalized
time.

6. Respond to an Open Poll. -- User can respond to an open poll by
entering the poll id and enter Yes, No or Maybe to the proposed
meeting times.

7. Close an Active Poll. -- The poll initiator can close the poll by
entering the poll id and then selecting one of the proposed time as
the finalized meeting time.


If you have further questions, email me at dasanuiit@gmail.com.
