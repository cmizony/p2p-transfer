#ifndef COMPILE_EXPRESSIONS
#define COMPILE_EXPRESSIONS

#include <stdio.h>
#include <stdlib.h>
#include <regex.h>


//////////////////////////////////////////////////////////////////////
//------Prototype fonctions to Compile the regex expressions--------//
//////////////////////////////////////////////////////////////////////



struct regexData{
  regex_t* preg_announce;
  regex_t* preg_look;
  regex_t* preg_getfile;
  regex_t* preg_update;
};


int compileExpressionAnnonce(regex_t*);
int compileExpressionLook(regex_t*);
int compileExpressionGetfile(regex_t*);
//int compileExpressionHave(regex_t*);
int compileExpressionUpdate(regex_t*);
int compileAllExpressions(struct regexData *);

int initRegexData(struct regexData *);
void freeRegexData(struct regexData *);




#endif
