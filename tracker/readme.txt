=========================================================
TRACKEIRB v1.0
=========================================================

---------------------------------------------------------
CONFIGURATION DU TRACKER

Dans le dossier src/ se trouve le fichier tracker.conf dans lequel sont définis :
     - port : le port sur lequel le tracker va lancer la connexion.
     - backlog_queue : longueur maximale  pour  la  file  des connexions  en  attente pour la fonction listen (le serveur reste multithreadé car cela correspond juste au temps de création des threads).
     - upload_timeout : si au bout de 3 fois cette valeur un peer n'a plu envoyé de messages alors il est considéré comme déconnecté.

---------------------------------------------------------

---------------------------------------------------------
COMPILATION DU TRACKER

Un Makefile est fourni dans le répertoire source.

De plus, il est necessaire d'avoir installé la librairie GLib car nous utilisons des structures qui lui sont propores. Il est possible de la télécharger sur le lien suivant : http://developer.gnome.org/glib/2.30/
---------------------------------------------------------


---------------------------------------------------------
LANCEMENT DU TRACKER

L'executable : "./tracker" qui lancera le tracker sur le port "port" défini dans tracker.conf sauf si on met un valeur en paramètre à l'executable, exemple : "./tracker 60001"
---------------------------------------------------------


---------------------------------------------------------
LANCEMENT DES TESTS

Après avoir fait un "make test", l'executable ./test lance un jeu de test (décrit dans testMessages.c) basé essentielement sur le tracker. Il permet de verifier le bon fonctionnement des réceptions et des réponses des requêtes au niveau du tracker.
---------------------------------------------------------


REMARQUE 

Le tracker doit être lancé avant le/les peers
