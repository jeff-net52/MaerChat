# Documentation Maer Chat

Cette documentation décrit le fork Maer Chat sans remplacer la documentation
utilisateur héritée de Conversations.

- [`BUILD.md`](BUILD.md) : compiler, signer et installer l’application.
- [`SERVER.md`](SERVER.md) : état vérifié de `contacts.chaumont.me`, XEP et
  prérequis serveur.
- [`SECURITY.md`](SECURITY.md) : modèle de sécurité, secrets, TLS et limites.
- [`TESTING.md`](TESTING.md) : matrice de validation et résultats reproductibles.
- [`UPSTREAM.md`](UPSTREAM.md) : architecture conservée et stratégie de mise à
  jour de Conversations.

## Aperçus

Les captures suivantes proviennent d’un build temporaire de documentation qui
n’est pas distribué. L’APK livrée conserve `FLAG_SECURE` sur l’écran de
connexion ; voir la preuve et le protocole dans [`TESTING.md`](TESTING.md).

- [Connexion](screenshots/login.png)
- [Erreur de connexion avec identifiants fictifs](screenshots/login-error.png)

Les informations de capacité XMPP affichées dans l’application sont calculées
pour le compte connecté. Elles prévalent sur toute liste statique, car un
serveur peut changer de configuration.
