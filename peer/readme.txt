=========================================================
TRACKEIRB v1.0
=========================================================

---------------------------------------------------------
CONFIGURATION DU PEER

Dans ce même dossier se trouve le fichier peer.conf dans lequel sont définis :

     - local.storage : le dossier qui contient les fichiers à partager
     - maximum.connected.peers : le nombre maximum de connexions entrantes sur le peer
     - maximum.message.size : le nombre maximum de pièces par message
     - piece.size : la taille maximum d'une pièce (en Ko)
     - update.frequency : la fréquence de mise à jour auprès du tracker (en seconde)

     - tracker.host : l'IP du tracker
     - tracker.port : Le port sur lequel le tracker a lancé la connexion

     - peer.host : l'IP du peer
     - peer.port : le port sur lequel le peer va lancer la connexion.

Important : Il est nécessaire que le dossier pointé par "local.storage" existe (sinon une exception est levée).

---------------------------------------------------------

---------------------------------------------------------
LANCEMENT DU PEER

L'executable : "./launch.sh" créer les dossiers "downloads" et "downloads2" (pour le test de l'application avec un réseau à 2 pairs), compile les sources du peer et lance le peer en fonction des données définies dans le fichier de configuration.
---------------------------------------------------------


REMARQUE 

Il est conseillé de naviguer dans les dossiers du peer en utilisant eclipse.


