for x in `find . -type f -name '*.java'`
do
	iconv -f iso8859-1 -t utf-8 $x > "$x.utf8"
	rm $x;
	mv "$x.utf8" $x
done

for x in `find . -type f -name '*.txt'`
do
	iconv -f iso8859-1 -t utf-8 $x > "$x.utf8"
	rm $x;
	mv "$x.utf8" $x
done

for x in `find . -type f -name '*.header'`
do
	iconv -f iso8859-1 -t utf-8 $x > "$x.utf8"
	rm $x;
	mv "$x.utf8" $x
done