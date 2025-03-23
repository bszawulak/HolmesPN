Holmes is a stand-alone Java program designed for creating and analyzing various types of Petri nets. Many analytical algorithms and visualization methods have been implemented to aid researcher in the analysis of a given biological model based on Petri net theory.

Installation: not required, archive must be unpacked with its directory structure, Holmes.jar is a Java exectutable file (run.bat file can also be used to start the program).

Necessary components:
-Java Runtime Environment version 1.7 or 1.8 (Java JRE)
-R language (R language) with three free libraries: amap, fpc and cluster for the cluster-based analysis (performed from the Holmes interface, no R language knowledge is required except the installation of the R environment). User should make sure that the path to the rscript.exe (32b/x86 version) is valid. It can be checked and changed in Holmes Properties window (menu Windows->Properties(Ctrl+w) ) by clicking Set R Path button in the Properties window).
-For the INA-based invariant generation, INAwin32.exe program (freely available from the (Integrated Network Analyzer)) can be put into the //tools directory. INAwin32 is however not necessary (it is only an additional feature of Holmes), because a fully working p/t-invariant generator is implemented as a default one in Holmes.

Authors and history: see Help -> About... in Holmes main window.

This is free software, published under the Artistic License 2.0 dependend on non-free software.
