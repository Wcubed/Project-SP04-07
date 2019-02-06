#!/bin/bash

# Copies all the files that need to be submitted and zips them up.

packDir="packaged"
zipName="Spectrangle-Source-s1578472"
zipDir="${packDir}/${zipName}"

# ----------

echo "Cleaning '${packDir}'."
rm -r "${packDir}"

mkdir -p ${packDir}
mkdir -p ${zipDir}

# ----------

echo "Copying report."
cp "report/build/report.pdf" "${packDir}/Spectrangle-s1578472.pdf"

# ----------

echo "Copying readme."
cp "README.md" "${zipDir}/README.md"

echo "Copying protocol."
cp "Network protocol v1_4.pdf" "${zipDir}/Network protocol v1_4.pdf"

echo "Copying src."
cp -a "src" "${zipDir}/src"

echo "Copying doc."
cp -a "doc" "${zipDir}/doc"

echo "Copying lib."
cp -a "lib" "${zipDir}/lib"

echo "Copying artifacts (the complied application)."
cp -a "out/artifacts" "${zipDir}/artifacts"

# ----------

echo "Zipping package."
cd ${packDir}
zip -r "${zipName}.zip" "${zipName}"

echo "Removing temporary zip directory."
rm -r "${zipName}"