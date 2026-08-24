# Conditions d’utilisation provisoires du service Maer Chat

> **Statut : projet de travail mis à jour le 24 août 2026 — non opposable en
> l’état.** L’identité de l’opérateur et le périmètre logiciel/service sont
> renseignés. Le téléphone professionnel, le public B2B ou B2C, les tarifs, la
> durée, la résiliation et les niveaux de service restent à compléter et à faire
> valider avant ouverture commerciale.

## 1. Deux éléments juridiquement distincts

Maer Chat réunit deux éléments qui ne doivent pas être confondus :

1. **le logiciel Maer Chat**, logiciel libre dérivé de Conversations et
   distribué sous GNU General Public License version 3 ou ultérieure
   (GPLv3+) ;
2. **le service de messagerie MAER**, qui fournit notamment un compte et un
   accès à une infrastructure XMPP exploitée sous la responsabilité de son
   opérateur.

La GPLv3+ permet d’utiliser, d’étudier, de modifier et de redistribuer le
logiciel selon ses propres conditions. Elle n’exige pas l’achat d’une licence
logicielle auprès de MAER Engineering.

En revanche, l’accès au service de messagerie MAER nécessite un **compte
payant actif auprès de MAER Engineering**, selon l’offre et le contrat
commercial souscrits. Le paiement rémunère le service, son exploitation et les
prestations éventuellement associées ; il ne retire ni ne limite les droits
accordés sur le code par la GPLv3+.

## 2. Opérateur et documents contractuels

Le service est exploité par :

- **Emilien REEVES**, entrepreneur individuel ;
- nom commercial : **MAER Engineering** ;
- adresse : **16 ZI Saint Roch Box 1, 52340 Biesles, France** ;
- SIRET : **523 560 175 00045** ;
- contact et assistance : **contact@maer.fr** ;
- téléphone professionnel : **à compléter**.

Le bon de commande, le devis, le contrat d’abonnement ou tout autre document
commercial accepté avec l’opérateur précise notamment le prix, la facturation,
la durée, le renouvellement, la résiliation, l’assistance et, le cas échéant,
les engagements de disponibilité.

**En cas de différence avec le présent projet de texte, le contrat commercial
conclu avec l’opérateur prévaut pour les conditions du service.** La licence
GPLv3+ continue toutefois de régir le logiciel indépendamment de ce contrat.

## 3. Compte et accès au service

L’utilisateur doit disposer d’un compte autorisé et maintenir ses moyens
d’authentification confidentiels. Il doit signaler rapidement à l’opérateur
toute suspicion d’accès non autorisé et protéger les appareils sur lesquels le
compte est configuré.

Le compte est personnel ou affecté à une organisation selon le contrat
commercial. Le partage d’identifiants, l’usurpation d’identité ou le
contournement des mesures de contrôle d’accès sont interdits.

L’application peut être utilisée librement avec un serveur XMPP compatible,
sans compte ni paiement auprès de MAER Engineering. Cette liberté ne vaut pas
droit d’accès aux serveurs exploités par MAER : cet accès nécessite un compte
payant actif attribué dans le cadre d’une offre ou d’un contrat MAER.

## 4. Usage acceptable

Le service doit être utilisé conformément à la loi, aux droits des tiers et à
la destination prévue au contrat. Sont notamment interdits :

- l’envoi de contenus manifestement illicites ou l’organisation d’activités
  illicites ;
- le harcèlement, l’usurpation d’identité et les atteintes aux droits d’autrui ;
- la diffusion non sollicitée en masse, les logiciels malveillants et les
  tentatives de fraude ;
- les attaques, tests intrusifs non autorisés, perturbations ou contournements
  visant le service, ses utilisateurs ou son infrastructure ;
- l’utilisation de ressources de manière manifestement abusive au détriment
  des autres utilisateurs.

Une politique plus détaillée et une procédure de signalement doivent être
ajoutées avant une ouverture publique du service.

## 5. Messages, fichiers et chiffrement

L’utilisateur reste responsable des messages et fichiers qu’il choisit
d’envoyer, ainsi que des droits nécessaires sur ces contenus.

TLS protège la connexion au serveur lorsqu’il est correctement validé. Le
chiffrement de bout en bout OMEMO ne protège que les conversations pour
lesquelles il est effectivement activé et dont les appareils sont correctement
vérifiés. Les indicateurs affichés dans l’application doivent être consultés
avant l’envoi d’informations sensibles. Même avec OMEMO, certaines métadonnées
XMPP peuvent rester visibles pour l’infrastructure.

