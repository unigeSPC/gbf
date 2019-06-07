# Great Balls of Fire

Great Balls of Fire (**GBF**) is a collection of codes to assess the hazard caused by the impact of volcanic ballistic projectiles. It contains a ballistic model written in *Scala* and post-processing functions written in *Matlab* used to generate a range of probabilistic outputs.



## Installation

The repository contains the following files:

File | Description
----- | ------
`gbf.jar` | Compiled version of the ballistic model
`processGBF.m` | Function to post process model outputs
`displayGBF.m` | Function to display processed outputs
`MANUAL.pdf` | Complete user manual
`README.md` | This file
`LICENSE.md` | License file
`simulator/` | The source code to the GBF model
`dependencies/` | Dependencies to the *Matlab* functions 
`example_run/` | Example of input files

`gbf.jar` is the compiled version of the GBF model.

### Compiling GBF from source
The repository already contains a ready-to-use `.jar` file. To compile **GBF** from source, first download the `sbt` from [here](https://www.scala-sbt.org/index.html) and follow: 

```java
$ sbt
> compile
> oneJar
```
The resulting `.jar` file will be located in the `target/scala-2.11/` directory. Note that the first time the project is built, all necessary libraries will be downloaded. This may take several minutes, but successive builds will be much faster.

## Usage

### Running the model
**GBF** requires two inputs (see examples in the folder `example_run`):
- A *configuration file* (`input.conf`)
- A *DEM file* in an [ArcMap ASCII raster format](http://desktop.arcgis.com/en/arcmap/latest/manage-data/raster-and-images/esri-ascii-raster-format.htm) (`DEM.txt`)

For details on input parameters, refer to the file `GBF_manual.pdf`. The general command to run the model is:
```java
java -jar gbf.jar conf_file nb_workers
```

where `conf_file` is the path to the configuration file and `nb_workers` is the number of requested CPU cores. To run the example run with 4 workers, use:
```java
java -jar gbf.jar example_run/input.conf 4
```

### Model output
The output file is an ASCII file where each line is a different VBP and columns are the following variables:

Column | Variable
-------| --------
**Column 1**| Eastern coordinate of the i-esim clast [m]
**Column 2**| Western coordinate of the i-esim clast [m]
**Column 3**| Altitude on the DEM of the i-esim clast [m]
**Column 4**| Mass of the i-esim clast [kg]
**Column 5**| Diameter of the i-esim clast [m]
**Column 6**| Kinetic energy of the i-esim clast at the impact [J]
**Column 7**| Incident angle at the impact [degrees]
**Column 8**| Ejection angle of the i-esim clast with respect to the vertical [degrees]
**Column 9**| Azimuthal angle of ejection, measured from north to east clockwise [degrees]
**Column 10** | Initial velocity of the i-esim clast [m/s]
**Column 11** | Total time of flight of the i-esim clast [s]

### Post-processing model output
The model output can be post-processed in *Matlab* using the `processGBF.m` file, the output of which can be displayed using the function `displayGBF.m`. A comprehensive description of the post-processing method can be found in [this blog entry](https://e5k.github.io/codes/2017/10/09/ballistic-post-processing/).





## Additional documentation
Instructions are provided in the the file [included documentation](https://github.com/unigeSPC/gbf/raw/master/doc/doc.pdf). Additional instructions on the post-processing of VBP data are also provided in [this blog post](https://e5k.github.io/codes/2017/10/09/ballistic-post-processing/). Updates are presented [here](https://e5k.github.io/pages/gbf).

## Releases
**Jun 2019**: Added columns to GBF output file and remodeled repository
**Apr 2018**: Changes in the definition of the source and new post-processing functions
**Nov 2016**: First release

## Citation
Please cite **GBF** as:
> Sébastien Biass, Jean-Luc Falcone, Costanza Bonadonna, Federico Di Traglia, Marco Pistolesi, Mauro Rosi, Pierino Lestuzzi, Great Balls of Fire: A probabilistic approach to quantify the hazard related to ballistics — A case study at La Fossa volcano, Vulcano Island, Italy, Journal of Volcanology and Geothermal Research, 325, 2016, http://dx.doi.org/10.1016/j.jvolgeores.2016.06.006.

## License
GBF is a free and open source software releaser under GPL 3. See the
documentation or the file `LICENSE.txt` for further information.

