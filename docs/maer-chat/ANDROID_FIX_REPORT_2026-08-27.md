# Rapport de correction Android — 27 août 2026

## Résultat livré

La variante Android `conversationsFreeDebug` est livrée en version
`0.5.2+free`, code `704` pour ARM64 (`700` pour l’APK universelle). Elle utilise
uniquement `xmpp.maer.fr` comme domaine MAER. La recherche statique ne trouve
aucune référence à l’ancien domaine interdit.

APK ARM64 :

`build/outputs/apk/conversationsFree/debug/fr.maer.chat-0.5.2-conversations-free-arm64-v8a-debug.apk`

SHA-256 :

`C32039485575822158D3DAF325E9081BF147EA337A03EA6EE121F50C7F2397C9`

Taille : `48 935 681` octets.

`apksigner 37.0.0` confirme les schémas v1 et v2, avec un seul certificat
`CN=Android Debug` dont l’empreinte SHA-256 est
`c4bab7cfc6a5f204215310c63ad5207e04d3aef67b154d4796cb02c03be81beb`.
Cet APK est un artefact de validation Debug, pas une Release publiable.

## Corrections fonctionnelles et de sécurité

- Notifications : visibilité privée sur l’écran verrouillé et version publique
  générique sans contact ni aperçu du message.
- Appareils liés : contrat `urn:maer:pairing:1`, expiration, nouvelle tentative,
  erreurs précises, `policy-violation` distinct pour le throttling, protection
  locale anti-rejeu et consommation après succès.
- Appels Windows/Android : détection de `MAER-CALL/1`, boutons audio, vidéo et
  partage d’écran, avec URL strictement HTTPS sur l’hôte exact `meet.jit.si`,
  sans user-info, sous-domaine ni port alternatif. L’expiration est revalidée
  au clic avant toute ouverture. Le contrat Windows à trois lignes impose
  `issued`, `expires`, `room`, une durée exacte de deux heures, le format UTC
  milliseconde, des séparateurs LF stricts, la dérive future maximale de cinq
  minutes et la correspondance exacte entre mode, libellé, salle, chemin et
  fragment Jitsi. Les liens bruts
  vers une salle `MAER-*` sont retirés des spans cliquables, y compris lorsqu’un
  message est expiré ou falsifié ; les liens ordinaires restent actifs.
- Partage d’écran Android : le wrapper WebRTC, et non l’Activity, possède le
  callback MediaProjection. Le remplacement de la caméra est transactionnel ;
  erreurs de capture ou de service de premier plan restaurent l’état précédent,
  et l’arrêt système restaure la caméra même après destruction de l’UI.
- Navigation : actions d’accueil et de gestion de compte correctement
  consommées ; liens profonds historiques retirés, seul le schéma XMPP
  canonique reste exposé.
- Accessibilité ciblée : états Appareils liés annoncés, descriptions de la
  visio et actions de partage d’écran.
- Tests instrumentés : lancement des vrais écrans et vues, sans gonfler un
  layout factice.
- Baseline Lint : régénérée par la tâche officielle ; 626 constats historiques
  restent filtrés et aucun nouveau constat n’est introduit.

## Validation automatisée

- `testConversationsFreeDebugUnitTest` : 199 tests, 0 échec, 0 erreur,
  0 ignoré.
- Cas ciblés : invitations d’appel, confidentialité des notifications,
  anti-rejeu, expiration du pairing, erreurs du contrat et manifeste des liens
  profonds, tous réussis.
- `compileConversationsFreeDebugAndroidTestJavaWithJavac` : réussi.
- `lintConversationsFreeDebug` : réussi, aucun nouveau constat, 626 constats
  historiques filtrés.
- `assembleConversationsFreeDebug` : réussi.
- Émulateur jetable API 31 : **2/2 tests instrumentés réussis** en 2 min 18,
  aucun échec ni test ignoré. Ils couvrent la navigation Contacts/Groupes/Appels
  et l’écran interactif Appareils liés.
- Après l’instrumentation, **14/14 tests JVM ciblés réussis** en 1 min 23 :
  contrat `MAER-CALL/1`, neutralisation des liens Jitsi bruts, transaction du
  capturer et cycle de vie du partage d’écran.
