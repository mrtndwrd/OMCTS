default: thesis.pdf

thesis.bbl: thesis.aux
	bibtex thesis.aux

thesis.aux: thesis.tex
	pdflatex thesis.tex

thesis.pdf: thesis.bbl thesis.tex
	pdflatex thesis.tex
	pdflatex thesis.tex

img/deepseatreasure.eps: img/deepseatreasure.pdf
	pdftops -eps img/deepseatreasure.pdf

img/buridansass.eps: img/buridansass.pdf
	pdftops -eps img/buridansass.pdf

thesis.ps: thesis.dvi
	dvips -t A4 -P pdf thesis.dvi

all: thesis.pdf thesis.ps

clean:
	@rm -f *.aux *.log *.bbl *.blg

really-clean:
	@rm -f *.pdf *.dvi *.aux *.log *.bbl *.blg *.ps
