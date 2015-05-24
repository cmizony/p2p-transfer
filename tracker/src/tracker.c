#include <unistd.h>
#include <time.h>
#include"tracker.h"
#include "compileExpressions.h"


void error(char *msg){
	perror(msg);
	exit(1);
}

/*
 * Fonction utilisé par la glib pour libérer la structure file
 *
 */
void freeFile (void* f,void* data) {
	data=NULL;
    free(((struct File*)f)->filename);
    free(((struct File*)f)->key);
}

/*
 * Gere la deconnexion d'un peer si le timeout est depassé
 * Utilise les pointeurs de foncion de la glib
 */
int checkTimeoutHashPeersIntoFileKeys (void* key,void* value,void* user_data) {
	struct PeerData* peer = (struct PeerData*)value;
	unsigned int timeout = *((int*) user_data); 
	return ((time(NULL) - peer->timestamp) > (3 * timeout));
}

/*
 * Gere la deconnexion d'un peer si le timeout est depassé
 * Utilise les pointeurs de foncion de la glib
 */
int checkTimeoutHashFileKeys (void* key,void* value,void* user_data) {
	unsigned int size,i;
	if (value == NULL) 
		return TRUE;
	g_hash_table_foreach_remove((GHashTable*)value,checkTimeoutHashPeersIntoFileKeys,user_data); 
	size = g_hash_table_size ((GHashTable*)value);

	if (size == 0)  // Suppression fichier si plus de peers l'a
		for (i=0;i < files->len; i++)
			if (strcmp(((struct File*)g_ptr_array_index(files,i))->key,(char*)key) == 0)
				 g_ptr_array_remove_index(files,i);
	return (size == 0);
}

/*
 * Gere la deconnexion d'un peer si le timeout est depassé
 * Utilise les pointeurs de foncion de la glib
 */
void checkTimeout(int timeout) {
	g_hash_table_foreach_remove(hash_filekeys,checkTimeoutHashFileKeys,&timeout);
	g_hash_table_foreach_remove(hash_peers,checkTimeoutHashPeersIntoFileKeys,&timeout);
}

/* USER functions */
/**
 * Parse et et traite les parametre passés au lancement du serveur.
 * Les structure passées en parametre sont modifier en fonction des arguments.
 *
 * @param struct TrackerData* tracker_data Structure de données contenant les informations utiles du serveur
 * @param int argc , nomrbe d'argument
 * @param char **argv , Tableau contenant les arguments du programme
 **/
void parseParameter(struct TrackerData* tracker_data, int argc, char **argv){
  if (argc >= 2)
    {
      tracker_data->port = atoi(argv[1]);
      if (tracker_data->port < 60000 || tracker_data->port > 60025)
	{
	  fprintf(stderr,"ERROR, port must be between 60000 and 60025\n");
	  exit (1);
	}
      tracker_data->backlog_queue= 5;
    }
}	
/**
 * Initialise les informations du tracker. Les données sont stockées dans la structure passée en paramètre
 *
 **/
void initTrackerData (struct TrackerData** tracker_data) {
	*tracker_data = malloc(sizeof(struct TrackerData));
	(*tracker_data)->regex_data = malloc(sizeof(struct regexData));
	bzero((char *) &((*tracker_data)->addr), sizeof((*tracker_data)->addr));
	initRegexData((*tracker_data)->regex_data);

	pthread_mutex_init(&mutex_filekeys, NULL);
	pthread_mutex_init(&mutex_files, NULL);
}

/**
 * Fonction d'initialisation d'un client
 *
 **/
void initPeerData (struct PeerData** peer_data) {
	*peer_data = malloc (sizeof (struct PeerData));
	(*peer_data)->size_addr=sizeof((*peer_data)->sock_addr);
	(*peer_data)->timestamp=time(NULL);
}

/**
 * Initialise la socket de connexion du serveur.
 *
 **/
void startConnexion(struct TrackerData *tracker_data) {
	tracker_data->sock = socket(AF_INET,SOCK_STREAM , 0);
	if (tracker_data->sock < 0) error("ERROR opening socket server socket");

	tracker_data->addr.sin_family = AF_INET;
	tracker_data->addr.sin_addr.s_addr = INADDR_ANY;
	tracker_data->addr.sin_port = htons(tracker_data->port);

	if (bind(tracker_data->sock,(struct sockaddr *) &(tracker_data->addr), sizeof(tracker_data->addr)) < 0) 
		error("ERROR on binding server socket");

	listen(tracker_data->sock,tracker_data->backlog_queue); 
}
/**
 * Le serveur attend une connexion sur sa socket.
 * Fonction bloquante.
 * Lors d'une connexion, un struct PerrData est instancié et retourné a la fonction appelante
 *
 * @param struct TrackerData *tracker_data, Information sur le tracker (navette)
 **/
