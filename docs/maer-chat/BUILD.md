# Compiler et installer Maer Chat

## Prérequis

- JDK 21 ;
- Android SDK Platform 37 ;
- Android Build Tools 37.0.0 ;
- un accès à Maven Central, Google Maven et JitPack au premier build ;
- Android 6.0 (API 23) ou plus récent pour l’appareil cible.

Le wrapper fourni télécharge Gradle 9.4.1. Android Studio peut installer le JDK
et le SDK requis. Ne mélangez pas un SDK Windows avec un Gradle Linux sous WSL :
les outils `aapt`, `adb` et `apksigner` doivent tous appartenir au même système.

## Debug installable

Depuis la racine du dépôt :

```shell
./gradlew clean testConversationsFreeDebugUnitTest \
  lintConversationsFreeDebug assembleConversationsFreeDebug
```

L’APK universel se trouve dans
`build/outputs/apk/conversationsFree/debug/`. Pour l’installer :

```shell
adb install -r chemin/vers/fr.maer.chat-0.5.0-conversations-free-arm64-v8a-debug.apk
```

Le paquet `fr.maer.chat` peut cohabiter avec Conversations. Une installation
`-r` ne fonctionne qu’avec une APK signée par la même clé que la version déjà
installée.

## Release signable ou signée

Sans `signing.properties`, la commande suivante produit des APK Release
minifiées non signées, prêtes à être signées :

```shell
./gradlew assembleConversationsFreeRelease
```

Pour une signature de développement locale, créez une clé hors du dépôt puis un
fichier `signing.properties` également hors versionnement :

```properties
keystore=/chemin/absolu/maer-chat-development.jks
keystore.password=mot-de-passe-local
keystore.alias=maer-chat-development
```

Le mot de passe et la clé ne doivent jamais être ajoutés à Git, inclus dans une
archive source ou transmis dans les journaux CI. Une clé de production doit être
gérée séparément avec sauvegarde et contrôle d’accès.

## Variante distribuée

La variante recommandée est `conversationsFree`. Elle n’embarque aucune
configuration FCM de Conversations. Pour un réveil push XEP-0357, Maer Chat doit
posséder sa propre configuration applicative et un relais push compatible ; voir
[`SERVER.md`](SERVER.md).

## État du build validé

Le build courant utilise `compileSdk` et `targetSdk` 37. Sa suite unitaire
compte **178 tests réussis**, sans échec. L’assemblage
`conversationsFreeRelease` minifié réussit et produit cinq APK dont le nom se
termine explicitement par `release-unsigned`.

Le portail de publication reste toutefois rouge : `spotlessCheck` signale des
écarts préexistants dans 14 fichiers Java, et `lintConversationsFreeDebug`
signale 8 erreurs hors baseline (une mise à niveau disponible d’AGP et sept
ressources MAER devenues inutilisées). Les 645 constats amont restants sont
filtrés par la baseline. Ces contrôles doivent passer sans régénération aveugle
de la baseline avant toute livraison.

Le test instrumenté de navigation passe également sur l’émulateur Android
API 31 : **1 test réussi**, sans échec ni erreur.

Une APK Debug ARM64 0.5.0 antérieure a été installée sur un Samsung XCover 7
SM-G556B sous Android 16/API 36. Cette validation historique démontre la
procédure d’installation neuve ; elle ne qualifie ni les APK Debug du build
courant, ni une future Release de production. Aucun appareil n’était connecté
pendant le contrôle du présent état des sources. Les scénarios nécessitant une
session XMPP authentifiée restent soumis aux limites détaillées dans
[`TESTING.md`](TESTING.md).

## Artefacts Debug et historiques

Les APK 0.4.0 signées avec une clé de développement et toutes les APK 0.5.0
Debug sont des artefacts de test **non publiables**. Le dépôt ne conserve plus
leurs anciennes empreintes comme s’il s’agissait d’une livraison. Le fichier
[`../../dist/0.5.0/MANIFEST.md`](../../dist/0.5.0/MANIFEST.md) décrit les preuves
à produire pour une future Release.

L’identité de l’application candidate reste `fr.maer.chat`, avec `minSdk` 23 et
`targetSdk` 37. Les codes de version attendus sont `500` pour l’APK universelle
et `504` pour l’APK ARM64. Une clé privée de production pérenne, distincte de
toute clé Debug ou de développement historique, doit être établie avant toute
publication sur un magasin d’applications.
