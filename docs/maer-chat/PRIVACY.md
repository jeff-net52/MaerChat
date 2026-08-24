# Politique de confidentialité provisoire de Maer Chat

> **Statut : projet de transparence mis à jour le 24 août 2026 — incomplet et
> non prêt comme notice juridique finale.** L’identité de l’exploitant et la
> localisation générale de l’hébergement sont renseignées. Les rôles précis par
> traitement, les durées de conservation, le téléphone, le DPO éventuel et les
> sous-traitants ultérieurs restent à confirmer avant l’ouverture commerciale.

## 1. Périmètre et responsables

Cette politique couvre l’application Android Maer Chat et l’utilisation du
service de messagerie MAER auquel elle se connecte. Le serveur XMPP configuré
par défaut dans le projet est `contacts.chaumont.me`, sous réserve d’évolution
de l’offre.

Le logiciel est un client XMPP libre. D’autres opérateurs peuvent exploiter des
serveurs compatibles et traiter les données selon leurs propres politiques.

L’exploitant technique et commercial déclaré est :

- **Emilien REEVES**, entrepreneur individuel, nom commercial **MAER
  Engineering** ;
- **16 ZI Saint Roch Box 1, 52340 Biesles, France** ;
- SIRET **523 560 175 00045** ;
- contact vie privée et exercice des droits : **contact@maer.fr**, sous réserve
  de confirmation ;
- téléphone et délégué à la protection des données : **à compléter**.

Pour les comptes fournis à une organisation, cette dernière peut être
responsable du traitement des communications de ses utilisateurs et MAER
Engineering son sous-traitant technique. MAER Engineering peut parallèlement
être responsable de ses traitements propres, par exemple la relation client,
la facturation, la sécurité de son infrastructure et la réponse aux obligations
légales. Cette répartition doit être déterminée traitement par traitement et
formalisée dans le contrat, notamment par des clauses conformes à l’article 28
du RGPD lorsque MAER agit comme sous-traitant.

## 2. Données susceptibles d’être traitées

Selon les fonctions utilisées et les capacités du serveur, l’application ou le
service peuvent traiter :

- les données de compte : identifiant XMPP (JID), nom affiché, avatar,
  paramètres et informations d’abonnement nécessaires à l’autorisation du
  service ;
- les données d’authentification : secret utilisé par l’application pour
  authentifier le compte et données techniques produites par le mécanisme
  d’authentification ;
- le carnet XMPP, les groupes, les blocages, les présences, les appareils et
  les clés publiques de chiffrement ;
- les messages, réactions, corrections, accusés de réception et pièces jointes,
  avec leur état de synchronisation ;
- les données d’appel audio/vidéo et informations techniques nécessaires à
  l’établissement d’une communication lorsque cette fonction est utilisée ;
- les métadonnées de communication, telles que les identifiants des
  correspondants, dates et heures, adresses réseau, tailles, état de livraison,
  informations de connexion et caractéristiques du client ;
- les diagnostics que l’utilisateur choisit de consulter ou de transmettre ;
- les autorisations et données du téléphone auxquelles l’utilisateur donne
  accès pour une action précise, par exemple caméra, microphone, contacts,
  fichiers ou localisation.

Le mot de passe du compte est conservé localement sous une forme chiffrée au
moyen d’Android Keystore. Il est utilisé pour l’authentification XMPP via une
connexion TLS ; le traitement exact côté serveur dépend du mécanisme SASL
négocié et de la configuration de l’opérateur.

## 3. Finalités

Ces données sont susceptibles d’être utilisées pour :

- créer, autoriser et administrer le compte et l’abonnement ;
- connecter l’application, acheminer et synchroniser les communications ;
- afficher les contacts, groupes, avatars, présences et appareils ;
- proposer les pièces jointes, sauvegardes, appels et notifications demandés ;
- assurer la sécurité, prévenir les abus et diagnostiquer les incidents ;
- exploiter, maintenir et améliorer la fiabilité technique du service ;
- répondre aux demandes de support et aux obligations légales applicables.

Toute utilisation à des fins de mesure d’audience, prospection, profilage ou
amélioration produit doit être décrite séparément si elle est ajoutée. La
variante `conversations/free` examinée ne déclare pas de SDK publicitaire ou
d’analyse dans ses dépendances de compilation ; cette observation ne couvre pas
les journaux du serveur et doit être revérifiée à chaque version distribuée.

## 4. Bases juridiques à déterminer

Le responsable de traitement doit rattacher chaque finalité à une base
juridique adaptée et la documenter avant publication. Selon la situation réelle,
il peut notamment s’agir de l’exécution du contrat de service, du respect d’une
obligation légale, de l’intérêt légitime dûment mis en balance ou du
consentement lorsqu’il est requis.

