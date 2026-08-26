# Validation de Maer Chat

## Commandes reproductibles

```shell
./gradlew spotlessCheck
./gradlew testConversationsFreeDebugUnitTest
./gradlew lintConversationsFreeDebug
./gradlew assembleConversationsFreeDebug
./gradlew assembleConversationsFreeRelease
```

Les tests locaux ajoutés pour Maer Chat n'ouvrent aucune connexion et ne
nécessitent aucun identifiant :

- normalisation du JID de connexion ;
- chiffrement authentifié et migration des secrets de compte ;
- format CEB v3, dérivation PBKDF2-HMAC-SHA256 et lecture compatible des
  sauvegardes v2 ;
- stockage temporaire chiffré de la phrase secrète d’import hors des données
  WorkManager, liaison à l’identifiant de tâche et suppression ;
- persistance du brouillon via la sérialisation de la conversation ;
- politique TLS 1.2/1.3, refus du trafic en clair et autorités système uniquement ;
- permissions de notification et service XMPP non exporté dans le manifeste ;
- filtres de l’accueil (toutes, non lues, groupes et favorites), présentation
  du bouton Appels et choix audio/vidéo ;
- résolution des thèmes clair/sombre et des ressources après changement de
  configuration portrait/paysage ;
- garde statique contre un JID Maer personnel embarqué et contre le passage
  direct d'un mot de passe, d'une passphrase ou d'un jeton FAST à `Log`.

## État de la validation locale

La suite unitaire complète compte **178 tests réussis** avec `compileSdk` et
`targetSdk` réglés sur l’API 37. Les tests Android hors appareil s’exécutent avec
un runtime Robolectric API 36 ; ce niveau de runtime ne modifie pas la cible API
37 de l’application.

`lintConversationsFreeDebug` échoue actuellement sur **8 erreurs hors
baseline** : une alerte de version AGP et sept chaînes MAER inutilisées après la
simplification de la connexion. **645 constats amont** restent filtrés par la
baseline. `spotlessCheck` échoue également sur le format de 14 fichiers Java
préexistants. Ces dettes ne sont pas masquées et restent bloquantes pour la
publication, même si `lintVitalConversationsFreeRelease` est vert.

Une APK Debug de validation, non publiable, a été installée et démarrée sur un
émulateur Android API 31.
L’écran de connexion a été exercé avec des identifiants entièrement fictifs :
l’échec est affiché proprement, sans fermeture inattendue. Une capture demandée
avec `adb` sur ce build a produit **zéro octet**, ce qui confirme l’application
de `FLAG_SECURE` sur cet écran.

Un test instrumenté supplémentaire s’exécute sur l’AVD
`RTM_Tablet_API31` sous Android 12/API 31. Il lance réellement l’écran
Contacts, vérifie la présence des quatre entrées de navigation, bascule entre
Contacts et Groupes, contrôle la présence d’Appels sur l’accueil et confirme
que le logo emploie `FIT_CENTER`. Le dernier passage compte **1 test réussi**,
sans échec ni erreur.

Pour la version 0.5.0, l’identité Android reste `fr.maer.chat`, avec `minSdk` 23
et `targetSdk` 37. Le code visible attendu est `500` pour l’APK universelle et
`504` pour l’APK ARM64 : Gradle multiplie le `versionCode` 5 par 100 puis ajoute
le suffixe propre à l’ABI. Les anciennes APK 0.4.0 signées avec une clé de
développement et les APK 0.5.0 Debug sont explicitement **non publiables** ;
leurs anciennes empreintes ne constituent pas une preuve de Release.

Le désassemblage des deux DEX de la Release 0.2.0 après R8 ne contient aucun
appel émetteur à
`android.util.Log` ou `java.util.logging.Logger` ; seules des méthodes non
émettrices de configuration ou de test de niveau subsistent.

Les images de documentation [`login.png`](screenshots/login.png) et
[`login-error.png`](screenshots/login-error.png) ont donc été capturées avec un
build temporaire autorisant explicitement la capture. Ce build de documentation
n’est pas distribué et les images ne constituent pas un contournement présent
dans l’APK livrée.

Avec un téléphone ou émulateur détecté :

