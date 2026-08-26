# Maer Chat 0.5.0 — artefacts retirés de la publication

> **AUCUN ARTEFACT DE CE DOSSIER N’EST PUBLIABLE.**

Ce dossier ne contient actuellement aucun APK. Son ancien manifeste référençait
une APK Debug ARM64 qui n’est plus présente et dont l’empreinte ne décrit pas
les sorties du build courant. Cette ancienne empreinte est donc retirée pour
éviter qu’elle soit confondue avec une preuve de livraison.

Les fichiers produits sous
`build/outputs/apk/conversationsFree/debug/` sont signés avec la clé Debug
locale, varient selon l’environnement de compilation et servent uniquement aux
tests. Ils ne doivent être ni publiés, ni renommés en Release, ni versés sur un
magasin d’applications.

Une future livraison 0.5.0 exige un manifeste neuf, généré depuis le commit ou
tag immuable effectivement compilé, et doit identifier au minimum :

- la variante `conversationsFreeRelease` exacte ;
- le commit et le tag source signés ;
- le nom, la taille et le SHA-256 de chaque APK publiée ;
- l’empreinte du certificat de production et les schémas de signature validés ;
- le rapport de dépendances/licences et les résultats de tests associés.

Les blocages juridiques et techniques restants sont suivis dans
[`../../docs/maer-chat/RELEASE_COMPLIANCE.md`](../../docs/maer-chat/RELEASE_COMPLIANCE.md).
