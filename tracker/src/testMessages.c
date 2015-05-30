#include <assert.h>
#include "treatMessage.h"


void freeFile (void* f,void* data) {
        data=NULL;
	free(((struct File*)f)->filename);
	free(((struct File*)f)->key);
}

//////////////////////////////////////////////////////////////////////
//---------------------Tests with some messages---------------------//
//////////////////////////////////////////////////////////////////////

int main (void)
{ 
        //initializing the global variables : files & hash_filekeys 
        files = g_ptr_array_new_with_free_func((GDestroyNotify) g_free);
	hash_filekeys = g_hash_table_new_full((GHashFunc)g_str_hash,(GEqualFunc)g_str_equal,(GDestroyNotify)g_free,(GDestroyNotify)g_free);  

	//initializing the regex_t
	struct regexData* regex_data = malloc(sizeof(struct regexData));
	initRegexData(regex_data);

	//initializing two PeerData 
	struct PeerData* peer_data = malloc(sizeof(struct PeerData));
	peer_data->regex_data_ptr = regex_data;
	inet_aton("63.161.169.137", &peer_data->sock_addr.sin_addr);
	struct PeerData* peer_data1 = malloc(sizeof(struct PeerData));
	peer_data1->regex_data_ptr = regex_data;
	inet_aton("212.161.169.137", &peer_data1->sock_addr.sin_addr);


	//writing expression
	const char* str_request0 = "announce listen 60018 seed [jed 23 2 dj2 jed 33 223443 djkbdd445xkjc] leech [cle1 a etjdd33]";   
	const char* str_request0bis = "announce listen 60017 seed [abc 23 2 dj2] leech [a]";   
	
	const char* str_request1 = "look [filesize>\"64\" filesize<\"32\" filename=\"jed\"]";
	const char* str_request1bis = "look [filesize>\"2\"]";
	const char* str_request1ter = "look [filename=\"jed\" filesize<\"40\" filesize=\"33\"]";
	const char* str_request1qua  ="look [filesize>\"2\" filesize<\"32\" filesize<\"32\" filesize<\"32\"]";
	const char* str_request1qui  ="look [filename=\"jed\"]";
	const char* str_request1six  ="look [filesize>\"2\" filesize<\"32\" filename=\"jed\" filesize<\"32\" filesize<\"32\"]";
	
       	const char* str_request2 = "getfile 8905e92afeb80fc7722ec89eb0bf0966";
       	const char* str_request2bis = "getfile a";
	const char* str_request2ter = "getfile cle1";
	
	const char* str_request3 = "update seed [djhdd23dkjhd2 dskshdj3sjdh3jshd3] leech [a delhd433jh2 sdjhgd44432dskj3]";
	//wrong expressions
	const char* str_request4 = "update seed [djhdd23dkjhd2 dskshdj3sjdh3jshd3R] leech [a delhd433jh2 sdjhgd44432dskj3]";
	const char* str_request5 = "errorerrorerrorerrorerror";
	

	//testing if these expressions are valid or not
	char * response= NULL;
	
	assert (strcmp((response=treatMessage(peer_data, str_request0)),"ko") != 0); free(response);
	assert (strcmp((response=treatMessage(peer_data1, str_request0bis)),"ko")!=0);free(response);
	
	assert (strcmp((response=treatMessage(peer_data, str_request1)), "ko")!=0);free(response);
      	assert (strcmp((response=treatMessage(peer_data, str_request1bis)), "ko") !=0);free(response);
       	assert (strcmp((response=treatMessage(peer_data, str_request1ter)),"ko") !=0);free(response);
       	assert (strcmp((response=treatMessage(peer_data, str_request1qua)), "ko") !=0);free(response);
	assert (strcmp((response=treatMessage(peer_data, str_request1qui)), "ko") !=0);free(response);
	assert (strcmp((response=treatMessage(peer_data, str_request1six)),"ko") !=0);free(response);
	       	
	assert (strcmp((response=treatMessage(peer_data1, str_request2)),"ko") !=0);free(response);
       	assert (strcmp((response=treatMessage(peer_data1, str_request2bis)),"ko") !=0);free(response);
      	assert (strcmp((response=treatMessage(peer_data1, str_request2ter)),"ko") !=0);free(response);
	
	assert (strcmp((response=treatMessage(peer_data, str_request3)),"ko") !=0);free(response);
	assert (strcmp((response=treatMessage(peer_data, str_request4)),"ko") ==0);free(response);
	assert (strcmp((response=treatMessage(peer_data, str_request5)),"ko") ==0);free(response);


	
	/*
	 *verifying if the structures are well completed for the "annonce" message
	 */
	//the port numbers are well added ?
	assert (peer_data->port == 60018);
	assert (peer_data1->port == 60017);
	
	//the files given in the "annonce" message are well added in the files structure?
	assert(files->len==2);
	assert (strcmp(((struct File*)g_ptr_array_index(files, 0))->filename, "jed")==0);
	assert (((struct File*)g_ptr_array_index(files, 0))->size == 23);
	assert (((struct File*)g_ptr_array_index(files, 0))->piece_size ==2);
	assert (strcmp(((struct File*)g_ptr_array_index(files, 0))->key, "dj2")==0);
	assert (strcmp(((struct File*)g_ptr_array_index(files, 1))->filename, "jed")==0);
	assert (((struct File*)g_ptr_array_index(files, 1))->size == 33);
	assert (((struct File*)g_ptr_array_index(files, 1))->piece_size == 223443);
	assert (strcmp(((struct File*)g_ptr_array_index(files, 1))->key, "djkbdd445xkjc")==0);



	//all the keys are well added in the hash_filekeys structure? 
	assert(g_hash_table_lookup (hash_filekeys, "dj2")!=NULL);
	GHashTable* IPnumber1 = g_hash_table_lookup (hash_filekeys, "dj2");
	assert(g_hash_table_size(IPnumber1) == 2);
	struct PeerData* peer1 = g_hash_table_lookup(IPnumber1, inet_ntoa(peer_data->sock_addr.sin_addr));
	struct PeerData* peer1bis = g_hash_table_lookup(IPnumber1, inet_ntoa(peer_data1->sock_addr.sin_addr));
	assert(peer1 != NULL);
	assert(peer1bis!=NULL);
	assert(peer1->port == 60018);   
	assert(peer1bis->port == 60017);   

	assert(g_hash_table_lookup (hash_filekeys, "djkbdd445xkjc")!=NULL);
	GHashTable* IPnumber2 = g_hash_table_lookup (hash_filekeys, "djkbdd445xkjc");
	assert(g_hash_table_size(IPnumber2) == 1);
	struct PeerData* peer2 = g_hash_table_lookup(IPnumber2, inet_ntoa(peer_data->sock_addr.sin_addr));
	assert(peer2 != NULL);
	assert(peer2->port == 60018);

	assert(g_hash_table_lookup (hash_filekeys, "cle1")!=NULL);
	GHashTable* IPnumber3 = g_hash_table_lookup (hash_filekeys, "cle1");
	assert(g_hash_table_size(IPnumber3) == 1);
	struct PeerData* peer3 = g_hash_table_lookup(IPnumber3, inet_ntoa(peer_data->sock_addr.sin_addr));
	assert(peer3 != NULL);
	assert(peer3->port == 60018); 

	assert(g_hash_table_lookup (hash_filekeys, "a")!=NULL);
	GHashTable* IPnumber4 = g_hash_table_lookup (hash_filekeys, "a");
	assert(g_hash_table_size(IPnumber4) == 2);
	struct PeerData* peer4 = g_hash_table_lookup(IPnumber4, inet_ntoa(peer_data->sock_addr.sin_addr));
	struct PeerData* peer4bis = g_hash_table_lookup(IPnumber4, inet_ntoa(peer_data1->sock_addr.sin_addr));
	assert(peer4 != NULL);
	assert(peer4bis!=NULL);
	assert(peer4->port == 60018);   
	assert(peer4bis->port == 60017);   

	assert(g_hash_table_lookup (hash_filekeys, "etjdd33")!=NULL);
	GHashTable* IPnumber5 = g_hash_table_lookup (hash_filekeys, "etjdd33");
	assert(g_hash_table_size(IPnumber5) == 1);
	struct PeerData* peer5 = g_hash_table_lookup(IPnumber5, inet_ntoa(peer_data->sock_addr.sin_addr));
	assert(peer5 != NULL);
	assert(peer5->port == 60018); 



	/*
	 *verifying if the structures are well completed for the "update" message 
	 */
	assert(g_hash_table_lookup (hash_filekeys, "sdjhgd44432dskj3")!=NULL);
	GHashTable* IPnumber6 = g_hash_table_lookup (hash_filekeys, "sdjhgd44432dskj3");
	assert(g_hash_table_size(IPnumber6) == 1);
	struct PeerData* peer6 = g_hash_table_lookup(IPnumber6, inet_ntoa(peer_data->sock_addr.sin_addr));
	assert(peer6 != NULL);
	assert(peer6->port == 60018); 

	assert(g_hash_table_lookup (hash_filekeys, "delhd433jh2")!=NULL);
	GHashTable* IPnumber7 = g_hash_table_lookup (hash_filekeys, "delhd433jh2");
	assert(g_hash_table_size(IPnumber7) == 1);
	struct PeerData* peer7 = g_hash_table_lookup(IPnumber7, inet_ntoa(peer_data->sock_addr.sin_addr));
	assert(peer7 != NULL);
	assert(peer7->port == 60018); 

	assert(g_hash_table_lookup (hash_filekeys, "dskshdj3sjdh3jshd3")!=NULL);
	GHashTable* IPnumber8 = g_hash_table_lookup (hash_filekeys, "dskshdj3sjdh3jshd3");
	assert(g_hash_table_size(IPnumber8) == 1);
	struct PeerData* peer8 = g_hash_table_lookup(IPnumber8, inet_ntoa(peer_data->sock_addr.sin_addr));
	assert(peer8 != NULL);
	assert(peer8->port == 60018); 

	assert(g_hash_table_lookup (hash_filekeys, "djhdd23dkjhd2")!=NULL);
	GHashTable* IPnumber9 = g_hash_table_lookup (hash_filekeys, "djhdd23dkjhd2");
	assert(g_hash_table_size(IPnumber9) == 1);
	struct PeerData* peer9 = g_hash_table_lookup(IPnumber9, inet_ntoa(peer_data->sock_addr.sin_addr));
	assert(peer9 != NULL);
	assert(peer9->port == 60018); 



	/*
	 * 9 keys are added???
	 */
	assert (g_hash_table_size(hash_filekeys) == 9);

	printf("\nAll the structures have been well completed\n");


	/*
	 * free the structures allocated 
	 */
	//free files
	g_ptr_array_foreach(files,freeFile,NULL);
	g_ptr_array_free(files,TRUE);

	//free hash_filekeys
	g_free(hash_filekeys);
	
	//free the peerData
	free(peer_data);
	free(peer_data1);

	//free the regexData
	freeRegexData(regex_data);

	return EXIT_SUCCESS;
}
