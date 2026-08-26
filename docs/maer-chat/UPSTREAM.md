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

Le remote `upstream` est réservé à la lecture. Pour éviter qu’une commande
exécutée dans le mauvais terminal publie le fork sur le dépôt Conversations,
chaque clone de maintenance doit conserver cette configuration locale :

```shell
git remote set-url upstream https://codeberg.org/iNPUTmice/Conversations.git
git remote set-url --push upstream https://upstream-push-disabled.invalid/Conversations.git
```

Le suffixe `.invalid` est réservé et ne peut pas désigner un serveur Git réel.
`git fetch upstream` continue donc à lire Codeberg, tandis que
`git push upstream` échoue avant tout envoi. Cette protection appartient à la
configuration locale du clone et doit être réappliquée après un nouveau clone.
La publication du fork utilise exclusivement `origin`, après les contrôles et
l’autorisation de livraison.

1. Vérifier les URL de lecture et de push avec `git remote -v`.
2. Lire le changelog et les migrations de base avant toute fusion.
3. Fusionner sur une branche temporaire, sans écraser l’identité, la politique
   TLS, le stockage sécurisé ni le parcours de connexion Maer Chat.
4. Exécuter tests, lint, Debug et Release minifiée.
5. Tester une mise à jour réelle avec une copie de base contenant des comptes,
   messages, clés OMEMO et brouillons.
6. Mettre à jour ce document et `NOTICE.md` avec le nouveau commit.

La licence GPLv3 et les avis de copyright amont doivent rester présents dans
toute version dérivée distribuée.
