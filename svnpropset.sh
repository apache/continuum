#!/bin/bash
#
# Sets svn properties to the correct values for well known file extensions
# This script probably could be simplified to speed it up!

find . -name "*.bat" | grep -v '\.svn' | while read n
do
svn propset svn:mime-type 'text/plain' $n
svn propset svn:eol-style CRLF $n
done

#find . -name "*.bin" | grep -v '\.svn' | while read n
#do
#svn propset svn:mime-type 'application/octet-stream' $n
#svn propdel svn:keywords $n
#done

find . -name "*.bmp" | grep -v '\.svn' | while read n
do
svn propset svn:mime-type 'image/bmp' $n
done

#find . -name "*.c" | grep -v '\.svn' | while read n
#do
#svn propset svn:eol-style native $n
#svn:keywords 'Date Author Id Revision HeadURL' $n
#done

find . -name "*.class" | grep -v '\.svn' | while read n
do
svn propset svn:mime-type 'application/java' $n
svn propdel svn:keywords $n
done

#find . -name "*.cmd" | grep -v '\.svn' | while read n
#do
#svn propset svn:mime-type 'text/plain' $n
#svn propset svn:eol-style CRLF $n
#done

#find . -name "*.cpp" | grep -v '\.svn' | while read n
#do
#svn propset svn:eol-style native  $n
#svn propset svn:keywords 'Date Author Id Revision HeadURL' $n
#done

find . -name "*.css" | grep -v '\.svn' | while read n
do
svn propset svn:mime-type 'text/css' $n
svn propset svn:eol-style native $n
svn propset svn:keywords 'Date Author Id Revision HeadURL' $n
done

#find . -name "*.doc" | grep -v '\.svn' | while read n
#do
#svn propset svn:mime-type 'application/msword $n
#svn propdel svn:keywords $n
#done

#find . -name "*.dsp" | grep -v '\.svn' | while read n
#do
#svn propset svn:eol-style CRLF $n
#done

#find . -name "*.dsw" | grep -v '\.svn' | while read n
#do
#svn propset svn:eol-style CRLF $n
#done

find . -name "*.dtd" | grep -v '\.svn' | while read n
do
svn propset svn:mime-type 'text/plain' $n
svn propset svn:eol-style native $n
svn propset svn:keywords 'Date Author Id Revision HeadURL' $n
done

find . -name "*.ent" | grep -v '\.svn' | while read n
do
svn propset svn:mime-type 'text/plain' $n
svn propset svn:eol-style native $n
svn propset svn:keywords 'Date Author Id Revision HeadURL' $n
done

find . -name "*.exe" | grep -v '\.svn' | while read n
do
svn propset svn:mime-type 'application/octet-stream' $n
svn propdel svn:keywords $n
done

find . -name "*.gif" | grep -v '\.svn' | while read n
do
svn propset svn:mime-type 'image/gif' $n
svn propdel svn:keywords $n
done

find . -name "*.gz" | grep -v '\.svn' | while read n
do
svn propset svn:mime-type 'application/x-gzip' $n
svn propdel svn:keywords $n
done

#find . -name "*.h" | grep -v '\.svn' | while read n
#do
#svn propset svn:eol-style native $n
#svn propset svn:keywords 'Date Author Id Revision HeadURL' $n
#done

find . -name "*.htm" | grep -v '\.svn' | while read n
do
svn propset svn:mime-type 'text/html' $n
svn propset svn:eol-style native $n
svn propset svn:keywords 'Date Author Id Revision HeadURL' $n
done

find . -name "*.html" | grep -v '\.svn' | while read n
do
svn propset svn:mime-type 'text/html' $n
svn propset svn:eol-style native $n
svn propset svn:keywords 'Date Author Id Revision HeadURL' $n
done

find . -name "*.jar" | grep -v '\.svn' | while read n
do
svn propset svn:mime-type 'application/java-archive' $n
svn propdel svn:keywords $n
done

find . -name "*.java" | grep -v '\.svn' | while read n
do
svn propset svn:mime-type 'text/plain' $n
svn propset svn:eol-style native $n
svn propset svn:keywords 'Date Author Id Revision HeadURL' $n
done

