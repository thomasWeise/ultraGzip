import gzip
import sys

contentIn = sys.stdin.buffer.read()
contentOut = gzip.compress(contentIn)
sys.stdout.buffer.write(contentOut)
