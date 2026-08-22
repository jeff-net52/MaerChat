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
- [`CGU.md`](CGU.md) : projet de conditions du service payant, à compléter
  avec les coordonnées et paramètres contractuels réels.
- [`PRIVACY.md`](PRIVACY.md) : projet d’information sur les données, à
  compléter avant ouverture commerciale.

Le code source public du client est publié sur
[GitHub](https://github.com/jeff-net52/MaerChat). Sa distribution serveur
associée, multi-organisation et fondée sur ejabberd Community Server, est
publiée dans
[MAER XMPP Server](https://github.com/jeff-net52/MAER-XMPP-Server).

## Aperçus

Les captures suivantes proviennent d’un build temporaire de documentation qui
n’est pas distribué. L’APK livrée conserve `FLAG_SECURE` sur l’écran de
connexion ; voir la preuve et le protocole dans [`TESTING.md`](TESTING.md).

- [Connexion](screenshots/login.png)
- [Erreur de connexion avec identifiants fictifs](screenshots/login-error.png)

Les informations de capacité XMPP affichées dans l’application sont calculées
pour le compte connecté. Elles prévalent sur toute liste statique, car un
serveur peut changer de configuration.
