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
adb install -r chemin/vers/fr.maer.chat-0.1.0-conversations-free-universal-debug.apk
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
compte **146 tests réussis** ; Robolectric emploie un runtime API 36 pour les
tests Android hors appareil. L’analyse Lint finale ne signale aucune nouvelle
anomalie : **655 constats amont** sont consignés dans la baseline et
`warningsAsErrors` rend toute nouvelle alerte hors baseline bloquante.

L’APK a été installée et démarrée sur un émulateur Android API 31. Aucun essai
sur appareil physique n’a été effectué. Les scénarios qui demandent une session
XMPP authentifiée restent soumis aux limites détaillées dans
[`TESTING.md`](TESTING.md).

## Artefacts livrés — version 0.1.0

Les deux APK universelles se trouvent dans `dist/` :

| Fichier | Taille | SHA-256 |
|---|---:|---|
| `Maer-Chat-0.1.0-debug.apk` | 78 584 142 octets | `60ab39e1dbccb832734fb40472791495e0b8c28dc40224e00dc218cc4434f111` |
| `Maer-Chat-0.1.0-release-dev-signed.apk` | 62 001 419 octets | `628bebd60d782d5e5c5e6d3c7a3d001d12143ea93108b1ac8216f8b182d8768b` |

L’identité vérifiée de l’application est `fr.maer.chat`, version code `100`,
version affichée `0.1.0+free`, avec `minSdk` 23 et `targetSdk` 37. La Release est
alignée et sa signature de développement est valide avec les schémas APK v1,
v2 et v3. Cette clé de développement permet l’installation et les essais, mais
elle doit être remplacée par la clé privée de production avant publication sur
un magasin d’applications.
