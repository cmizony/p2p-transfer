#include "treatMessage.h"
#include <string.h>


//////////////////////////////////////////////////////////////
//---------------Checking if a message is valid-------------// 
//---------or not & separate all the essential values-------//
//////////////////////////////////////////////////////////////



/**
 * This function valid, parse and treat a message 
 *
 * @param struct PeerData*
 * @param const char*, the message to threat
 *
 **/

char* treatMessage (struct PeerData* peer_data, const char* str_request){
  size_t nmatch = 0;
  regmatch_t* pmatch = NULL;
  char* response_msg = NULL;

  /*
   *get the first word of the message
   */
  char start[9]; 
  strncpy(start, str_request, 9);
  strtok(start, " ");  
  /*
   *If the message starts with "announce" :  
   */
  if (strcmp(start, "announce") == 0){
    nmatch = peer_data->regex_data_ptr->preg_announce->re_nsub;
    pmatch = malloc (sizeof (*pmatch) * nmatch);
    if (pmatch){
      // verify if the message is valid
      if (regexec (peer_data->regex_data_ptr->preg_announce, str_request, nmatch, pmatch, 0) == 0){
	printf("Regex : Valid announce message\n");	
	//separate the essential values : $Port, seed[], leech[], and treat then
	if (setPort(peer_data, str_request, pmatch, 1) == 0 
	    && setFiles(str_request, pmatch, 3, peer_data) == 0
	    && setKeys(str_request, pmatch, 6, peer_data) == 0){
	  //send the response msg
	  response_msg = strdup("ok");
	  printf ("RESPONSE MSG : < %s >\n", response_msg);
	}
      }
      //if the message is not valid
      else {
	printf("WARNING : Invalid Announce Message\n");
      }
    }
  }
  /*
   *If the message starts with "look" :
   */
  else if (strcmp(start, "look") == 0){
    nmatch = peer_data->regex_data_ptr->preg_look->re_nsub;
    pmatch = malloc (sizeof (*pmatch) * nmatch);
    if (pmatch){
      // verify if the message is valid
      if (regexec (peer_data->regex_data_ptr->preg_look, str_request, nmatch, pmatch, 0) == 0){
	printf ("Regex : Valid look message\n");
	//printf("LookMessage : %s\n", str_request);
	//separate the essential values : look[], and treat then
	response_msg = sendFiles(str_request, pmatch, 1); 
      }
      //if the message is not valid
      else {
	printf("WARNING : Invalid Look Message\n");
      }
    }
  }
  /*
   * If the message starts with "getfile" :
   */
  else if (strcmp(start, "getfile") == 0){
    nmatch = peer_data->regex_data_ptr->preg_getfile->re_nsub;
    pmatch = malloc (sizeof (*pmatch) * nmatch);
    if (pmatch){
      // verify if the message is valid
      if (regexec (peer_data->regex_data_ptr->preg_getfile, str_request, nmatch, pmatch, 0) == 0){
	printf ("Regex : Valid getfile message\n");
	//printf("GetfileMessage : %s\n", str_request);
	//separate the essential values : $Key, and treat them
	response_msg = sendPeersList(str_request, pmatch, 1);
      }
      //if the message is not valid
      else {
	printf("WARNING : Invalid Getfile Message\n");
      }
    }
  }
  /*
   * If the message starts with "update" :   
   */
  else if (strcmp(start, "update") == 0){
    nmatch = peer_data->regex_data_ptr->preg_update->re_nsub;
    pmatch = malloc (sizeof (*pmatch) * nmatch);
    if (pmatch){
      // verify if the message is valid
      if (regexec (peer_data->regex_data_ptr->preg_update, str_request, nmatch, pmatch, 0) == 0){
	printf ("Regex : Valid update message\n");
	//printf("UpdateMessage : %s\n", str_request);
	//separate the essential values : seed[], leech[], and treat then
	if (setKeys(str_request, pmatch, 1, peer_data) == 0
	    && setKeys(str_request, pmatch, 2, peer_data) == 0){
	  //send the response msg
	  response_msg = strdup("ok");
	  printf ("RESPONSE MSG : < %s >\n", response_msg);
	}
      }
      //if the message is not valid
      else {
	printf("WARNING : Invalid Update Message\n");
      }
    }
  }
  /*
   *if the message starts with none of the words given
   */
  else {
    printf("WARNING : InvalidMessage\n");
  }
  //liberate the value pmatch
  free(pmatch);
  if (response_msg == NULL )
    response_msg = strdup("ko");

  return response_msg; 
}





