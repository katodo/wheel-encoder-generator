#!/usr/bin/perl

open(FILE, "common.txt");
@common = <FILE>;

print "---\n";

while (<>) {
  ## strip out tags
  s/\<[!a-zA-Z0-9\/]+\s*[a-zA-Z0-9=".;:\-,\#_%\/ ]*\>//g;
  s/&nbsp;//g;
  tr/A-Z/a-z/;

  ## Strip out punctuation
  s/[\(\),.:;?"]+//g;

  ## strip out duplicate spaces
  s/^\s+//g;
  s/\s+%//g;

  #print STDERR;

  @words = split(/\s+/);
  for ($i = 0; $i < $#words; $i++) {
    #print "$words[$i] search = ", grep( /^$words[$i]$/, @common), "\n";
    if ( grep(/^$words[$i]$/, @common) == "" ) {
      $indexwords{$words[$i]}++;
      $index2{$words[$i]}->{$words[$i+1]}++
        if ( ($i+1) < $#words && grep(/^$words[$i+1]$/, @common) == "" );
      $index3{$words[$i]}->{$words[$i+1]}->{$words[$i+2]}++
	if (($i+2) < $#words && grep(/^$words[$i+2]$/, @common) == "" );
    }
  }
}

foreach $w (keys %indexwords) {
  print "$indexwords{$w} $w\n";
  foreach $w2 (keys %{$index2{$w}}) {
    print "$index2{$w}->{$w2} $w $w2\n";
    foreach $w3 (keys %{$index3{$w}->{$w2}}) {
      print "$index3{$w}->{$w2}->{$w3} $w $w2 $w3\n";
    }
  }
}