struct PeerData* waitConnexion(struct TrackerData *tracker_data){
	struct PeerData* peer;
	initPeerData(&peer);
	peer->regex_data_ptr = tracker_data->regex_data;
	peer->sock = accept(tracker_data->sock,(struct sockaddr *) &(peer->sock_addr), &(peer->size_addr));
	return peer;
}
/**
 * Fonction permettant de lire le message reçus sur la socket
 * Traite ensuite ce message et répond au peer par la socket
 * Puis la socket est fermée
 **/
#define SIZE_BUFFER 4096
void start_thread (void * peer_data) {
	struct PeerData* peer = (struct PeerData *) peer_data;
	printf("Peer %s connected\n",inet_ntoa(peer->sock_addr.sin_addr));
	if (peer->sock < 0)
		return;
	int buffer_size = SIZE_BUFFER;
	char buffer[buffer_size];

	bzero(buffer,buffer_size);

	int len = read(peer->sock, buffer, SIZE_BUFFER);
	if (len < 0) error("ERROR reading from socket");
	if (strstr(buffer,"\n") != NULL)buffer[len-2]='\0';

	printf("Message received: %s___\n", buffer);

	char* response_msg = treatMessage(peer, buffer);	

	/* Ecriture socket */
	int n = write(peer->sock,response_msg,strlen(response_msg));

	if (n < 0) error("ERROR writing to socket\n");

	close(peer->sock);
	free(response_msg);
	printf("Peer %s disconnected\n\n",inet_ntoa(peer->sock_addr.sin_addr));
}

/**
 *  Fonction permetant de charger le fichier de configuration du tracker
 *  Les configurations sont sauvegardés dans la structure tracker
 */
void parseConfigFile( char* file_name, struct TrackerData* tracker) 
{
	char* comand = malloc(sizeof(char) * 256 );
	char* value = malloc(sizeof(char) * 256 );
	int ret = 42;  
	FILE* file =  fopen(  file_name, "r");
	if ( file == NULL)
	{
		printf("[ERROR] Erreur a l'ouverture du fichier de configuration...\n");
		return;
	}
	while ( (ret =fscanf( file, "%s = %s",comand,value)) != EOF)
	{ 
	  if ( strcmp(comand,"port") == 0 )
	      tracker->port = atoi(value);
	  else if ( strcmp(comand,"backlog_queue") == 0 )
	      tracker->backlog_queue = atoi(value);
	  else if ( strcmp(comand,"upload_timeout") == 0 )
	      tracker->timeout = atoi(value);
	  else
	      printf("[WARNING] parametre du fichier de configuration non reconnu : %s <-> %s \n",comand, value );
	}
	fclose( file );

}
/**
 * Fonction principale du serveur.
 * Accepte des parametre en entrée
 * Au lancement du programme, le serveur initialise ses structures
 de donnée et effectue une attante active sur le socket nouvellement crée.
 * Lors d'une connexion, un thread gérant le nouveau client est lancé.
 * Le serveur retourne alros dasn une attente active et bloquante d'une nouvelle connexion.
 **/
int main (int argc, char **argv) {
	// parse parameters (port)
	// default port : xxxx
	// nslookup nommachine (pour avoir l'ip)

	struct PeerData* peer_new;
	struct PeerData* peer_tmp;
	struct TrackerData* tracker_data;
	hash_filekeys = g_hash_table_new_full((GHashFunc)g_str_hash,(GEqualFunc)g_str_equal,(GDestroyNotify)g_free,(GDestroyNotify)g_free);

	files = g_ptr_array_new_with_free_func((GDestroyNotify) g_free);
	hash_peers = g_hash_table_new_full((GHashFunc)g_str_hash,(GEqualFunc)g_str_equal,(GDestroyNotify)g_free,(GDestroyNotify)g_free);

	g_thread_init (NULL);
	initTrackerData(&tracker_data);

	parseConfigFile( "tracker.conf", tracker_data);
	parseParameter(tracker_data,argc,argv);


	startConnexion(tracker_data);
	printf("Tracker started on port %d\n",tracker_data->port);

	while (1) {
		peer_new=waitConnexion(tracker_data);
		//checkTimeout(time(NULL)); TODO 
		peer_tmp=g_hash_table_lookup (hash_peers, inet_ntoa(peer_new->sock_addr.sin_addr));
		if (peer_tmp != NULL){
			peer_new->port=peer_tmp->port;
			peer_new->timestamp=peer_tmp->timestamp;
		}
		else
			g_hash_table_insert (hash_peers, inet_ntoa(peer_new->sock_addr.sin_addr), peer_new);
			

		g_thread_create((GThreadFunc)start_thread,(gpointer)peer_new, TRUE, NULL);

	}
	g_free(hash_filekeys);
	g_free(hash_peers);
	g_ptr_array_foreach(files,freeFile,NULL);
	g_ptr_array_free(files,TRUE);

	pthread_mutex_destroy( &mutex_filekeys ); 
	pthread_mutex_destroy( &mutex_files ) ;
	free(tracker_data);
	freeRegexData(tracker_data->regex_data);

	return 0;
}