/*
  all these fonctions separate some values from a sentence
  and treat them by completing the structures
  piece is the number of the piece of the initial message 
  (delimited by parentheses in the regex compiled expression) 
  that you want to parse more precisely
*/




/**
 *This function set the port number of the peer
 *in the PeerData structure
 *
 * @param struct PeerData*
 * @param const char*, the message to threat
 * @param regmatch_t*
 *
 **/

int setPort (struct PeerData* peer_data, const char* str_request, regmatch_t* pmatch, int piece){
  char* port;
  int start = pmatch[piece].rm_so;
  int end = pmatch[piece].rm_eo;
  size_t size = end - start;	  

  port = malloc (sizeof (*port) * (size + 1));
  if (port){
    //port contains the port number of the peer
    strncpy (port, &str_request[start], size);
    port[size] = '\0';
    printf ("port %s added\n", port);
    //set the port number of the peer in the PeerData structure
    peer_data->port = atoi(port);
    free(port);
    return 0;
  }
  return -1;
}



/**
 *This function complete the GPtrArray files with 
 *the files described in the message + complete the 
 *GHasTable with the couple (Key, array of peer IP)
 *
 * @param const char*, the message to threat
 * @param regmatch_t*
 * @param int
 * @param struct PeerData*
 *
 **/
 
int setFiles (const char* str_request, regmatch_t* pmatch, int piece, struct PeerData* peer_data){
  char* desc;
  int start = pmatch[piece].rm_so;
  int end = pmatch[piece].rm_eo;
  size_t size = end - start;	  

  desc = malloc (sizeof (*desc) * (size + 1));
  if (desc){
    //desc contains the list of the files with their description issued from the seed[] part of the message 
    strncpy (desc, &str_request[start], size);
    desc[size] = '\0';
    if (strcmp(desc, "") != 0)
      //set the file description in a new struct File
      setFileDescription(desc, peer_data);
    free(desc);
    return 0;
  }
  return -1;
}




/**
 *This function complete the last one by parsing the description 
 *of the files given and adding it to a new structure File
 *
 * @param const char*, description of the file in one sentence to parse
 * @param struct PeerData*
 *
 **/
void setFileDescription (char* desc, struct PeerData* peer_data){
  int count = 0;
  char str[strlen(desc)];
  strcpy(str, desc);
  char* info = NULL;
  info = strtok(str, " ");
  struct File* file = NULL;
  while(info != NULL)
    {
      if (count%4==0){
	// there is a new file described so we create a new struct File
	file = malloc (sizeof(struct File));
	//	printf("__Filename__ : %s\n", info);
	file->filename = malloc (strlen(info)*sizeof(char) + 1);
	strcpy(file->filename, info);
      }
      else if (count%4==1){
	//	printf("__Size__ : %s\n", info);
	file->size = atoi(info);
      }
      else if (count%4==2){
	//	printf("__PieceSize__ : %s\n", info);
	file->piece_size = atoi(info);
      }
      else if (count%4==3){
	//	printf("__Key__ : %s\n", info);
	file->key = malloc (strlen(info)*sizeof(char) + 1);
	strcpy(file->key, info);

	//the description is ended so we add the file in the GPTrArray files
	unsigned int index = 0;
	int exist = -1;
	//verify if the file is not already in the struct files
	pthread_mutex_lock( &mutex_files );
	while(index<files->len){
	  if (strcmp(((struct File*)g_ptr_array_index(files, index))->key, info) == 0){  
	    exist = 0;
	  }
	  index ++;
	}
	//if the file doesn't exist yet
	if (exist == -1){
	  //add the file
	  g_ptr_array_add(files, file);
	  pthread_mutex_unlock( &mutex_files );
	  printf ("file : %s added\n", file->filename);
	  //and we add the peer IP on the GHashTable : hash_filekeys identified by this key
	  addKeyIntoHashfilekeys (file->key, peer_data);
	}	
	else{ 
	  pthread_mutex_unlock( &mutex_files );
	  //and we also add the peer IP on the GHashTable : hash_filekeys identified by this key
	  addKeyIntoHashfilekeys (file->key, peer_data);
	  printf ("file %s not added because its key already exists\n", file->filename);
	  freeFile(file,NULL);
	  free(file);
	}
      }
      info = strtok(NULL, " ");
      count ++;
    }
}




