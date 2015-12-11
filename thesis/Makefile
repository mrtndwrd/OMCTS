default: thesis.ps

thesis.bbl: thesis.aux
	bibtex thesis.aux
	sed -i '/\\begin/a \\\vspace{-7pt}' thesis.bbl
	sed -i '/\\begin/i \\\vspace{-7pt}' thesis.bbl

thesis.aux: thesis.tex
	pdflatex thesis.tex

thesis.pdf: thesis.bbl thesis.tex
	pdflatex thesis.tex
	pdflatex thesis.tex

img/deepseatreasure.eps: img/deepseatreasure.pdf
	pdftops -eps img/deepseatreasure.pdf

img/buridansass.eps: img/buridansass.pdf
	pdftops -eps img/buridansass.pdf

thesis.dvi: thesis.bbl thesis.tex img/deepseatreasure.eps img/buridansass.eps
	latex thesis.tex
	latex thesis.tex

thesis.ps: thesis.dvi
	dvips -t A4 -P pdf thesis.dvi

all: thesis.pdf thesis.ps

clean:
	@rm -f *.aux *.log *.bbl *.blg

really-clean:
	@rm -f *.pdf *.dvi *.aux *.log *.bbl *.blg *.ps
