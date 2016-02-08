default: presentation.pdf

presentation.bbl: presentation.aux
	bibtex presentation.aux

presentation.aux: presentation.tex
	pdflatex presentation.tex

presentation.pdf: presentation.bbl presentation.tex
	pdflatex presentation.tex
	pdflatex presentation.tex

clean:
	@rm -f *.aux *.log *.bbl *.blg

really-clean:
	@rm -f *.pdf *.dvi *.aux *.log *.bbl *.blg *.ps
