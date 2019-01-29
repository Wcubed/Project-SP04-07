#!/bin/bash

mkdir -p build
pdflatex --output-directory build report.tex
biber --input-directory build --output-directory build report
pdflatex --output-directory build report.tex
