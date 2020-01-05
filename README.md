# Peer-2-Peer Image Distribution

This is a simple project on sharing images between other clients on a network in a peer-2-peer way. A [Distributed Hash Table](https://en.wikipedia.org/wiki/Distributed_hash_table) is utilized to share the content information between the clients.

# How to Use
1. Go into both directories for each application where the .java files are located and run ``javac *.java``
2. The DHT servers need to be started first, so go into the directory ``Distributed Hash Table\bin\``
3. Four total instances will need to be ran each with different command parameters specified
- ``java DHTServerNode currentServerIP currentServerPort nextDHTServerIP nextDHTServerPort ServerID``
4. Since all the servers are connected in a ring, an example of the 4 instances can be as follows

| Instance        | currentServerIP  | currentServerPort  | nextDHTServerIP   | nextDHTServerPort   | ServerID    |
| :-------------: | :----------------| ------------------:| ----------------- |-------------------: | :----------:|
| 1               | localhost        | 20670              | localhost         | 20671               | 0           |
| 2               | localhost        | 20671              | localhost         | 20672               | 1           |
| 3               | localhost        | 20672              | localhost         | 20673               | 2           |
| 4               | localhost        | 20673              | localhost         | 20670               | 3           |

**NOTE: the server ID MUST be numbers between 0 and 3**

The output should resemble something like this:

![](https://github.com/TroyFernandes/Distributed-Hash-Table---P2P-Application/blob/master/Demo%20Images/Servers_Example.PNG)

5. Now the P2P application needs to be run. Head into the ``P2P Application\bin\`` folder and run this command:
``java UI serverIP serverPort firstDHTNodeIP firstDHTNodePort``
6. This is important, the ``serverIP`` and ``serverPort`` must be different than those used for the DHT servers as this is the server in which the P2P Clients communicate with EACH OTHER (NOT the DHT Servers). However, ``firstDHTNodeIP`` and ``firstDHTNodePort`` MUST be the same fields as those you gave ``ServerID = 0`` to. (e.g in the example above, these values are ``localhost`` and ``20670``)
**IMPORTANT:** Choose ports for each P2P Client which are two apart. For example, if you choose port 20674 for P2P Client 1, then choose port 20676 for P2P Client 2.
7. Now you can interact with the GUI Interface

![](https://github.com/TroyFernandes/Distributed-Hash-Table---P2P-Application/blob/master/Demo%20Images/GUI_Example.PNG)

8.  As a quick demonstration, you can use Client 1 to upload a file to the DHT Servers, and on Client 2 you can set your **Download Directory**, click the **Start Fileserver** button, click the **Get Available Files** Button, then  enter in the filename into the **File to Get** from the available files field which shows up and then click **Get Fil**e. The file will now be where you set your **Download Directory**. 
