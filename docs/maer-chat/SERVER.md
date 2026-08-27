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

## Observations publiques du 26 août 2026

Ces vérifications ne nécessitent ni compte ni mot de passe.

| Élément | Observation |
|---|---|
| Domaine XMPP | `xmpp.maer.fr` |
| IPv4 | `82.67.146.209` |
| IPv6 | aucune adresse AAAA annoncée |
| SRV client STARTTLS | aucun enregistrement `_xmpp-client._tcp` |
| SRV client TLS direct | aucun enregistrement `_xmpps-client._tcp` |
| Ports contrôlés | 5222 et 5269 accessibles ; 5223 inaccessible depuis le poste de test |
| TLS XMPP | STARTTLS sur 5222 négocié en TLS 1.3 |
| Certificat XMPP | nom `xmpp.maer.fr` validé par OpenSSL sans contournement |
| HTTPS | ancien certificat ne couvrant pas `xmpp.maer.fr`, refusé avec `SEC_E_WRONG_PRINCIPAL` |
| SASL et authentification | non vérifiés faute de compte de test dédié |

Le port 5222 et son STARTTLS sont désormais joignables, ce qui lève le blocage
de transport observé le 24 août. Cela ne valide toutefois ni le virtual host
après authentification, ni le mot de passe, ni les capacités XMPP du compte.
L’absence de SRV oblige en outre le client à utiliser son repli vers le domaine
et le port standard. Publier un SRV client explicite après validation complète,
par exemple :

```dns
_xmpp-client._tcp.xmpp.maer.fr. 3600 IN SRV 0 5 5222 xmpp.maer.fr.
```

Ne publiez cette valeur qu’après vérification qu’elle correspond réellement au
service exploité. Le certificat STARTTLS couvre actuellement `xmpp.maer.fr`,
mais le vhost HTTPS utilisé par le client Windows présente encore un mauvais
nom de certificat et doit être corrigé séparément.

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
