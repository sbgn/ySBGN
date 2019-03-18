# ySBGN - Bidirectional translation between GraphML (yEd Graph Editor) and SBGN-ML

The aim is to provide the most accurate translation for metabolic networks between [GraphML (yEd)](https://www.yworks.com/products/yed) and [SBGN-ML Process Description](https://sbgn.github.io/sbgn/) file formats. Translation in both direction is possible. This project should ultimately be integrated into the [SBFC](https://www.ebi.ac.uk/biomodels/tools/converters/).

Compatible formats:
 - SBGN-ML 0.2 (Process Description)
 - yEd 3.17.1

To find more information, please check the [Wiki](https://github.com/sbgn/ySBGN/wiki). A brief comparison of the tools we used to edit biological diagrams in the SBGN Process Description language is available in the [Features for managing large scale biological diagrams in SBGN PD](https://github.com/sbgn/ySBGN/wiki/Features-editor) page. The translation mapping rules are briefly described in [Translation mapping](https://github.com/sbgn/ySBGN/wiki/Translation-mapping) page. The latest release including executable application and source files can be directly donwloaded from [here](https://github.com/sbgn/ySBGN/releases).

Known issues are shown in the [Issues](https://github.com/sbgn/ySBGN/issues) section and the limitations of the tool are listed on the [Limitations](https://github.com/sbgn/ySBGN/wiki/Limitations) page.

## Requirements

 - Java 8 (with JavaFX if you want to use the GUI)
 - Maven (tested with Maven 3.5)

## Install

After cloning the repository and getting into its directory:

`mvn clean`

`mvn install`

A small GUI is also provided as the main class of the package. It can be launched by double clicking on the jar or by directly calling the package with `java -jar`. Be sure to have JavaFX working in your Java distribution.

The executable jar can be directly downloaded from the [Release page](https://github.com/sbgn/ySBGN/releases).

## How to contribute

If you have any suggestions or want to report a bug, don't hesitate to create an [issue](https://github.com/sbgn/ySBGN/issues). Pull requests and all forms of contribution will be warmly welcomed.

## Contributors

Irina Balaur, [EISBM](http://www.eisbm.org/), Lyon, France - specified the translation rules, developed the code and the GUI

Alexander Mazein, [EISBM](http://www.eisbm.org/), Lyon, France - idea, advice on the translation rules, project coordination

Ludovic Roy, [EISBM](http://www.eisbm.org/), Lyon, France - advised on the SBGN-ML format  

Vasundra Touré, [NTNU](https://www.ntnu.edu/about), Trondheim, Norway - advised on specific issues  

Charles Auffray, [EISBM](http://www.eisbm.org/), Lyon, France - strategic advice  

## Useful links

 - [SBGN-ML](https://github.com/sbgn/sbgn/wiki/SBGN_ML)
  - [yEd Graph Editor](https://www.yworks.com/products/yed)
 - [SBFC](http://sbfc.sourceforge.net/mediawiki/index.php/Main_Page)
 - [Newt Editor](http://web.newteditor.org/#)
 - [CD2SBGMML](https://github.com/sbgn/cd2sbgnml) - translation between [CellDesigner](http://www.celldesigner.org/) and [SBGN-ML](https://github.com/sbgn/sbgn/wiki/SBGN_ML) in both directions
 - [eTRIKS](https://www.etriks.org/) 
 - [Disease Maps Project](http://disease-maps.org/) 

## Acknowledgements

This work has been supported by the Innovative Medicines Initiative Joint Undertaking under grant agreement no. IMI 115446 (eTRIKS), resources of which are composed of financial contribution from the European Union’s Seventh Framework Programme (FP7/2007-2013) and EFPIA companies.