/**
 *This function add the keys on the GHashTable hash_peer or update its array of peer IP associed
 *
 * @param const char*, description of the file in one sentence to parse
 * @param regmatch_t*
 * @param int
 * @param struct PeerData*
 *
 **/
int setKeys (const char* str_request, regmatch_t* pmatch, int piece, struct PeerData* peer_data){
  char* keys;
  int start = pmatch[piece].rm_so;
  int end = pmatch[piece].rm_eo;
  size_t size = end - start;	  

  keys = malloc (sizeof (*keys) * (size + 1));
  if (keys){
    //keys is the necessary value of the str_request message
    strncpy (keys, &str_request[start], size);
    keys[size] = '\0';
    if (strcmp(keys, "") != 0)
      //parse all the keys given
      parseKeys(keys, peer_data);
    free(keys);
    return 0;
  }
  return -1;
}




/**
 *This function complete the last one by parsing all 
 *the keys and treating them one by one
 *
 * @param char*, sentence containig all the keys to parse 
 * @param struct PeerData*
 *
 **/
void parseKeys (char* keys, struct PeerData* peer_data){
  char* str = malloc(strlen(keys)*sizeof(char) +1);
  strcpy(str, keys);
  keys = strtok(str, " ");

  char* key = NULL;
  while( keys != NULL )
    {
      key = malloc (strlen(keys)*sizeof(char) +1);   //pas free
      strcpy(key, keys);
      //printf("__key__ : %s\n", keys);
      //add the peer IP on the GHashTable hash_filekeys identified by this key
      addKeyIntoHashfilekeys (key, peer_data);
      keys = strtok(NULL, " ");
      free(key); 
    }
  free(str);
}



/**
 *This function int the HashTable hash_filekeys a couple 
 *containing a key and the HashTable pointing into the 
 *struct PeerData which has got the file identified by this key
 *
 * @param char*, a key 
 * @param struct PeerData*
 *
 **/
void addKeyIntoHashfilekeys (char* key, struct PeerData* peer_data){
  //the key exists yet
  pthread_mutex_lock( &mutex_filekeys );
  if (g_hash_table_lookup_extended (hash_filekeys,key,NULL,NULL)) {
    GHashTable* table = g_hash_table_lookup(hash_filekeys,key);
    g_hash_table_insert (table, inet_ntoa(peer_data->sock_addr.sin_addr), peer_data);
    printf ("peer IP %s added or updated for the key : %s\n", inet_ntoa(peer_data->sock_addr.sin_addr), key);
  }
  //the key does not exist
  else {
    char* key_copy = malloc(strlen(key)*sizeof(char) +1);
    strcpy(key_copy, key);
    GHashTable* new_hash = g_hash_table_new((GHashFunc)g_str_hash,(GEqualFunc)g_str_equal);
    g_hash_table_insert (new_hash, inet_ntoa (peer_data->sock_addr.sin_addr),peer_data);
    g_hash_table_insert (hash_filekeys,key_copy,new_hash);
    printf ("new key : %s & peer IP %s added\n", key_copy, inet_ntoa (peer_data->sock_addr.sin_addr));
 }
  pthread_mutex_unlock( &mutex_filekeys );
}




/**
 *This function find and send the list of the files according to 
 *the criterion given in the message sent by the peer
 *
 * @param const char*, the message to threat 
 * @param regmatch_t*
 * @param int
 *
 **/