Le présent projet ne choisit pas ces bases à la place de l’opérateur et ne doit
pas être publié sans cette analyse.

## 5. Stockage, destinataires et sous-traitants

L’application stocke localement les comptes, préférences, contacts, messages,
pièces jointes et clés nécessaires à son fonctionnement. La sauvegarde
automatique Android est désactivée dans la configuration actuelle. Une
sauvegarde manuelle chiffrée peut être créée à la demande de l’utilisateur ; sa
protection dépend aussi de la robustesse de la phrase secrète choisie.

Les données des serveurs MAER sont hébergées en France sur une infrastructure
exploitée en interne par MAER Engineering. Cette indication n’exclut pas
d’éventuels fournisseurs de réseau, DNS, certificats, sauvegarde ou autres
sous-traitants : leur liste exacte doit être vérifiée et publiée.

Le serveur peut conserver des données de compte, des métadonnées, des messages
hors ligne ou archivés et des fichiers selon les modules activés. Les durées et
règles de suppression doivent être ajoutées ici après vérification de la
configuration effective des serveurs et sauvegardes.

Les destinataires peuvent comprendre :

- les correspondants et groupes choisis par l’utilisateur ;
- le personnel habilité de l’opérateur dans la stricte mesure nécessaire ;
- les hébergeurs, fournisseurs réseau, relais de fichiers, de notifications ou
  d’appels effectivement utilisés ;
- les autorités ou tiers lorsque la loi l’impose.

La liste exacte des sous-traitants, leur localisation, leurs fonctions et les
garanties encadrant d’éventuels transferts internationaux restent à compléter.

## 6. Chiffrement et limites

TLS protège la connexion entre l’application et le serveur lorsqu’il est
correctement configuré et validé. Il ne constitue pas, à lui seul, un
chiffrement de bout en bout.

OMEMO peut protéger le contenu de certaines conversations de bout en bout
lorsqu’il est activé et que les appareils concernés sont correctement gérés et
vérifiés. Toutes les conversations, métadonnées, sauvegardes, pièces jointes ou
fonctions d’appel ne bénéficient pas automatiquement de cette protection. Les
indicateurs de sécurité affichés dans l’application décrivent l’état applicable
à la conversation en cours.

Même lorsqu’un contenu est chiffré de bout en bout, l’infrastructure peut devoir
traiter des métadonnées nécessaires à l’acheminement, telles que les adresses
XMPP, horaires, tailles et informations de connexion.

## 7. Durées de conservation

Les paramètres et données locales restent en principe sur l’appareil jusqu’à
leur suppression par l’utilisateur, la suppression du compte dans
l’application ou la désinstallation, sous réserve des fichiers exportés ou
sauvegardes manuelles conservés séparément.

Les durées applicables aux comptes, journaux, archives de messages, fichiers,
sauvegardes serveur, tickets de support et données de facturation doivent être
définies catégorie par catégorie par l’opérateur. Aucun délai n’est inventé
dans ce projet.

## 8. Choix et droits des personnes

Selon la réglementation applicable et la situation, une personne peut disposer
de droits d’accès, rectification, effacement, limitation, opposition,
portabilité, retrait du consentement et réclamation auprès de l’autorité de
contrôle compétente.

Les modalités pratiques, le délai de réponse, les justificatifs éventuellement
nécessaires et l’adresse de contact doivent être renseignés avant publication.
Lorsque le compte est fourni par une organisation, celle-ci peut aussi devoir
participer au traitement de la demande.

La suppression locale d’une conversation ou de l’application ne garantit pas,
à elle seule, l’effacement des copies déjà livrées aux correspondants, des
exports, des sauvegardes ou des données soumises à une obligation légale de
conservation.

## 9. Autorisations Android

Les autorisations sensibles doivent être demandées au moment où une fonction en
a besoin. Refuser une autorisation peut désactiver la fonction correspondante
sans nécessairement empêcher l’utilisation du reste de la messagerie. Les
autorisations accordées peuvent être revues dans les réglages Android.

## 10. Évolutions et informations à publier

Chaque changement significatif du service, du serveur, des dépendances de
télémétrie, des destinataires ou des finalités doit déclencher une mise à jour
de cette politique avec une date et un historique de versions.

Avant publication, l’opérateur doit au minimum compléter : son téléphone et le
contact DPO éventuel, les rôles et bases juridiques par finalité, les durées de
conservation, les sous-traitants, les transferts, la procédure d’exercice des
droits, le public visé et les règles particulières éventuellement applicables
aux mineurs ou aux comptes professionnels.

Pour les principes de sécurité technique connus du projet, voir
[`SECURITY.md`](SECURITY.md). Les conditions provisoires du service figurent
dans [`CGU.md`](CGU.md).
