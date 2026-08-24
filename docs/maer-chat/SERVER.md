# Domaine de messagerie `xmpp.maer.fr`

Le dépôt public
[MAER XMPP Server](https://github.com/jeff-net52/MAER-XMPP-Server) fournit une
distribution documentée basée sur ejabberd Community Server 26.07. Son modèle
multi-organisation associe chaque organisation à un virtual host XMPP et
documente deux modes : mutualisé avec séparation logique, ou dédié avec
instance, base, fichiers, certificats et sauvegardes séparés.

Ce dépôt ne prouve pas que la configuration publique décrite ci-dessous a déjà
été déployée. La production ne doit être modifiée qu’après identification de la
version installée, sauvegarde complète et validation sur une préproduction.

## Observations publiques du 24 août 2026

Ces vérifications ne nécessitent ni compte ni mot de passe.

| Élément | Observation |
|---|---|
| Domaine XMPP | `xmpp.maer.fr` |
| IPv4 | `82.67.146.209` |
| IPv6 | aucune adresse AAAA annoncée |
| SRV client STARTTLS | aucun enregistrement `_xmpp-client._tcp` |
| SRV client TLS direct | aucun enregistrement `_xmpps-client._tcp` |
| Ports contrôlés | 5222, 5223 et 5269 inaccessibles à 19 h 50 CEST depuis le poste de test |
| HTTPS | port 443 accessible |
| TLS XMPP et certificat | non vérifiables tant qu’un port XMPP ne répond pas |
| SASL et création de compte | non vérifiables avant mise en service XMPP |

Le port 5222 a brièvement répondu pendant le déploiement, mais le virtual host
retournait alors `host-unknown`; les ports ont ensuite été fermés pendant le
redémarrage. Le domaine est désormais la cible configurée dans Maer Chat, mais
l’observation publique ne permet donc pas encore de considérer le service de
messagerie comme opérationnel. Publier le SRV client après ouverture et
validation du port 5222,
par exemple :

```dns
_xmpp-client._tcp.xmpp.maer.fr. 3600 IN SRV 0 5 5222 xmpp.maer.fr.
```

Ne publiez cette valeur qu’après vérification qu’elle correspond réellement au
service exploité. Le certificat présenté par ejabberd devra couvrir
`xmpp.maer.fr` et la connexion STARTTLS devra être testée avant distribution du
client configuré pour la production.

## Capacités après authentification

MAM, PEP/OMEMO, HTTP Upload, Carbons, Stream Management, blocage, CSI, Push,
Bind 2, SASL 2 et découverte des services externes ne peuvent pas être confirmés
honnêtement sans compte de test. Maer Chat interroge les capacités Disco du
compte connecté et les affiche dans « Compte > Informations serveur ».

Les fonctions restent conditionnelles : l’absence d’une capacité masque ou
désactive l’action concernée. Les principales correspondances sont :

| Fonction | Extension ou service attendu |
|---|---|
| Historique synchronisé | XEP-0313 MAM |
| Multi-appareils cohérents | XEP-0280 Carbons, XEP-0198 SM |
| OMEMO | PEP/XEP-0163 et nœuds OMEMO compatibles Conversations |
| Pièces jointes asynchrones | XEP-0363 HTTP File Upload en HTTPS |
| Blocage | XEP-0191 |
| Notifications push | XEP-0357 + XEP-0198 + relais propre à Maer Chat |
| Appels fiables hors LAN | XEP-0215 et services STUN/TURN valides |

## Appels audio/vidéo

Le moteur Jingle/WebRTC reste présent, mais un bouton d’appel ne doit être
proposé que si le correspondant annonce les capacités nécessaires. Pour une
connectivité réelle à travers NAT et réseaux mobiles, configurez au minimum un
service STUN et, de préférence, TURN authentifié annoncé par XEP-0215. Sans
compte de test et sans résultat de découverte externe, les appels ne sont pas
considérés validés pour ce serveur.

## Tests authentifiés à prévoir

Utiliser deux comptes de test dédiés, saisis sur les appareils ou fournis par un
coffre CI, pour vérifier MAM, OMEMO multi-appareil, upload HTTPS, MUC, marqueurs,
réactions, correction, reprise XEP-0198, arrière-plan/Doze et STUN/TURN. Aucun
identifiant ne doit être ajouté au dépôt.
