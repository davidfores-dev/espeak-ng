VALENCIÀ

eSpeak NG Text-to-Speech + [Projecte AINA](https://politiquesdigitals.gencat.cat/ca/economia/catalonia-ai/aina) = MATXA TTS

És un projecte personal sorgit de la necessitat de poder parlar en la meua llengua materna, el Valencià. Com que no podia perquè estava operat de la boca vaig fer servir la IA Claude per a que em creara una app per a poder comunicar-me. Claude ha utilitzat el proyecte eSpeak y el Projecte AINA per a poder fer aquesta app. Es tracta d'una app feta per a la tauleta Xiaomi 6s pro 12.4 en Hyper OS 3.0.302.0. Desconec si funciona en més dispositius. La app té un apartat per a afegir botons amb frases ràpides y després un espai on poder escriure el que vols. Baix d'eixe espai pots seleccionar a Lluc (veu masculina) o Gina (veu femenina) del projecte AINA, amb la varietat valenciana. Finalment, tenim el boto reproduir que serveix per a reproduir el que vols que es transcriga a veu.

<img width="3048" height="2032" alt="Screenshot_2026-09-24-00-57-34-948_com reecedunn espeak" src="https://github.com/user-attachments/assets/bf4cc0d7-5886-4f09-a761-84de1f301eed" />
# eSpeak NG Text-to-Speech

* [Característiques](#features)
* [Idiomes compatibles](docs/languages.md)
* [Documentació](#documentation)
* [Compatibilitat amb eSpeak](#espeak-compatibility)
* [Història](#history)
* [Informació sobre la llicència](#license-information)

---

eSpeak NG és un sintetitzador de veu de codi obert, compacte i lleuger, disponible per a Linux, Windows, Android i altres sistemes operatius. És compatible amb [més de 100 idiomes i accents](docs/languages.md) i està basat en el motor eSpeak creat per Jonathan Duddington.

eSpeak NG utilitza un mètode de «síntesi per formants». Això permet oferir una gran quantitat d’idiomes ocupant molt poc d’espai. La veu és clara i es pot utilitzar a velocitats elevades, encara que no resulta tan natural ni suau com la d’altres sintetitzadors més grans basats en enregistraments de veu humana. També és compatible amb la síntesi per formants Klatt i permet utilitzar MBROLA com a motor de síntesi de veu backend.

eSpeak NG està disponible com a:

* Un programa de [línia d’ordes](src/espeak-ng.1.ronn) per a Linux i Windows que permet reproduir com a veu el text procedent d’un fitxer o de l’entrada estàndard (`stdin`).
* Una versió com a [biblioteca compartida](docs/integration.md) perquè puga ser utilitzada per altres programes. En Windows és una DLL.
* Una versió SAPI5 per a Windows, que permet utilitzar-lo amb lectors de pantalla i altres programes compatibles amb la interfície SAPI5 de Windows.
* eSpeak NG també ha sigut portat a altres plataformes, entre elles Solaris i Mac OSX.

## Característiques

* Inclou diferents veus, les característiques de les quals es poden modificar.
* Pot generar l’eixida de veu com un fitxer WAV.
* És compatible amb SSML (Speech Synthesis Markup Language), encara que de manera incompleta, i també amb HTML.
* Té una mida molt reduïda. El programa i les seues dades, incloent-hi nombrosos idiomes, ocupen només uns pocs MB.
* Es pot utilitzar com a front-end per a les [veus de difonema MBROLA](docs/mbrola.md). eSpeak NG converteix el text en fonemes amb informació sobre el to i la duració.
* Pot traduir text a codis de fonemes, de manera que es pot adaptar com a front-end per a altres motors de síntesi de veu.
* Té potencial per a incorporar altres idiomes. Alguns ja estan inclosos en diferents fases de desenvolupament. L’ajuda de parlants nadius per a estos o altres idiomes és benvinguda.
* Està escrit en C.

Consulta el [ChangeLog](ChangeLog.md) per a veure una descripció dels canvis introduïts en les diferents versions i en el projecte eSpeak NG.

Les plataformes següents són compatibles:

| Plataforma | Versió mínima | Estat                                                                            |
| ---------- | ------------- | -------------------------------------------------------------------------------- |
| Linux      |               | ![CI](https://github.com/espeak-ng/espeak-ng/actions/workflows/ci.yml/badge.svg) |
| BSD        |               |                                                                                  |
| Android    | 4.0           |                                                                                  |
| Windows    | Windows 8     |                                                                                  |
| Mac        |               |                                                                                  |

## Documentació

1. La [guia d’usuari](docs/guide.md) explica com configurar i utilitzar eSpeak NG des de la línia d’ordes o com a biblioteca.
2. La [guia de compilació](docs/building.md) proporciona informació sobre com compilar i construir eSpeak NG a partir del codi font.
3. L’[índex](docs/index.md) proporciona una llista completa amb informació més detallada per a col·laboradors i desenvolupadors.
4. Consulta la [guia de contribució](docs/contributing.md) per a començar a col·laborar amb el projecte.
5. Consulta el [full de ruta d’eSpeak NG](https://github.com/espeak-ng/espeak-ng/wiki/eSpeak-NG-roadmap) per a participar en el desenvolupament d’eSpeak NG.

## Compatibilitat amb eSpeak

Els binaris d’*espeak-ng* utilitzen les mateixes opcions de línia d’ordes que *espeak*, juntament amb diverses opcions addicionals que incorporen noves funcionalitats pròpies d’*espeak-ng*, com ara la possibilitat d’especificar el nom del dispositiu d’eixida d’àudio que s’ha d’

ENGLISH
# eSpeak NG Text-to-Speech

- [Features](#features)
- [Supported languages](docs/languages.md)
- [Documentation](#documentation)
- [eSpeak Compatibility](#espeak-compatibility)
- [History](#history)
- [License Information](#license-information)
----------

The eSpeak NG is a compact open source software text-to-speech synthesizer for 
Linux, Windows, Android and other operating systems. It supports 
[more than 100 languages and accents](docs/languages.md). It is based on the eSpeak engine
created by Jonathan Duddington.

eSpeak NG uses a "formant synthesis" method. This allows many languages to be
provided in a small size. The speech is clear, and can be used at high speeds,
but is not as natural or smooth as larger synthesizers which are based on human
speech recordings. It also supports Klatt formant synthesis, and the ability
to use MBROLA as backend speech synthesizer.

eSpeak NG is available as:

*  A [command line](src/espeak-ng.1.ronn) program (Linux and Windows) to speak text from a file or
   from stdin.
*  A [shared library](docs/integration.md) version for use by other programs. (On Windows this is
   a DLL).
*  A SAPI5 version for Windows, so it can be used with screen-readers and
   other programs that support the Windows SAPI5 interface.
*  eSpeak NG has been ported to other platforms, including Solaris and Mac
   OSX.

## Features

*  Includes different Voices, whose characteristics can be altered.
*  Can produce speech output as a WAV file.
*  SSML (Speech Synthesis Markup Language) is supported (not complete),
   and also HTML.
*  Compact size.  The program and its data, including many languages,
   totals about few Mbytes.
*  Can be used as a front-end to [MBROLA diphone voices](docs/mbrola.md).
   eSpeak NG converts text to phonemes with pitch and length information.
*  Can translate text into phoneme codes, so it could be adapted as a
   front end for another speech synthesis engine.
*  Potential for other languages. Several are included in varying stages
   of progress. Help from native speakers for these or other languages is
   welcome.
*  Written in C.

See the [ChangeLog](ChangeLog.md) for a description of the changes in the
various releases and with the eSpeak NG project.

The following platforms are supported:

| Platform    | Minimum Version | Status |
|-------------|-----------------|--------|
| Linux       |                 | ![CI](https://github.com/espeak-ng/espeak-ng/actions/workflows/ci.yml/badge.svg) |
| BSD         |                 |        |
| Android     | 4.0             |        |
| Windows     | Windows 8       |        |
| Mac         |                 |        |

## Documentation

1. [User guide](docs/guide.md) explains how to set up and use eSpeak NG from command line or as a library.
2. [Building guide](docs/building.md) provides info how to compile and build eSpeak NG from the source.
4. [Index](docs/index.md) provides full list of more detailed information for contributors and developers.
5. Look at [contribution guide](docs/contributing.md) to start your contribution.
6. Look at [eSpeak NG roadmap](https://github.com/espeak-ng/espeak-ng/wiki/eSpeak-NG-roadmap) to participate in development of eSpeak NG.

## eSpeak Compatibility

The *espeak-ng* binaries use the same command-line options as *espeak*, with
several additions to provide new functionality from *espeak-ng* such as specifying
the output audio device name to use. The build creates symlinks of `espeak` to
`espeak-ng`, and `speak` to `speak-ng`.

The espeak `speak_lib.h` include file is located in `espeak-ng/speak_lib.h` with
an optional symlink in `espeak/speak_lib.h`. This file contains the espeak 1.48.15
API, with a change to the `ESPEAK_API` macro to fix building on Windows
and some minor changes to the documentation comments. This C API is API and ABI
compatible with espeak.

The `espeak-data` data has been moved to `espeak-ng-data` to avoid conflicts with
espeak. There have been various changes to the voice, dictionary and phoneme files
that make them incompatible with espeak.

The *espeak-ng* project does not include the *espeakedit* program. It has moved
the logic to build the dictionary, phoneme and intonation binary files into the
`libespeak-ng.so` file that is accessible from the `espeak-ng` command line and
C API.

## History

The program was originally known as __speak__ and originally written
for Acorn/RISC\_OS computers starting in 1995 by Jonathan Duddington. This was
enhanced and re-written in 2007 as __eSpeak__, including a relaxation of the
original memory and processing power constraints, and with support for additional
languages.

In 2010, Reece H. Dunn started maintaining a version of eSpeak on GitHub that
was designed to make it easier to build eSpeak on POSIX systems, porting the
build system to autotools in 2012. In late 2015, this project was officially
forked to a new __eSpeak NG__ project. The new eSpeak NG project is a significant
departure from the eSpeak project, with the intention of cleaning up the
existing codebase, adding new features, and adding to and improving the
supported languages.

The *historical* branch contains the available older releases of the original
eSpeak that are not contained in the subversion repository.

1.24.02 is the first version of eSpeak to appear in the subversion
repository, but releases from 1.05 to 1.24 are available at
[http://sourceforge.net/projects/espeak/files/espeak/](http://sourceforge.net/projects/espeak/files/espeak/).

These early releases have been checked into the historical branch,
with the 1.24.02 release as the last entry. This makes it possible
to use the replace functionality of git to see the earlier history:

	git replace 8d59235f 63c1c019

__NOTE:__ The source releases contain the `big_endian`, `espeak-edit`,
`praat-mod`, `riskos`, `windows_dll` and `windows_sapi` folders. These
do not appear in the source repository until later releases, so have
been excluded from the historical commits to align them better with
the 1.24.02 source commit.

## License Information

eSpeak NG Text-to-Speech is released under the [GPL version 3](COPYING) or
later license.

The `getopt.c` compatibility implementation for getopt support on Windows is
taken from the NetBSD `getopt_long` implementation, which is licensed under a
[2-clause BSD](COPYING.BSD2) license.

Android is a trademark of Google LLC.

## Acknowledgements

The catalan extension was funded by [Departament de la Vicepresidència i de Polítiques Digitals i Territori de la Generalitat de Catalunya](https://politiquesdigitals.gencat.cat/ca/inici/index.html#googtrans(ca|en) 
within the framework of 
[Projecte AINA](https://politiquesdigitals.gencat.cat/ca/economia/catalonia-ai/aina).
