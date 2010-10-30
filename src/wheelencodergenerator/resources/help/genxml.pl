#!/usr/bin/perl

print '<!DOCTYPE index PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp Index Version 1.0//EN" "http://java.sun.com/products/javahelp/index_1_0.dtd">',"\n";
print "<index version=\"1.0\">\n";
while (<>) {
  chomp;
  s/\s*$//g;
  ($text, $target) = split(/:/);
  print "<indexitem target=\"$target\" text=\"$text\"/>\n";
}
print "</index>\n";
