# Peer to peer file transfer

*Distributed data transfer application based on a simple protocol*
![image-p2p](http://camille.mizony.com/img/portfolio/p7.jpg)


* **Server** is implemented in `C`
* **Peers** are implemented in `Java`

##Protocol used

Inform peers:
```
announce listen 60018 seed [jed 23 2 dj2 jed 33 223443 djkbdd445xkjc] leech [cle1 a etjdd33]
announce listen 60017 seed [abc 23 2 dj2] leech [a]
```

Search for a file based on name or size:
```
look [filesize>\"64\" filesize<\"32\" filename=\"jed\"]
look [filename=\"jed\"]
look [filesize>\"2\"]
look [filesize>\"2\" filesize<\"32\" filename=\"jed\" filesize<\"32\" filesize<\"32\"]
```

Get a file:
```
getfile 8905e92afeb80fc7722ec89eb0bf0966
getfile cle1
```

Update seed:
```
update seed [djhdd23dkjhd2 dskshdj3sjdh3jshd3] leech [a delhd433jh2 sdjhgd44432dskj3]
```
