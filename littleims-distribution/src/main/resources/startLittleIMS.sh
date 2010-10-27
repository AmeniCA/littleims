#! /bin/sh
./HSS/bin/hss.sh start &
./P-CSCF/bin/pcscf.sh start &
./S-CSCF/bin/scscf.sh start &
./I-CSCF/bin/icscf.sh start