find . -name "*.jpeg" | grep -v '\.svn' | while read n
do
svn propset svn:mime-type 'image/jpeg' $n
svn propdel svn:keywords $n
done

find . -name "*.jpg" | grep -v '\.svn' | while read n
do
svn propset svn:mime-type 'image/jpeg' $n
svn propdel svn:keywords $n
done

find . -name "*.js" | grep -v '\.svn' | while read n
do
svn propset svn:mime-type 'text/plain' $n
svn propset svn:eol-style native $n
svn propset svn:keywords 'Date Author Id Revision HeadURL' $n
done

find . -name "*.jsp" | grep -v '\.svn' | while read n
do
svn propset svn:mime-type 'text/plain' $n
svn propset svn:eol-style native $n
svn propset svn:keywords 'Date Author Id Revision HeadURL' $n
done

#find . -name "*.obj" | grep -v '\.svn' | while read n
#do
#svn propset svn:mime-type 'application/octet-stream' $n
#svn propdel svn:keywords $n
#done

find . -name "*.pdf" | grep -v '\.svn' | while read n
do
svn propset svn:mime-type 'application/pdf' $n
svn propdel svn:keywords $n
done

find . -name "*.png" | grep -v '\.svn' | while read n
do
svn propset svn:mime-type 'image/png' $n
svn propdel svn:keywords $n
done

find . -name "*.properties" | grep -v '\.svn' | while read n
do
svn propset svn:mime-type 'text/plain' $n
svn propset svn:eol-style native $n
svn propset svn:keywords 'Date Author Id Revision HeadURL' $n
done

find . -name "*.sh" | grep -v '\.svn' | while read n
do
svn propset svn:mime-type 'text/plain' $n
svn propset svn:eol-style native $n
svn propset svn:keywords 'Date Author Id Revision HeadURL' $n
done

find . -name "*.sql" | grep -v '\.svn' | while read n
do
svn propset svn:mime-type 'text/plain' $n
svn propset svn:eol-style native $n
svn propset svn:keywords 'Date Author Id Revision HeadURL' $n
done

find . -name "*.tgz" | grep -v '\.svn' | while read n
do
svn propset svn:mime-type 'application/octet-stream' $n
svn propdel svn:keywords $n
done

#find . -name "*.tif" | grep -v '\.svn' | while read n
#do
#svn propset svn:mime-type 'image/tiff' $n
#svn propdel svn:keywords $n
#done

#find . -name "*.tiff" | grep -v '\.svn' | while read n
#do
#svn propset svn:mime-type 'image/tiff' $n
#svn propdel svn:keywords $n
#done

find . -name "*.txt" | grep -v '\.svn' | while read n
do
svn propset svn:mime-type 'text/plain' $n
svn propset svn:eol-style native $n
svn propset svn:keywords 'Date Author Id Revision HeadURL' $n
done

find . -name "*.wsdl" | grep -v '\.svn' | while read n
do
svn propset svn:mime-type 'text/xml' $n
svn propset svn:eol-style native $n
svn propset svn:keywords 'Date Author Id Revision HeadURL' $n
done

find . -name "*.xml" | grep -v '\.svn' | while read n
do
svn propset svn:mime-type 'text/xml' $n
svn propset svn:eol-style native $n
svn propset svn:keywords 'Date Author Id Revision HeadURL' $n
done

find . -name "*.xsd" | grep -v '\.svn' | while read n
do
svn propset svn:mime-type 'text/xml' $n
svn propset svn:eol-style native $n
svn propset svn:keywords 'Date Author Id Revision HeadURL' $n
done

find . -name "*.xsl" | grep -v '\.svn' | while read n
do
svn propset svn:mime-type 'text/xml' $n
svn propset svn:eol-style native $n
svn propset svn:keywords 'Date Author Id Revision HeadURL' $n
done

find . -name "*.zip" | grep -v '\.svn' | while read n
do
svn propset svn:mime-type 'application/zip' $n
svn propdel svn:keywords $n
done

#find . -name "Makefile" | grep -v '\.svn' | while read n
#do
#svn propset svn:mime-type 'text/xml' $n
#svn propset svn:eol-style native $n
#svn propset svn:keywords 'Date Author Id Revision HeadURL' $n
#done

