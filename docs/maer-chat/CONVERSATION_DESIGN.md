# MAER Familiar Chat

## Intention

`MAER Familiar Chat` est le thème de conversation par défaut de Maer Chat. Il
reprend une structure immédiatement familière aux utilisateurs de messageries
modernes — en-tête, fil chronologique, bulles et compositeur ancré en bas — sans
reproduire à l’identique une application tierce. Le logo, les illustrations, les
icônes de marque, les couleurs et les textes restent des créations ou des actifs
originaux MAER.

## Palette

| Rôle | Clair | Sombre |
|---|---|---|
| Fond de conversation | `#F1F6FA` | `#07131F` |
| Surface et en-tête | `#FFFFFF` | `#0B1F33` |
| Bleu principal MAER | `#0057B8` | `#A7C8FF` |
| Accent cyan MAER | `#00BFEF` | `#5CDAFF` |
| Texte principal | `#0B1F33` | `#F5F9FC` |
| Texte secondaire | `#63717E` | `#B3C0CC` |
| Bulle reçue | `#FFFFFF` | `#13283B` |
| Bulle envoyée | `#DDF4FF` | `#073B5E` |
| Accusé de lecture | `#0089E6` | `#4DD7FF` |

Les couleurs fonctionnelles (erreur, avertissement, succès) restent distinctes
de la marque. Chaque combinaison texte/fond doit être contrôlée en contraste
avant livraison, y compris lorsque l’utilisateur personnalise le thème.

## Conversation

- Les messages reçus sont alignés à gauche et les messages envoyés à droite,
  avec une largeur maximale de 82 %, des angles souples de 10 dp et un coin
  directionnel de 3 dp, inversé automatiquement en écriture droite-à-gauche.
- Dans un groupe, l’avatar et le nom précèdent le premier message d’une série ;
  dans un échange direct, ils restent disponibles dans l’en-tête sans alourdir
  chaque bulle.
- L’heure et l’état d’envoi occupent une zone stable en bas de la bulle. Une
  horloge indique l’attente, une coche l’envoi, deux coches la livraison et deux
  coches accentuées la lecture. Une erreur affiche aussi un libellé ou une icône,
  afin que l’information ne dépende jamais de la couleur seule.
- Les réponses citées utilisent un filet bleu MAER et une surface légèrement
  contrastée. Les réactions, pièces jointes, messages vocaux et aperçus de liens
  conservent la même grille d’espacement.

## Avatars

Les avatars XMPP disponibles sont affichés dans l’en-tête, les listes et les
conversations de groupe. En leur absence, un monogramme lisible sur un dégradé
MAER déterministe sert de remplacement ; il ne doit pas changer au redémarrage.
Les images sont recadrées en cercle sans déformation et une description
accessible reprend le nom du contact ou du groupe.

## Compositeur

Le compositeur est une surface arrondie et compacte : action de pièce jointe à
gauche, champ multiligne au centre, puis action vocale ou envoi à droite selon le
contenu. Il suit le clavier et les marges système, préserve le brouillon et garde
les actions principales à portée du pouce. Toute action destructive ou payante
demande une confirmation explicite.

## Personnalisation

L’utilisateur peut choisir le mode clair, sombre ou système, puis ajuster la
couleur principale, les bulles, la police, la taille du texte et le fond de
conversation. Un aperçu en direct, une option de réinitialisation et des
préréglages MAER évitent les configurations illisibles. Les réglages restent
locaux au profil sauf consentement explicite à leur synchronisation.

## Accessibilité

- cibles tactiles d’au moins 48 dp et navigation complète au clavier ou par
  technologies d’assistance ;
- libellés de lecteur d’écran pour les avatars, reçus, médias et actions du
  compositeur ;
- prise en charge des grandes polices, du texte jusqu’à 200 %, du mode paysage
  et du sens droite-à-gauche sans masquer le contenu ;
- contraste WCAG AA visé pour le texte et les contrôles, états jamais exprimés
  par la couleur seule, animations réduites lorsque le système le demande ;
- ordre de lecture cohérent : en-tête, messages chronologiques, indicateurs puis
  compositeur.
