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
adb install -r chemin/vers/fr.maer.chat-0.2.0-conversations-free-arm64-v8a-debug.apk
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
compte **154 tests réussis**, sans échec ni test ignoré. L’analyse Lint finale
ne signale aucune nouvelle anomalie : **650 constats amont** sont filtrés par la
baseline et `warningsAsErrors` rend toute nouvelle alerte hors baseline
bloquante.

L’APK a été installée et démarrée sur un émulateur Android API 31. Aucun essai
sur appareil physique n’a été effectué. Les scénarios qui demandent une session
XMPP authentifiée restent soumis aux limites détaillées dans
[`TESTING.md`](TESTING.md).

## Artefacts livrés — version 0.2.0

Les APK ARM64 et universelle se trouvent dans `dist/` :

| Fichier | Taille | SHA-256 |
|---|---:|---|
| `Maer-Chat-0.2.0-arm64-release-dev-signed.apk` | 32 178 511 octets | `1df7c4e60808ea471e42f2a6f7ba467cd4e57804ce04db3e98313112d558b43b` |
| `Maer-Chat-0.2.0-universal-release-dev-signed.apk` | 62 534 460 octets | `06eb5ddbc9745eac7a6ff37a2dc9011ec5d9a322de59a09feade0b5e8d894e35` |

L’identité vérifiée de l’application est `fr.maer.chat`, version affichée
`0.2.0+free`, avec `minSdk` 23 et `targetSdk` 37. Le code de version vaut `204`
pour l’APK ARM64 et `200` pour l’universelle. Les deux Releases sont alignées et
leur signature de développement est valide avec les schémas APK v1, v2 et v3.
L’empreinte SHA-256 du certificat est
`88b31196c797cae22b75b838c078a95a886fcf94bace75c8f958d52d106f7f4d`,
identique à la version 0.1.0 de développement afin de permettre une mise à jour
sans perte de données. Cette clé doit être remplacée par une clé privée de
production pérenne avant toute publication sur un magasin d’applications.
