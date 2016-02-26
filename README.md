# Ultra GZIP

This tool tries to achieve the maximum possible [`gzip`](https://en.wikipedia.org/wiki/Gzip) compression regardless of the necessary runtime.

It therefore attempts to compress an array of bytes by using several different settings of different `gzip` implementations available
for Java, as well as other compression software installed on the system.


## 1. How to use `Ultra GZIP` from the Command Line

## 2. Requirements

If you are on Linux, the following utilities can improve the compression which can be achieved by
installing the following additional programs:

* [gzip](https://en.wikipedia.org/wiki/Gzip) (should already be installed)
* [AdvanceCOMP](https://en.wikipedia.org/wiki/AdvanceCOMP) (`sudo apt-get install advancecomp`)
* [7-zip](http://www.7-zip.org/) (`sudo apt-get install p7zip-full`)
* [zopfli](https://en.wikipedia.org/wiki/Zopfli) (`sudo apt-get install zopfli`)


## 3. Use as `Ultra GZIP` in your Java Code