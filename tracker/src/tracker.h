#ifndef TRACKER
#define TRACKER

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/wait.h>  
#include <sys/socket.h>
#include <strings.h>
#include <glib.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <pthread.h>

#include "treatMessage.h"

GHashTable* hash_filekeys;
GHashTable* hash_peers;
GPtrArray* files;
pthread_mutex_t mutex_filekeys;
pthread_mutex_t mutex_files;

struct PeerData {
  int sock;
  int port;
  struct sockaddr_in sock_addr;
  socklen_t size_addr;
  struct regexData* regex_data_ptr; //l'objet est en réalité stocké dans TrackerData
  unsigned int timestamp;
};

struct File {
  char* filename;
  unsigned int size;
  unsigned int piece_size;
  char* key;
};

/* USER structures */
struct TrackerData {
  int port;
  int backlog_queue;
  int sock; 
  int timeout;
  
  struct sockaddr_in addr;
  struct regexData* regex_data;
};



void error(char *);
void acceptConnexion ();
void getListFiles();
void parseQuery();
void sendMatchPeer ();
void sendListFiles();
void hashAddPeer(char*, struct PeerData*);

void parseParameter(struct TrackerData* ,int , char **);
void initTrackerData (struct TrackerData**);
void initPeerData (struct PeerData**);
void startConnexion(struct TrackerData*);
struct PeerData* waitConnexion(struct TrackerData*);
void start_thread (void *);
void freeFile (void*,void*);



#endif
