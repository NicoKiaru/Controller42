# Controller 42

Il s’agit d’un logiciel permettant de contrôler différents appareils (pousse-seringue, shutter, camera, tracker de billes, actuateurs linéaires, micromanipulateurs) de manière coordonnée et unifiée au sein d’un même programme.

L'acquisition se fait en Matlab.
La visualization des données s'effectue sous ImageJ (en cours...).

[![IMAGE ALT TEXT HERE](https://img.youtube.com/vi/6Fa23DvvOuM/0.jpg)](https://www.youtube.com/watch?v=6Fa23DvvOuM)

L’avantage de ce logiciel réside dans son aspect modulaire et dans la standardisation de la gestion des « devices ». Lors du démarrage d’une acquisition, tous les devices générent au moins un fichier texte de type device_name_log.txt, qui contient toutes les informations d’état ou de changement d’état du device, en précisant l’heure de chaque évènement. Pour les devices nécessitant plusieurs types de fichiers, par exemple pour une l’acquisition vidéo par une caméra, un fichier vidéo est créé en parallele.

Ce projet contient l’équivalent de « drivers » pour les modules suivants:
  - Camera (video adapteur)
  - [Actuateur linéaire Zaber](https://www.zaber.com/products/product_detail.php?detail=T-LA60A&tab=Series+Features)
  - [Pousse-seringue Aladdin](http://www.wpi-europe.com/products/pumps-and-microinjection/laboratory-syringe-pumps/al1000-220.aspx)
  - [Micromanipulateur Sutter MP-285](http://www.sutter.com/MICROMANIPULATION/mp285.html)
  - Trackeur d’objet temps réél
  - [Controlleur de statif Nikon TI](https://www.nikoninstruments.com/en_CH/Products/Inverted-Microscopes/Eclipse-Ti-E)
  - Shutter TTL (TTL Output)
  - [Shutter Thorlabs (Thorlabs SH05)](https://www.thorlabs.de/thorproduct.cfm?partnumber=SH05)
  - Commentaires textes

## Pré-requis

Il faut posséder une version de Matlab, >= 2012.

## Démarrer avec Controller 42
  - Dézipper les fichiers dans le répertoire de votre choix
  - Lancer Matlab puis choisir ce répertoire comme répertoire de travail
  - Exécuter cMin dans Matlab > la configuration la plus basique du soft s'éxecute
  - Modifier le fichier ini/minconfig.ini pour préciser les utilisateurs et le répertoire d'enregistrement des données
  - Tester indépendemment les modules à ajouter
  - Ajouter les modules dans un nouveau fichier .ini (s'inspirer des exemples fournis)
  - Créer un nouveau script matlab de lancement du programme avec le nouveau fichier ini
  - L'exécuter

## Notes
Le logiciel peut être utilisé soit à l'aide de Controller 42 pour controller tous les modules à la fois, ou alors pour contrôler un module uniquement.

## Crédits
Projet développé par Nicolas Chiaruttini, débuté le 17/04/2012, au sein du laboratoire Aurélien Roux, à l'Université de Genève.

Ce projet contient un objet permettant de manipuler des fichiers de configuration:

> First release on 29.01.2003
> (c) Primoz Cermelj, Slovenia
> Contact: primoz.cermelj@gmail.com
> Download location: http://www.mathworks.com/matlabcentral/fileexchange/loadFile.do?objectId=2976&objectType=file