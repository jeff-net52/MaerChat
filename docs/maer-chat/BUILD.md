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
adb install -r chemin/vers/fr.maer.chat-0.5.4-conversations-free-arm64-v8a-debug.apk
```

Le paquet `fr.maer.chat` peut cohabiter avec Conversations. Une installation
`-r` ne fonctionne qu’avec une APK signée par la même clé que la version déjà
installée.

> **Attention aux tests instrumentés :** Android Gradle Plugin désinstalle
> l’application cible lors du nettoyage de `connected*AndroidTest`, ce qui
> supprime ses données privées. Ces tâches sont interdites sur un téléphone
> utilisateur. Le build les refuse par défaut sur un serial physique. Utilisez
> `tools/run-connected-tests.ps1 -Serial emulator-5554` avec un émulateur ;
> l’option `-AllowDisposablePhysicalDevice` est réservée à un appareil jetable
> dont l’effacement a été explicitement accepté.

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
compte **206 tests réussis**, sans échec, erreur ni test ignoré.
`compileConversationsFreeDebugAndroidTestJavaWithJavac`,
`lintConversationsFreeDebug`, `assembleConversationsFreeDebug` et
`spotlessJavaCheck` réussissent.
Lint ne signale aucun nouveau problème ; 626 constats historiques restent
explicitement filtrés par la baseline. La qualification d’une Release signée
reste un contrôle distinct avant publication publique.

Les sources des tests instrumentés compilent. Aucun test `connected*` n’a été
exécuté pour qualifier ce binaire final.

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
`targetSdk` 37. Les codes de version 0.5.4 sont `900` pour l’APK universelle
et `904` pour l’APK ARM64. Une clé privée de production pérenne, distincte de
toute clé Debug ou de développement historique, doit être établie avant toute
publication sur un magasin d’applications.
