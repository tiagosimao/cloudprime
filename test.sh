#!/bin/sh
echo 'requesting trivial job'
curl http://localhost:8000/f.html?n=1337
echo 'requesting a massive job async'
curl http://localhost:8000/f.html?n=133737788485179 &
echo 'requesting a trivial job again'
curl http://localhost:8000/f.html?n=1337
echo 'requesting a massive job again, a new instance should be generated'
sleep 10s
curl http://localhost:8000/f.html?n=133737788485179

