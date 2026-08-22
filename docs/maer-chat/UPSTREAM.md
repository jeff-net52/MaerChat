# Base Conversations et stratégie de maintenance

Maer Chat conserve le moteur du dépôt officiel Conversations plutôt que de
réimplémenter XMPP. La base importée est le commit
`9fdc9ed5b2e2e4981cf58fce871c2ed47623c2bc`, version 2.20.1.

## Architecture conservée

- `XmppConnectionService` orchestre comptes, connexions, stockage,
  notifications et transferts.
- `XmppConnection` et les managers XMPP gèrent SRV, TLS, SASL, Disco, MAM,
  Carbons, PEP, MUC, HTTP Upload, Jingle et reconnexion.
- `DatabaseBackend` conserve comptes, conversations, messages et état local.
- `AxolotlService` porte l’implémentation OMEMO éprouvée.
- L’interface reste en Java/XML avec Data Binding et Material 3.

Une migration globale vers Compose, Room, Kotlin ou une nouvelle bibliothèque
XMPP n’est pas incluse dans cette version : elle multiplierait les risques de
régression sur OMEMO, l’historique, les médias et l’arrière-plan. La
modernisation se fait par ressources, composants et façades incrémentales.

## Mise à jour amont

1. Ajouter ou actualiser le remote officiel Codeberg.
2. Lire le changelog et les migrations de base avant toute fusion.
3. Fusionner sur une branche temporaire, sans écraser l’identité, la politique
   TLS, le stockage sécurisé ni le parcours de connexion Maer Chat.
4. Exécuter tests, lint, Debug et Release minifiée.
5. Tester une mise à jour réelle avec une copie de base contenant des comptes,
   messages, clés OMEMO et brouillons.
6. Mettre à jour ce document et `NOTICE.md` avec le nouveau commit.

La licence GPLv3 et les avis de copyright amont doivent rester présents dans
toute version dérivée distribuée.
