#!/usr/bin/env python
"""Build an article zip file from a set of directories.

Usage:
    makearticle.py [OPTIONS]

Options:
 -a                 Build all the zip files you can
 -k <article key>   Specify unique portion of pmc filename to build zip for
 -r <rootdir>       Directory where original stuff lives
 -h                 Help (this message)

Examples:
    makearticle.py -a -r /home/plos_pubs/cdrom/2/3
    makearticle.py -r pbio.0020073 -r /home/plos_pubs/cdrom/2/3
"""
from getopt import getopt
import zipfile
import sys
import os

# Globals set during option parsing
all = False
key = ""
root = "."

def main(argv):
    # parse arguments
    global root, all, key
    try:
        opts, args = getopt(argv[1:], "ak:r:h?", [ "help" ])
    except Exception, e:
        print e
        usage()

    for opt, arg in opts:
        if opt == "-h" or opt == "-?" or opt == "--help": usage()
        elif opt == "-k": key = arg
        elif opt == "-r": root = arg
        elif opt == "-a": all = True

    if all:
        # We want to parse all messages, so get list of files
        keys = os.listdir(root + "/pmc")
        for key in keys:
            try:
                name = key[:key.rindex(".xml")]
            except Exception, e:
                print e
                continue # no idea
            print "Building " + name
            makeZip(name, root)
    else:
        # Just process one file
        makeZip(key, root)

def usage():
    print __doc__
    sys.exit(1)

def makeZip(key, rootdir):
    """Build a zip file from content in a collection of directories

    @param key: A unique portion of the pmc (and other) files
    @param rootdir: Directory that contains pmc, pdf, supinfo, table, etc. dirs
    """
    # Create zip file
    z = zipfile.ZipFile(key + ".zip", "w")

    # Iterate over directories
    dirs = os.listdir(rootdir)
    for dir in dirs:
        try: files = os.listdir(rootdir + "/" + dir)
        except: continue # not a dir
        for fname in files:
            if key in fname:
                if dir == "pmc":
                    z.write(rootdir + "/" + dir + "/" + fname, "pmc.xml")
                else:
                    extpos = fname.rindex('.')
                    ext = fname[extpos:]
                    name = fname[:extpos]
                    name = name.replace("-O", "")
                    name = name.replace("-S", "")
                    name = name.replace(".", "%2E")
                    name = name.replace("/", "%2F")
                    name += ext
                    z.write(rootdir + "/" + dir + "/" + fname, name)

if __name__ == "__main__":
    sys.exit(main(sys.argv))

