#!/bin/bash

mkdir -p build
# Twice, because things like the table of contents need two passes to update properly.
pdflatex --output-directory build report.tex
pdflatex --output-directory build report.tex