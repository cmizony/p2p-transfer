#ifndef VALID_AND_PARSE_MESSAGE
#define VALID_AND_PARSE_MESSAGE

#include <pthread.h>

#include "compileExpressions.h"
#include "tracker.h"


///////////////////////////////////////////////////
//-------Prototype fonctions to check if---------// 
//------a message is valid or not separate-------//
//------- all the essential values---------------//
///////////////////////////////////////////////////

struct PeerData;
struct File;

/*
 * fonction to valid, parse and treat a message
 */
char* treatMessage (struct PeerData*, const char*);


/*
 * fonctions to help parsing and treating the message 
 */
// set the port number of the peer in the struct PeerData
int setPort (struct PeerData*, const char*, regmatch_t*, int);
// add a file with its description in the GPtrArray files
int setFiles (const char*, regmatch_t*, int, struct PeerData*);
void setFileDescription (char*, struct PeerData*);

// add a key and update its list of owner peers 
int setKeys (const char*, regmatch_t*, int, struct PeerData*);
void parseKeys (char*, struct PeerData*);
void addKeyIntoHashfilekeys (char*, struct PeerData*);

// find the file demanded with its criterions and send its description
char* sendFiles (const char*, regmatch_t*, int);
char* getFilesList (char*, char*); 
int verifyCriterions (char*, struct File*);
char* sendFilesToPeer (struct File*, int);

// send the list of the peers who has the file 
char* sendPeersList (const char*, regmatch_t*, int);



#endif