char* sendFiles (const char* str_request, regmatch_t* pmatch, int piece){
  char* criterions;
  int start = pmatch[piece].rm_so;
  int end = pmatch[piece].rm_eo;
  size_t size = end - start;	  
  char* response_msg = NULL;  
  //get the important part of the message :  all the filename & filesize 
  criterions = malloc (sizeof (*criterions) * (size + 1));
  if (criterions){    
    strncpy (criterions, &str_request[start], size);
    criterions[size] = '\0';
    
    //verify if a filename restriction is given because it is easier to restrist the number of files which we have to verify all the criterions with, then 
    char criterions_copy[strlen(criterions)];
    strcpy(criterions_copy, criterions);
    char* filename = NULL;
    filename = strtok(criterions_copy, " ");
    // try to find a filename
    while (filename != NULL){
      if (strncmp(filename, "filename", 8) == 0){
	//the value filename contains the name of the file
	filename = strtok(filename, "\"");
	filename = strtok(NULL, "\"");
	printf ("filename found is %s\n", filename);
	//now that we have a filename, get the files which valid the criterions given
       	response_msg = getFilesList (criterions, filename); 
	free(criterions);
	return response_msg;
      }
      filename = strtok(NULL, " ");
    }
    //no filename restriction is given so try to find a file which valid the filesize restrictions given
    printf ("no filename restriction\n");
    response_msg = getFilesList (criterions, NULL); 
  }
  free(criterions);
  return response_msg;
}


/**
 *This function find the files which valid the criterions given.
 *If a filename is given, it is easier to find if the file valids the criterions
 *because the complexity is lower then
 *
 * @param char*, the sentence containig the criterions
 * @param char*, name of the file concerned if there is a filename restriction in 
 * the message (found in yhe last fonction), or NULL if there is no filename restriction given
 *
 **/
char* getFilesList (char* criterions, char* filename){
  char* response_msg = NULL;
  unsigned int index = 0;
  int n_file = 0;
  response_msg = malloc (7*sizeof(char));
  strcpy (response_msg, "list [");

  //for each file, verify the criterions begining by the filename restriction if we found one before
  pthread_mutex_lock( &mutex_files );
  while(index < files->len){
    if ((filename == NULL) || (strcmp(((struct File*)g_ptr_array_index(files, index))->filename, filename) == 0)){
      struct File* file;
      file = g_ptr_array_index (files, index);
      //verify if all the others criterions are correct (so only the filesize ones)
      if (verifyCriterions (criterions, file) == 0){
	//add the description of this file to the response_msg
	printf("criterions are ok so a file description will be sent !\n");
	n_file++;
	char* response = sendFilesToPeer (file, n_file);
	response_msg = realloc(response_msg, strlen(response_msg) + strlen(response) + 1);
	strcat(response_msg, response);
	free(response);
      }
    }
    index ++;
  }
  pthread_mutex_unlock( &mutex_files );
  response_msg = realloc(response_msg, strlen(response_msg) + 2); 
  strcat (response_msg, "]");
  printf ("RESPONSE MSG : < %s >\n", response_msg);
  return response_msg;
}






/**
 *This function helps the last one by verifying all the filesize criterions given
 *
 * @param char*, the sentence containig the criterions
 * @param char*, name of the file concerned
 *
 **/
int verifyCriterions (char* criterions, struct File* file){
  char criterions_copy[strlen(criterions)];
  strcpy(criterions_copy, criterions);
  char* crit = strtok(criterions_copy, " ");
  //parse all the criterions and if it is a filesize restriction, verify if file agrees with it
  while (crit != NULL){
    //it is a filesize restriction
    if (strncmp(crit, "filesize", 8) == 0){
      //comparison is the caracter > or < or =
      char comparison='\0';     
      // size is the value of the size associated
      char size [strlen(crit)-9];
      comparison = crit[8];
      int i = 10;  
      while(crit[i] == '0' 
	    || crit[i] == '1' 
	    || crit[i] == '2' 
	    || crit[i] == '3' 
	    || crit[i] == '4' 
	    || crit[i] == '5' 
	    || crit[i] == '6' 
	    || crit[i] == '7'
	    || crit[i] == '8' 
	    || crit[i] == '9'){
	size[i-10]=crit[i];
	i++;
      }
      size[i-10]='\0';
      if ((comparison == '>' && file->size > (unsigned)atoi(size)) 
	  || (comparison == '<' && file->size < (unsigned)atoi(size)) 
	  || (comparison == '=' && file->size == (unsigned)atoi(size))){
	//printf("comparison %c %s is ok\n", comparison, size);
      }
      else{
	//printf("comparison %c %s is not ok\n", comparison, size);
	return -1;
      }
    }
    crit = strtok(NULL, " ");	  
  }
  return 0;
}




