default: report.ps

report.bbl: report.aux
	bibtex report.aux
	sed -i '/\\begin/a \\\vspace{-7pt}' report.bbl
	sed -i '/\\begin/i \\\vspace{-7pt}' report.bbl

report.aux: report.tex
	pdflatex report.tex

report.pdf: report.bbl report.tex
	pdflatex report.tex
	pdflatex report.tex

img/deepseatreasure.eps: img/deepseatreasure.pdf
	pdftops -eps img/deepseatreasure.pdf

img/buridansass.eps: img/buridansass.pdf
	pdftops -eps img/buridansass.pdf

report.dvi: report.bbl report.tex img/deepseatreasure.eps img/buridansass.eps
	latex report.tex
	latex report.tex

report.ps: report.dvi
	dvips -t A4 -P pdf report.dvi

all: report.pdf report.ps

clean:
	@rm -f *.aux *.log *.bbl *.blg

really-clean:
	@rm -f *.pdf *.dvi *.aux *.log *.bbl *.blg *.ps
