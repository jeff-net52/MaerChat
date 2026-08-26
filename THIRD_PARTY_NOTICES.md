# Maer Chat — composants tiers et obligations de distribution

> **Inventaire de travail, volontairement non exhaustif.** Il rapproche les
> dépendances déclarées dans `build.gradle`, le graphe Gradle résolu pour
> `conversationsFreeReleaseRuntimeClasspath` le 27 août 2026 et les
> métadonnées POM disponibles pour les versions résolues. Il ne constitue ni un
> SBOM du binaire final, ni une preuve que tous les avis transitifs ou natifs ont
> été collectés. Les fichiers de licence et avis contenus dans chaque artefact
> effectivement publié font foi.

Cet état de travail concerne Maer Chat 0.5.0.

## Base amont et licence du programme

Maer Chat est dérivé de
[Conversations](https://codeberg.org/iNPUTmice/Conversations), créé et maintenu
en amont par Daniel Gultsch et ses contributeurs.

- Base importée : Conversations 2.20.1, commit
  `9fdc9ed5b2e2e4981cf58fce871c2ed47623c2bc` du 13 août 2026.
- Copyright amont : © 2014–2026 Daniel Gultsch et contributeurs.
- Licence du programme dérivé : GNU General Public License version 3 ou
  ultérieure ; voir [`LICENSE`](LICENSE).
- Stratégie de suivi amont : [`docs/maer-chat/UPSTREAM.md`](docs/maer-chat/UPSTREAM.md).

Le fait que l’accès au service de messagerie MAER soit payant ne modifie pas la
licence GPLv3+ du logiciel.

## Dépendances identifiées dans la variante candidate

Le build Android `conversations/free` déclare directement une partie des
composants suivants ; les éléments signalés comme transitifs proviennent du
graphe Gradle résolu. Les regroupements de licences reprennent les métadonnées
POM et les avis présents dans le dépôt : ils facilitent l’audit mais ne
remplacent ni les textes de licence, ni les fichiers `NOTICE`, ni l’inspection
du contenu exact des AAR/JAR intégrés.

### Apache License 2.0

- AndroidX / Android Jetpack, y compris AppCompat, WorkManager, Preference et
  Emoji2, ainsi que leurs modules AndroidX transitifs résolus ;
- Material Components for Android 1.14.0 ;
- OpenKeychain OpenPGP API 5.7.1 ;
- Immutables 2.12.2 ;
- Guava 33.6.0-android ;
- ZXing Core 3.5.4 ;
- Floating Action Button Speed Dial 3.3.0 ;
- OkHttp 5.3.2 ;
- Okio 3.16.4 ;
- Retrofit 3.0.0 et son convertisseur Gson ;
- Gson 2.13.1 ;
- Android Image Cropper 4.7.0 ;
- Transcoder 0.11.2 ;
- ShortcutBadger 1.1.22 ;
- Conscrypt Android 2.5.3 ;
- JXMPP 1.1.0 ;
- MiniDNS 1.1.1 : les POM des artefacts résolus déclarent Apache License 2.0 ;
- osmdroid Android 6.1.20 ;
- les outils de désucrage Android `desugar_jdk_libs` 2.1.5 ;
- Kotlin standard library 2.2.21 et Kotlin Coroutines 1.9.0 ;
- Jemoji 1.7.6 ;
- Egloo 0.6.1 ;
- les annotations JSpecify, Error Prone et J2ObjC résolues par le build.

### Licences permissives ou multiples

- Bouncy Castle 1.84 : licence permissive de type MIT fournie par le projet ;
- HSLuv Java 1.0 : licence MIT ;
- WebRTC Android 129.0.0 : le paquet de distribution d’iNPUTmice déclare Apache
  2.0 et MIT dans son POM ; le moteur WebRTC conserve son avis BSD 3 clauses et
  des notices tierces propres qui doivent être extraites de l’artefact livré ;
- Protocol Buffers Java 2.5.0 : licence BSD 3 clauses ;
- les données Unicode/emoji incorporées par Jemoji : conserver les attributions
  et conditions de l’artefact effectivement résolu.

### Copyleft compatible avec le programme

- `signal-protocol-java` 2.6.2 (libsignal-protocol Java) : GNU General Public
  License version 3 ;
- `curve25519-java` 0.4.1 : GNU General Public License version 3 ;
- GNU Libidn Java 1.15 : GNU Lesser General Public License version 2.1 ou
  ultérieure. Le texte LGPL 2.1 est embarqué dans l’écran des licences.

## Textes embarqués dans le client

L’écran « Conditions, confidentialité et licences » de la variante candidate
donne accès hors ligne :

- à la GNU GPLv3 complète ;
- à la GNU LGPLv2.1 complète ;
- à la licence Apache 2.0 complète ;
- aux avis MIT de Bouncy Castle et HSLuv ;
- aux avis BSD de WebRTC et Protocol Buffers ;
- à l’inventaire synthétique des composants et aux notes de version.

Ces textes ne dispensent pas de conserver les éventuels fichiers `NOTICE`, avis
additionnels et licences des composants natifs et données embarquées dans
l’archive exacte publiée. En particulier, les notices tierces complètes du
binaire WebRTC, les données Unicode/emoji et tout code natif doivent encore
être inventoriés automatiquement avant une publication binaire.

Les variantes Quicksy et Play Store sont exclues du produit Maer Chat actuel.
Leurs dépendances conditionnelles ne doivent être déclarées comme distribuées
que si ces variantes sont réactivées ultérieurement.

## Cartographie et données externes

L’application peut afficher des cartes au moyen d’osmdroid. Lorsque des données
ou tuiles OpenStreetMap sont utilisées, l’attribution « © les contributeurs
OpenStreetMap » et les conditions de l’Open Database License (ODbL) doivent être
respectées, ainsi que la politique du fournisseur de tuiles réellement
configuré.

## Ressources graphiques héritées

Les œuvres conservées dans `art/` sont couvertes par leur licence propre,
notamment Creative Commons Attribution-ShareAlike 4.0 International ; voir
[`art/LICENSE`](art/LICENSE). Toute ressource redistribuée depuis ce répertoire
doit garder l’attribution requise et, en cas d’adaptation partagée, respecter la
clause de partage dans les mêmes conditions.

Le statut des noms et logos Maer Chat est documenté séparément dans
[`TRADEMARKS.md`](TRADEMARKS.md). Une licence de logiciel ne vaut pas
automatiquement autorisation d’utiliser une marque.

## Fichiers comportant leurs propres avis

Certains fichiers sources conservent des avis BSD, Apache-2.0 ou d’autres avis
permissifs individuels. Ils doivent rester attachés aux fichiers concernés. La
notice générale [`NOTICE.md`](NOTICE.md) ne les remplace pas.

## Liste de contrôle avant publication

Pour chaque APK ou autre binaire distribué :

1. résoudre le graphe exact des dépendances de la variante publiée ;
2. générer un rapport de licences incluant les dépendances transitives et les
   composants natifs ;
3. comparer ce rapport à ce document et corriger les écarts ;
4. reproduire les textes de licence, copyrights et notices exigés par chaque
   composant ;
5. conserver les avis présents dans les fichiers sources ;
6. fournir le code source correspondant et les scripts nécessaires selon les
   modalités permises par la GPLv3+ ;
7. conserver `LICENSE`, `NOTICE.md`, le présent fichier et les attributions des
   ressources graphiques dans la distribution ;
8. vérifier séparément les droits d’utilisation des polices, icônes, images,
   sons, cartes, tuiles et marques ajoutés au fork.

Tant que les étapes 1 à 4 ne sont pas archivées pour l’APK Release exacte, cet
inventaire ne doit pas être présenté comme exhaustif ni comme une validation de
publication.

Ce document est une aide de conformité technique et ne constitue pas un avis
juridique.
