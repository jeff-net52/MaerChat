# Dossier de conformité de publication — Maer Chat 0.5.0

> État au 27 août 2026. Ce dossier organise les preuves techniques et les
> décisions restant à prendre. Il ne remplace pas une consultation juridique et
> n’autorise pas à présenter la version comme juridiquement validée tant que les
> blocages indiqués ci-dessous ne sont pas levés.

## 1. Périmètre confirmé

- Le client Android Maer Chat est un logiciel libre dérivé de Conversations,
  distribué sous GNU GPL version 3 ou ultérieure.
- Le client peut être utilisé gratuitement avec un serveur XMPP compatible.
- Seul l’accès aux serveurs de messagerie exploités par MAER Engineering exige
  un compte payant actif.
- Le service est exploité par Emilien REEVES, entrepreneur individuel, sous le
  nom commercial MAER Engineering, 16 ZI Saint Roch Box 1, 52340 Biesles,
  SIRET 523 560 175 00045, `contact@maer.fr`.
- Les données des serveurs MAER sont annoncées comme hébergées en France sur une
  infrastructure exploitée en interne.

## 2. État de la version

| Contrôle | État | Preuve ou action |
| --- | --- | --- |
| Version Android unique | Conforme dans les sources | `build.gradle`, version 0.5.0, code de base 5 |
| Notes propres au fork | Conforme dans les sources | `CHANGELOG.md`, notes Fastlane 504 et notes hors ligne |
| Historique amont séparé | Conforme dans les sources | section « Historique Conversations amont » du changelog |
| Licence principale | Conforme dans les sources | `LICENSE` et copie GPLv3 embarquée |
| Provenance Conversations | Conforme dans les sources | `NOTICE.md` et `UPSTREAM.md` |
| Source correspondant au binaire | **Bloquant avant APK publique** | publier l’APK avec le tag immuable `v0.5.0` pointant sur son source exact |
| Inventaire transitif des licences | **À finaliser avant APK publique** | conserver le rapport de dépendances résolues et les avis de chaque artefact distribué |
| Droits sur le logo Maer Chat | **À confirmer** | documenter l’auteur, la cession ou licence et le titulaire de la marque |
| Artefacts présents ou historiques | **Non publiables** | APK Debug et anciennes signatures de développement ; voir `dist/0.5.0/MANIFEST.md` |
| Clé et certificat de production | **Bloquant avant APK publique** | établir la clé pérenne hors dépôt et archiver son empreinte publique |
| Qualité Spotless/Lint | **Bloquant avant APK publique** | 14 fichiers Java à reformater avec revue et 8 erreurs Lint hors baseline à traiter |

La GPLv3, section 6, impose que la distribution de l’objet compilé donne un
accès équivalent au code source correspondant exact. Le dépôt d’une version
amont ou d’une branche qui évolue après la compilation n’est pas une preuve
suffisante. Le tag et le hash SHA-256 de chaque APK doivent être enregistrés
dans la fiche de livraison.

## 3. Données et contrats

| Contrôle | État | Élément manquant |
| --- | --- | --- |
| Identité de l’exploitant | Partiel | numéro de téléphone professionnel |
| Contact RGPD | À confirmer | confirmer `contact@maer.fr` et l’absence ou présence d’un DPO |
| Public de l’offre | Bloquant | professionnels uniquement ou également consommateurs |
| Rôles RGPD par organisation | Bloquant | responsable, sous-traitant ou responsables conjoints selon les décisions réelles |
| Contrat de sous-traitance | Bloquant si MAER est sous-traitant | clauses article 28, sécurité, assistance, sous-traitants ultérieurs et sort des données |
| Durées de conservation | Bloquant | messages, fichiers, journaux, sauvegardes, support, facturation et après-résiliation |
| Sous-traitants et transferts | À vérifier | réseau, DNS, certificats, sauvegardes et tout service hors infrastructure interne |
| Procédure de violation | À formaliser | registre, analyse de risque, notification CNIL sous 72 h lorsque requise |

L’information dans l’application est volontairement synthétique et accessible
avant connexion. Les conditions et la notice complètes doivent être remises par
l’organisation ou MAER Engineering et rester disponibles sur un support
durable lorsque le droit applicable l’exige.

## 4. Qualifications réglementaires à instruire

1. Le service payant présente les caractéristiques possibles d’un service de
   communications interpersonnelles non fondé sur la numérotation au sens du
   code européen et de l’article L.32 du code des postes et des communications
   électroniques. Les obligations opérationnelles doivent être confirmées avec
   un professionnel compétent et, le cas échéant, l’ARCEP/CCED.
2. Le règlement européen sur la cyberrésilience s’applique principalement à
   partir du 11 décembre 2027, mais son article 14 relatif aux notifications
   s’applique à partir du 11 septembre 2026. Il faut qualifier le rôle de MAER
   Engineering comme fabricant, importateur, distributeur ou éventuel
   gestionnaire de logiciel libre, puis préparer la gestion des vulnérabilités.
3. Si le service est fourni à des consommateurs, les règles relatives aux
   contrats à distance, à la médiation, à la rétractation, à l’accessibilité des
   services de communications électroniques et à l’affichage des prix doivent
   être ajoutées. Le statut B2B/B2C est donc une décision bloquante.

## 5. Sources officielles de contrôle

- [GNU GPL version 3](https://www.gnu.org/licenses/gpl-3.0.html), notamment la
  section 6 sur le code source correspondant ;
- [RGPD — règlement (UE) 2016/679](https://eur-lex.europa.eu/eli/reg/2016/679/oj),
  notamment les articles 12 à 14, 28, 30, 32 à 34 ;
- [Code européen des communications électroniques](https://eur-lex.europa.eu/legal-content/FR/TXT/?uri=CELEX:32018L1972) ;
- [Article L.32 du CPCE](https://www.legifrance.gouv.fr/codes/article_lc/LEGIARTI000049571421/) ;
- [Mentions d’identification imposées aux éditeurs de services en ligne par la LCEN](https://www.legifrance.gouv.fr/codes/id/LEGISCTA000006089778) ;
- [Obligations légales présentées par l’ARCEP](https://extranet.arcep.fr/communications-electroniques/obligations-legales) ;
- [Règlement (UE) 2024/2847 sur la cyberrésilience](https://eur-lex.europa.eu/eli/reg/2024/2847/oj) ;
- [Obligations de notification du CRA à partir du 11 septembre 2026](https://digital-strategy.ec.europa.eu/en/policies/cra-reporting) ;
- [Information et transparence selon la CNIL](https://www.cnil.fr/fr/conformite-rgpd-information-des-personnes-et-transparence) ;
- [Qualification responsable/sous-traitant selon la CNIL](https://www.cnil.fr/fr/rgpd-comment-bien-identifier-son-role) ;
- [Gestion des violations de données selon la CNIL](https://www.cnil.fr/fr/violations-de-donnees-personnelles-les-regles-suivre).

## 6. Procédure de livraison

1. exécuter `tools/check-release-compliance.sh` ;
2. résoudre tous les éléments marqués bloquants ;
3. compiler la variante exacte et conserver le graphe de dépendances résolu ;
4. signer avec la clé de production, jamais avec une clé de développement ;
5. créer le tag signé `v0.5.0` sur le commit compilé ;
6. publier ensemble APK, SHA-256, tag source, `LICENSE`, `NOTICE.md`,
   `THIRD_PARTY_NOTICES.md` et notes de version ;
7. archiver les résultats de tests, le SBOM ou inventaire de dépendances et la
   décision de validation juridique.