/**
 *This function send the description of a file
 *(filename, size, piece_size and key) to the peer 
 *because it is the one he asked for
 *
 * @param struct File, the file which description has to be sent
 *
 **/
char* sendFilesToPeer (struct File* file, int n_file){
  char* response = NULL;
  response = malloc(strlen(file->filename) + 2 * 20 * sizeof(char) + strlen(file->key) +4 +1);
  response[0] = '\0';
  
  if (n_file>1)
    strcat (response, " ");
 
  strcat (response, file->filename);
  strcat (response, " ");
  
  char* size = malloc(20 * sizeof(char));//TODO log int
  sprintf(size,"%d",file->size);
  strcat (response, size);
  free(size);
  strcat (response, " ");
  
  char* piece_size = malloc(20 * sizeof(char)); // TODO log int
  sprintf(piece_size,"%d",file->piece_size);
  strcat (response, piece_size);
  strcat (response, " ");
 
  strcat (response, file->key);
  free(piece_size);
  //  printf ("the response to add !!:%s\n", response);

  return response;
}



/**
 * This function is used as a pointer function in the next function when this one reads all the HashTable hash_filekeys
 * It adds a "peer IP:peer port" in the response message
 *
 * @param void*
 * @param void*
 * @param void*
 *
 **/
void HashExtractPeer(void* key, void* value, void * response_msg) {
  char buf_port[6];
  char* c_key=(char*)key;
  c_key=NULL;
  struct PeerData* peer_data = (struct PeerData*)value;
  char* peer_IP = inet_ntoa(peer_data->sock_addr.sin_addr);
  strcat((char *)response_msg, peer_IP);
  strcat((char *)response_msg, ":"); 
  snprintf(buf_port,6,"%d", peer_data->port);
  strcat((char *)response_msg, buf_port);
  strcat ((char *)response_msg, " ");
}


/**
 * This function send the list of the peers who has got the file defined by the key given (or part of the file)
 *
 * @param const char*, the message to threat 
 * @param regmatch_t*
 * @param int
 *
 **/
char* sendPeersList (const char* str_request, regmatch_t* pmatch, int piece){
  char* key;
  int start = pmatch[piece].rm_so;
  int end = pmatch[piece].rm_eo;
  size_t size = end - start;	  
  char* response_msg = NULL;

  key = malloc (sizeof (*key) * (size + 1));
  if (key){
    //key is the value of the key from the file the peer wants
    strncpy (key, &str_request[start], size);
    key[size] = '\0';
    //    printf ("__getfile_key__ : %s\n", key);
    //if the key exists give the list of "peers IP:Peer port associated"
    if (g_hash_table_lookup_extended (hash_filekeys, key, NULL, NULL) != 0){
      printf ("peers fonded for the key : %s \n", key); 
      GHashTable* existing_list_of_peers = g_hash_table_lookup (hash_filekeys, key);
      response_msg = malloc(9*sizeof(char) + strlen(key));
      strcpy (response_msg, "peers ");
      strcat (response_msg, key);  
      strcat (response_msg, " [");
      //add all the couples "peer IP:peer port" in the response message
      response_msg=realloc(response_msg, strlen(response_msg) + g_hash_table_size(existing_list_of_peers)*23*sizeof(char));
      g_hash_table_foreach(existing_list_of_peers, HashExtractPeer, response_msg);
      response_msg[strlen(response_msg)-1]='\0';
      strcat (response_msg, "]");
      printf ("RESPONSE MSG : < %s >\n", response_msg);
    }
    //there are no peers in the hash_filekeys
    else {
      printf ("no peers concerned by the key : %s\n", key);
      response_msg = malloc(9*sizeof(char) + strlen(key) +1);
      strcpy (response_msg, "peers ");
      strcat (response_msg, key);
      strcat (response_msg, " []");
      printf ("RESPONSE MSG : < %s >\n", response_msg);
    }
    free(key);
  }
  return response_msg;
}