- L’émulateur `emulator-5580` a été identifié par `ro.kernel.qemu=1`, puis
  arrêté proprement. Chaque commande ADB utilisait explicitement sa série ;
  aucun test instrumenté n’a visé le téléphone utilisateur.
- Après reconnexion explicite du Samsung, l’APK ARM64 finale a été installée
  uniquement avec `adb -s R5GL42017QV install -r`. Le résultat était `Success`,
  la version reste `0.5.2+free` code `704`, `firstInstallTime` est resté à
  `2026-08-27 10:09:43` et seul `lastUpdateTime` est passé à
  `2026-08-27 11:54:05` : la mise à jour sur place n’a donc pas désinstallé
  l’application ni effacé ses données. Aucun test connecté n’a ensuite été
  lancé sur ce téléphone.
- `git diff --check` : réussi.
- Recherche statique du domaine interdit : réussie, zéro occurrence.

Résultat XML historique, antérieur aux sources finales :

`build/outputs/androidTest-results/connected/debug/flavors/conversationsFree/TEST-SM-G556B - 16-_-conversationsFree.xml`

Ce passage comptait 2 tests réussis, mais ne qualifie pas le binaire final et
son nettoyage a provoqué l’incident documenté ci-dessous.

## Incident du runner instrumenté sur le téléphone utilisateur

Le téléphone contenait initialement `0.5.1+free` code `604`. L’installation
`adb install -r` de `0.5.2+free` code `704` avait réussi et conservé le
`firstInstallTime` du 24 août 2026 ainsi que les données.

Après l’exécution demandée de
`connectedConversationsFreeDebugAndroidTest`, Android Gradle Plugin a
automatiquement désinstallé le paquet cible pendant son nettoyage. Aucun
`adb uninstall` manuel n’a été lancé, mais `pm list packages` ne contenait plus
`fr.maer.chat` et `/data/user/0/fr.maer.chat` n’existait plus : les données
locales du compte ont donc été perdues.

L’APK ARM64 0.5.2/704 a ensuite été réinstallée. Le nouvel
`firstInstallTime` est le 27 août 2026 à 10:09:43. L’activité visible est
`WelcomeActivity`, ce qui confirme qu’Android n’a pas restauré automatiquement
l’ancien compte. Caméra, microphone, notifications et Bluetooth restent non
accordés. Le buffer crash Android est vide.

Prévention ajoutée : le graphe Gradle refuse désormais les tâches
`connected*AndroidTest` sans `ANDROID_SERIAL`, et refuse un serial physique
sauf opt-in `-PallowDisposableConnectedTests=true`. Le script
`tools/run-connected-tests.ps1` accepte automatiquement un émulateur et exige
`-AllowDisposablePhysicalDevice` pour un appareil physique jetable. Il détecte
et propage aussi un JDK Android Studio ainsi que le SDK Android vers Gradle si
les variables d’environnement n’étaient pas déjà définies. Ces tests ne doivent
plus être exécutés sur un téléphone utilisateur.

## Limites restantes

- Le partage d’écran n’a pas été déclenché physiquement : le bouton n’est
  disponible que pendant une visio connectée et aucun appel vers un tiers n’a
  été initié. Les API, le manifeste, le build et le chemin RTP sont validés,
  mais un essai bout en bout exige deux comptes/appareils de test.
- Le pairing réseau réel exige une session serveur authentifiée ; l’écran et
  le contrat sont testés, pas une approbation réelle sur un compte utilisateur.
- Les tests sur émulateur ne remplacent pas un appel XMPP/WebRTC réel entre
  deux terminaux ni la validation du serveur déployé.
- `FLAG_SECURE` a empêché UIAutomator et les captures d’extraire le contenu de
  l’application. Cette protection a été conservée ; aucune capture contenant
  des conversations personnelles n’a été produite.
- 626 constats Lint historiques restent dans la baseline. Aucun nouveau
  constat n’est ajouté, mais cette dette n’est pas présentée comme corrigée.
- Aucune clé Release fictive n’a été créée ; la signature de production et
  la qualification d’une Release restent à effectuer séparément.
