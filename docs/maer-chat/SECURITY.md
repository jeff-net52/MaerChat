# Sécurité de Maer Chat

## Principes

- TLS correctement validé est obligatoire ; aucune option ne doit permettre de
  contourner une erreur de certificat pour le serveur Maer Chat.
- DNS SRV est utilisé lorsqu’il existe, puis le domaine XMPP et le port standard
  servent de repli.
- Les secrets de compte sont chiffrés avec une clé non exportable de l’Android
  Keystore et ne sont ni affichés ni journalisés.
- Les transferts applicatifs utilisent HTTPS ; STARTTLS XMPP reste géré par la
  socket du moteur et n’exige pas d’autoriser HTTP en clair.
- La variante distribuée ne contient aucun compte, mot de passe, certificat
  privé, clé de signature ou secret de service.

La clé locale ne demande pas de biométrie : le service doit pouvoir se
reconnecter en arrière-plan. Si le Keystore est invalidé ou si une sauvegarde est
restaurée sur un autre téléphone, l’utilisateur doit ressaisir le mot de passe.

## Journaux et diagnostic

Le diagnostic peut afficher le JID, le serveur résolu, le port, l’état TLS, la
ressource, les compteurs et les capacités annoncées. Il ne doit jamais afficher
le mot de passe, un jeton SASL FAST, une clé OMEMO privée ou le contenu brut des
stanzas. Avant de partager un diagnostic, l’utilisateur doit considérer le JID
et les noms de contacts comme des métadonnées personnelles.

## Sauvegardes

La sauvegarde automatique Android est désactivée dans le manifeste avec
`android:allowBackup="false"`. La base locale, les secrets et les préférences ne
doivent donc pas être exportés ou restaurés par le mécanisme de sauvegarde du
système.

La sauvegarde manuelle Conversations/CEB reste disponible :

- toute nouvelle exportation utilise le format CEB version 3 ;
- sa clé AES de 256 bits est dérivée avec PBKDF2-HMAC-SHA256 et 310 000
  itérations, puis le contenu est chiffré et authentifié avec AES-GCM ;
- l’import sait également lire les sauvegardes historiques de version 2 avec
  leur dérivation de clé d’origine, mais aucune nouvelle sauvegarde v2 n’est
  produite ;
- la phrase secrète transmise à la tâche d’import n’est jamais placée dans les
  données persistées par WorkManager. Elle est temporairement chiffrée par
  Android Keystore, référencée par l’identifiant de la tâche dans le répertoire
  interne exclu des sauvegardes, puis supprimée à la fin de la tâche ;
- les secrets de compte lus dans le CEB sont rechiffrés avec la clé Keystore de
  l’appareil avant leur insertion dans SQLite.

Un fichier CEB reste sensible malgré ce chiffrement : sa robustesse dépend aussi
de la qualité du mot de passe. La clé Android Keystore locale n’est jamais
exportée.

## Limites

Le chiffrement OMEMO protège le contenu compatible mais pas toutes les
métadonnées XMPP (adresses, horaires, taille et rythme des échanges). La sécurité
dépend aussi du système Android, du serveur, de la confiance accordée aux
appareils OMEMO et de la protection physique du téléphone.
