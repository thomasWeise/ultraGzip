# Ultra GZIP

[<img alt="Travis CI Build Status" src="https://img.shields.io/travis/thomasWeise/ultraGzip/master.svg" height="20"/>](https://travis-ci.org/thomasWeise/ultraGzip/)

This tool tries to achieve the maximum possible [`gzip`](https://en.wikipedia.org/wiki/Gzip) compression regardless of the necessary runtime.

It therefore attempts to compress an array of bytes by using several different settings of different `gzip` implementations available
for Java, as well as other compression software installed on the system.

## 1. How to use `Ultra GZIP` from the Command Line

Run `java -jar ultraGzip-0.8.8-full.jar ARGUMENTS`

The following arguments are supported:

- `in=/path/to/file` the path to the file with the source data to be compressed
- `si` compress contents written to `stdin` instead of a file. You must specify either `in=...` or the `si` option. 
- `out=/path/to/file` the path to the file where the compressed data should be written to
- `so` write the compressed contents to `stdout` instead of a file. You must specify either `out=...` or the `so` option.
- `help` print the help screen
- `gzip=/path/to/gzip`, `advdef=/path/to/advancecomp`, `7z=/path/to/7z`, `python3=/path/to/python3`, `pigz=/path/to/pigz`, `zopfli=/path/to/zopfli`: optional arguments for specifying the paths to the external software tools that may be used by this program.  


## 2. Requirements

If you are on Linux, the following utilities can improve the compression which can be achieved by
installing the following additional programs:

* [gzip](http://en.wikipedia.org/wiki/Gzip) (should already be installed)
* [AdvanceCOMP](https://en.wikipedia.org/wiki/AdvanceCOMP) (`sudo apt-get install advancecomp`)
* [7-zip](http://www.7-zip.org/) (`sudo apt-get install p7zip-full`)
* [zopfli](http://en.wikipedia.org/wiki/Zopfli) (`sudo apt-get install zopfli`)
* [pigz](http://zlib.net/pigz/) (`sudo apt-get install pigz`)
* [Python 3's gzip](http://docs.python.org/3/library/gzip.html) library, which should normally be installed, too

## 3. Use as `Ultra GZIP` in your Java Code

You can import the class `UltraGzip` and then create a job which takes an array of `byte` as input data and returns an array of `byte` as compression result. The job implements `Callable<byte[]>`.

## 4. Licensing

This software uses [JZlib](http://www.jcraft.com/jzlib/) as one of its internal `gzip` utilities. Although our software here is `GPL` licensed, JZlib is under a [BSD-style license](http://www.jcraft.com/jzlib/LICENSE.txt).
The GPL licensing of our software therefore _only_ applies to our own code, while the code of JZLib follows said [BSD-style license](http://www.jcraft.com/jzlib/LICENSE.txt).
The binary distribution of our software may include binary versions of JZlib.

## 5. Contact

If you have any questions or suggestions, please contact
[Prof. Dr. Thomas Weise](http://iao.hfuu.edu.cn/team/director) of the
[Institute of Applied Optimization](http://iao.hfuu.edu.cn/) at
[Hefei University](http://www.hfuu.edu.cn) in
Hefei, Anhui, China via
email to [tweise@hfuu.edu.cn](mailto:tweise@hfuu.edu.cn) with CC to [tweise@ustc.edu.cn](mailto:tweise@ustc.edu.cn).
