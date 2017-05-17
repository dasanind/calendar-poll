/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies
 * Copyright (C) 2004 - Bull SA
 * Copyright (C) 1996 - 2000 Dyade
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 *
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): ScalAgent Distributed Technologies
 */
package classic;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.QueueTcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TopicTcpConnectionFactory;


/**
 * Administers an agent server for the classic samples.
 */
public class ClassicAdmin {

  public static void main(String[] args) throws Exception {
    
    System.out.println();
    System.out.println("Classic administration...");

    AdminModule.connect("root", "root", 60);

    //Creating queues for poll client
    Queue queue0 = Queue.create("queue0");
    Queue queue = Queue.create("queue1");
    Queue queue2 = Queue.create("queue2");
    
    Queue queue3 = Queue.create("queue3");
    Queue queue4 = Queue.create("queue4");
    Queue queue5 = Queue.create("queue5");
    Queue queue6 = Queue.create("queue6");
    //Creating queues for poll client
    //Topic topic = Topic.create("topic");
    Topic topic = Topic.create("topic1");
    Topic topic2 = Topic.create("topic2");
    
    User.create("anonymous", "anonymous");

   //setFreeReading for the queues for poll client
    queue0.setFreeReading();
    queue.setFreeReading();
    queue2.setFreeReading();
    
    queue3.setFreeReading();
    queue4.setFreeReading();
    queue5.setFreeReading();
    queue6.setFreeReading();
    //setFreeReading for the for poll client
//    topic.setFreeReading();
    topic.setFreeReading();
    topic2.setFreeReading();
   //setFreeWriting for the queues for poll client
    queue0.setFreeWriting();
    queue.setFreeWriting();
    queue2.setFreeWriting();
    
    queue3.setFreeWriting();
    queue4.setFreeWriting();
    queue5.setFreeWriting();
    queue6.setFreeWriting();
    //setFreeWriting for the queues for poll client
//    topic.setFreeWriting();
    topic.setFreeWriting();
    topic2.setFreeWriting();

    javax.jms.ConnectionFactory cf =
      TcpConnectionFactory.create("localhost", 16010);
    javax.jms.QueueConnectionFactory qcf =
      QueueTcpConnectionFactory.create("localhost", 16010);
    javax.jms.TopicConnectionFactory tcf =
      TopicTcpConnectionFactory.create("localhost", 16010);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf", cf);
    jndiCtx.bind("qcf", qcf);
    jndiCtx.bind("tcf", tcf);
    //jndi for the queues for poll client
    jndiCtx.bind("queue0", queue0);
    jndiCtx.bind("queue1", queue);
    jndiCtx.bind("queue2", queue2);
    
    jndiCtx.bind("queue3", queue3);
    jndiCtx.bind("queue4", queue4);
    jndiCtx.bind("queue5", queue5);
    jndiCtx.bind("queue6", queue6);
    //jndi for the queues for poll client

//    jndiCtx.bind("topic", topic);
    jndiCtx.bind("topic1", topic);
    jndiCtx.bind("topic2", topic2);
    jndiCtx.close();

    AdminModule.disconnect();
    System.out.println("Admin closed.");
  }
}
