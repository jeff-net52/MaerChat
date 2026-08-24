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
compte **163 tests réussis**, sans échec ni test ignoré. L’analyse Lint finale
ne signale aucune nouvelle anomalie : **647 constats amont** sont filtrés par la
baseline et `warningsAsErrors` rend toute nouvelle alerte hors baseline
bloquante.

Le test instrumenté de navigation passe également sur l’émulateur Android
API 31 : **1 test réussi**, sans échec ni erreur.

L’APK Debug ARM64 0.5.0 a été installée sur un Samsung XCover 7 SM-G556B sous
Android 16/API 36. La précédente Release de développement 0.4.0 employait une
autre clé : Android a correctement refusé la mise à jour, puis la 0.4.0 a été
désinstallée avec autorisation explicite avant l’installation neuve de la 0.5.0.
Android a confirmé `0.5.0+free`, code 504. L’activité d’accueil a démarré et
aucune exception `AndroidRuntime` n’a été produite. Les scénarios nécessitant
une session XMPP authentifiée restent soumis aux limites détaillées dans
[`TESTING.md`](TESTING.md).

## Artefacts historiques locaux — version 0.4.0

Les APK ARM64 et universelle de validation se trouvent dans le répertoire local
ignoré `dist/`. Elles ne sont pas publiées avec le dépôt source :

| Fichier | Taille | SHA-256 |
|---|---:|---|
| `Maer-Chat-0.4.0-arm64-release-dev-signed.apk` | 32 211 653 octets | `31a61d5fec62a0c25b034297db7791d326b87d34cddd91f2e7cb2b37f3ba342a` |
| `Maer-Chat-0.4.0-universal-release-dev-signed.apk` | 62 567 602 octets | `39a6ceaca8235677bcedc10573bb499be43a1d27f3733aa88cfac32831fc860d` |

L’identité vérifiée de l’application est `fr.maer.chat`, version affichée
`0.4.0+free`, avec `minSdk` 23 et `targetSdk` 37. Le code de version vaut `404`
pour l’APK ARM64 et `400` pour l’universelle. Les deux Releases sont alignées et
leur signature de développement est valide avec les schémas APK v1, v2 et v3.
L’empreinte SHA-256 du certificat est
`88b31196c797cae22b75b838c078a95a886fcf94bace75c8f958d52d106f7f4d`,
identique à la version 0.1.0 de développement afin de permettre une mise à jour
sans perte de données. Cette clé doit être remplacée par une clé privée de
production pérenne avant toute publication sur un magasin d’applications.