```shell
./gradlew connectedConversationsFreeDebugAndroidTest
adb install -r build/outputs/apk/conversationsFree/debug/*universal-debug.apk
```

## Matrice fonctionnelle

| Domaine | Validation attendue |
|---|---|
| Identifiant | `emilien` devient `emilien@xmpp.maer.fr`; une adresse complète ou une ressource est refusée |
| Authentification | succès, mauvais mot de passe, serveur indisponible et certificat invalide différenciés |
| TLS | nom d’hôte, chaîne, dates, TLS 1.2 minimum et aucune validation désactivée |
| Réseau | reconnexion, Wi-Fi ↔ mobile, reprise XEP-0198 et rattrapage MAM sans doublon |
| Messages | envoi/réception, accusés, marqueurs, correction et réponses selon capacités |
| OMEMO | deux comptes, plusieurs appareils, confiance et corps illisible sur le transport |
| Médias | image, vidéo, document, position et vocal ; upload et téléchargement HTTPS |
| Notifications | premier plan, arrière-plan, verrouillage, Doze, réponse rapide et marquer lu |
| UI | clair/sombre/système, grandes polices, rotation, petit écran et tablette |
| Brouillons | navigation, rotation, recréation de l’activité et redémarrage du processus |
| Confidentialité | recherche statique de secrets et absence d’identifiants dans les logs |

## Conditions des tests réels

Les tests réseau authentifiés nécessitent deux comptes dédiés et, pour OMEMO,
au moins deux appareils ou instances. Les secrets sont saisis manuellement ou
injectés par un coffre CI, jamais committés. Une fonction non testable faute de
capacité serveur est notée « non vérifiée » et non « réussie ».

À titre historique, une APK Debug ARM64 0.5.0 a été installée sur un Samsung
XCover 7 SM-G556B sous Android 16/API 36. Android a d’abord refusé la mise à
jour de la Release 0.4.0,
car sa signature de développement historique différait de la clé Debug locale.
Après autorisation explicite, la 0.4.0 et ses données locales ont été supprimées,
puis la 0.5.0 a été installée comme nouvelle application. Le gestionnaire de
paquets a confirmé `0.5.0+free`, code 504 ; l’activité `WelcomeActivity` et le
processus sont restés actifs et aucun plantage `AndroidRuntime` n’a été relevé.
Cet essai portait sur un artefact de test antérieur ; il ne qualifie pas le
commit courant et ne doit pas être cité comme validation d’une Release.

La capture automatisée de l’interface n’a pas été possible : `FLAG_SECURE`
protège l’écran et l’appareil s’est reverrouillé. Cette limite n’affecte ni
l’installation ni le contrôle du processus, mais l’apparence finale doit être
confirmée directement sur l’écran déverrouillé du téléphone.

Aucun compte XMPP authentifié n’était disponible pour la validation décrite dans
ce document. Les résultats hors ligne ne valident donc ni le chemin
d’authentification réel ni les capacités négociées après connexion au serveur.
L’essai physique valide l’installation neuve et le démarrage ; il ne valide pas
la migration des données depuis une version signée avec une autre clé et ne
remplace pas les tests authentifiés à deux comptes.

La livraison automatisée ne prétend donc pas valider, sans compte réel :

- l'authentification réussie, la reconnexion, XEP-0198 et le rattrapage MAM ;
- l'échange OMEMO de bout en bout entre plusieurs appareils ;
- l'upload HTTP, les appels et la push serveur de bout en bout ;
- les actions de notification dépendant d’un message réel ;
- la restauration visuelle exacte d'une activité de conversation après rotation
  ou destruction du processus.

Le dernier point est volontairement laissé au test instrumenté : l'activité de
conversation dépend du service XMPP et de son cycle Android complet. Le test
Robolectric couvre la couche persistée du brouillon et la résolution des ressources
après rotation, sans remplacer cette validation sur appareil. La garde des logs
couvre les secrets d'authentification directement passés à `Log`; un contrôle
`logcat` avec des comptes de test reste requis pour les valeurs construites
indirectement par le moteur XMPP amont.

Une validation complète avant production doit encore employer des comptes XMPP
dédiés sur plusieurs appareils physiques et couvrir MAM, OMEMO, la push,
l’upload et les appels de bout en bout.
