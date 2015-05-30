#include "compileExpressions.h"


//////////////////////////////////////////////////////////////////////
//-----------------Fonctions to compile the expressions-------------//
//////////////////////////////////////////////////////////////////////



/**
  * Definition des expressions reguleres du type : Announce, Look, Getfile, Update
  *
  * @param regex_t : qui va etre le resultat de la compilation de l'expression reguliere
  * @return 0 si l'expression rguliere proposee a bien ete compilee, -1 sinon
  **/
int compileExpressionAnnonce(regex_t* preg_announce){
  //< announce listen $Port seed [$Filename1 $Length1 $PieceSize1 $Key1 $Filename2 $Length2 $PieceSize2 $Key2 …] leech [$Key3 $Key4 …]
 
  const char *str_regex_annonce = "^announce listen (([[:digit:]]+)) seed \\[(([[:graph:]]+ [[:digit:]]+ [[:digit:]]+ [[:lower:][:digit:]]+)?( [[:graph:]]+ [[:digit:]]+ [[:digit:]]+ [[:lower:][:digit:]]+)*)\\] leech \\[(([[:lower:][:digit:] ]*))\\]$"; 
  if (regcomp (preg_announce, str_regex_annonce, REG_EXTENDED) == 0)
    return 0;
  return -1;
}


int compileExpressionLook(regex_t* preg_look){  
  //< look [$Criterion1 $Criterion2 …]
  
  const char *str_regex_look = "^look \\[(((filesize[<>=]\"[0-9]+\" )*)((filename=\"([[:graph:]]+)\")|(filesize[<>=]\"[0-9]+\")){1}+(( filesize[<>=]\"[0-9]+\")*))\\]$";
  if (regcomp (preg_look, str_regex_look, REG_EXTENDED) == 0)
    return 0;
  return -1;
}


int compileExpressionGetfile(regex_t* preg_getfile){
  //< getfile $Key
  
  const char *str_regex_getfile = "^getfile (([[:lower:][:digit:]]+))$";
  if (regcomp (preg_getfile, str_regex_getfile, REG_EXTENDED) == 0)
    return 0;
  return -1;
}



int compileExpressionUpdate(regex_t* preg_update){
  //< update seed [$Key1 $Key2 $Key3 …] leech [$Key10 $Key11 $Key12 …]

  const char *str_regex_update = "^update seed \\[([[:lower:][:digit:] ]*)\\] leech \\[(([[:lower:][:digit:] ]*))\\]$";
  if (regcomp (preg_update, str_regex_update, REG_EXTENDED) == 0)
    return 0;
  return -1;
}


/**
  * Compile la regex passee en parametre. Permet de compiler une seul fois la regex pour qu'elle soit prete a utilisation
  *
  * @param struct regexData * regex_data : Regex a compiler.
  * @return 0 si success, -1 si echec.
  **/
int compileAllExpressions(struct regexData * regex_data){
  if (compileExpressionAnnonce(regex_data->preg_announce) == 0
      && compileExpressionLook(regex_data->preg_look) == 0 
      && compileExpressionGetfile(regex_data->preg_getfile) == 0 
      && compileExpressionUpdate(regex_data->preg_update) == 0)
    return 0;
  return -1;
}



/**
  * Initialise la regex passe en parametre.
  * L'argument doit etre alloue au prealable.
  *
  * @param struct regexData * regex_data : regex a initialiser
  * @return 0 si initialisation reussit, -1 si echec.
  **/

int initRegexData(struct regexData * regex_data){
  regex_data->preg_announce = malloc(sizeof(regex_t)); 
  regex_data->preg_look = malloc(sizeof(regex_t)); 
  regex_data->preg_getfile = malloc(sizeof(regex_t)); 
  regex_data->preg_update = malloc(sizeof(regex_t)); 
 
  if (compileAllExpressions(regex_data) == 0)
    return 0;
  else {
    printf("Error while compiling the regex expressions\n");
    return -1;
  }
}


/**
  * Libere la memoire de la structure passe en parametre
  *
  * @param struct regexData * regex_data , regex a desallouer
  **/
void freeRegexData(struct regexData * regex_data){
  regfree (regex_data->preg_announce);
  free(regex_data->preg_announce);
  regfree (regex_data->preg_look);
  free(regex_data->preg_look);
  regfree (regex_data->preg_getfile);
  free(regex_data->preg_getfile);
  regfree (regex_data->preg_update);
  free(regex_data->preg_update);
		
  free(regex_data);
}