Les modalités de conservation côté serveur, de sauvegarde et de suppression
doivent être précisées dans la politique de confidentialité et dans le contrat
commercial.

## 6. Disponibilité et évolution

Les fonctions disponibles dépendent à la fois de l’application, de la
configuration du serveur XMPP, du réseau, de l’appareil et des capacités du
correspondant. L’historique synchronisé, les pièces jointes, les appels, les
notifications, les avatars ou le chiffrement peuvent donc nécessiter des
services XMPP particuliers.

Les engagements éventuels de disponibilité, de maintenance, de support, de
réversibilité et de restauration ne peuvent être déduits de ce document : ils
doivent figurer dans l’offre ou le contrat commercial applicable.

## 7. Suspension et fin d’accès

L’opérateur peut être amené à suspendre un compte pour protéger le service,
répondre à une obligation légale, traiter une compromission, faire cesser un
usage illicite ou appliquer le contrat commercial. Les motifs, délais de
préavis, voies de recours, modalités de récupération des données et effets de
la résiliation doivent être définis dans la version contractuelle finale.

La fin de l’abonnement au service MAER n’éteint pas les droits déjà accordés sur
le logiciel au titre de la GPLv3+.

## 8. Données personnelles

Le traitement des données personnelles est décrit dans la
[politique de confidentialité provisoire](PRIVACY.md). Cette politique doit
être complétée avec les rôles de chaque responsable de traitement, les
coordonnées encore manquantes, les durées de conservation, les destinataires et
les éventuels transferts avant la mise à disposition publique du service.

Pour une organisation cliente, les rôles RGPD ne peuvent pas être fixés par une
simple étiquette contractuelle : ils dépendent de qui décide des finalités et
des moyens essentiels. Le contrat devra donc préciser si l’organisation agit
comme responsable de traitement et MAER Engineering comme sous-traitant pour la
messagerie, ainsi que les traitements pour lesquels MAER Engineering agit pour
son propre compte, notamment la facturation, la sécurité et la gestion du
service.

## 9. Logiciel libre, absence d’exclusivité et provenance

Le texte complet de la GPLv3 figure dans [`LICENSE`](../../LICENSE). La
provenance de Conversations est conservée dans [`NOTICE.md`](../../NOTICE.md)
et [`UPSTREAM.md`](UPSTREAM.md). Les composants tiers sont recensés, sans
prétention d’exhaustivité, dans
[`THIRD_PARTY_NOTICES.md`](../../THIRD_PARTY_NOTICES.md).

Le service MAER peut recommander une version déterminée de l’application pour
des raisons de compatibilité ou de support. Cela ne transforme pas le logiciel
libre en logiciel sous licence propriétaire.

## 10. Qualification réglementaire à faire valider

Un service XMPP payant permettant des échanges directs entre un nombre fini de
personnes est susceptible de relever des services de communications
interpersonnelles non fondés sur la numérotation. Avant commercialisation,
l’opérateur doit faire qualifier le service au regard du code des postes et des
communications électroniques et prendre contact, si nécessaire, avec les
interlocuteurs compétents de l’ARCEP et du CCED pour les obligations applicables.

Cette qualification ne dépend pas de la licence GPL du client Android.

## 11. Informations obligatoires à compléter

Avant publication comme conditions opposables, un professionnel compétent doit
au minimum renseigner et valider :

- le numéro de téléphone professionnel de l’opérateur ;
- la confirmation que `contact@maer.fr` reçoit les demandes RGPD et l’existence
  ou non d’un délégué à la protection des données ;
- le public visé, le territoire, l’âge minimal et les règles applicables aux
  comptes d’organisation ;
- le catalogue des offres, les prix TTC/HT, la facturation et les moyens de
  paiement ;
- la durée, le renouvellement, la résiliation, la rétractation lorsqu’elle
  s’applique et la gestion des impayés ;
- les engagements de service, la maintenance, le support, la réversibilité et
  la conservation des données ;
- les garanties, responsabilités et assurances adaptées à l’offre réelle ;
- le droit applicable, le règlement des litiges, la médiation de la
  consommation si elle s’applique et la juridiction compétente ;
- la date d’entrée en vigueur, la procédure d’acceptation et l’historique des
  versions.
