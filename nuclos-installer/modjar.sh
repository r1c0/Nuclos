#!/bin/bash -x

KEYSTORE="../nuclos-war/.keystore"
ALIAS='nuclos'
KEYPASS='solcun'
STOREPASS='solcun'

# TMPDIR=`mktemp -d`

unsign() {
	local JAR="$1"
	shift
	zip -d "$JAR" META-INF/\*.SF META-INF/\*.DSA META-INF/\*.RSA INDEX.LIST
}

sign() {
	local JAR="$1"
	shift
	jarsigner -keystore ".keystore" -storepass "$STOREPASS" -keypass "$KEYPASS" "$JAR" "$ALIAS"
}

verify() {
	local JAR="$1"
	shift
	jarsigner -verify "$JAR"
}

index() {
	local JAR="$1"
	shift
	jar i "$JAR" || reindex "$JAR"
}

reindex() {
	local JAR="$1"
	shift
	local TMPDIR=`mktemp -d`
	echo "Bogus $JAR: repacking/reindexing"
	unzip "$JAR" -d "$TMPDIR"
	rm "$JAR"
	jar cf "$JAR" -C "$TMPDIR" .
	index "$JAR"
	rm -r "$TMPDIR"
}

repack() {
	local JAR="$1"
	shift
	pack200 --repack "$JAR"
}

pack() {
	local JAR="$1"
	shift
	pack200 "$JAR.pack.gz" "$JAR"
	rm "$JAR"
}

modjar() {
	local DIR="$1"
	shift
	local TARGET="$1"
	shift
	echo "Processing $TARGET"
	unsign "$TARGET"
	(index "$TARGET" && \
		repack "$TARGET" && \
		sign "$TARGET" && \
		pack "$TARGET" ) || \
#		verify "$TARGET") || \
		exit -1
}

modjar2() {
	local DIR="$1"
	shift
	local TARGET="$1"
	shift
	echo "Processing2 $TARGET"
	unsign "$TARGET"
	(index "$TARGET" && \
		repack "$TARGET" && \
		sign "$TARGET" && \
#		pack "$TARGET" ) || \
		verify "$TARGET") || \
		exit -1
}

DIR="$1"
REGEX="(xalan)-.*jar"
shift

cp $KEYSTORE $DIR/.keystore
pushd $DIR
	for i in $*; do
		if [[ $i =~ $REGEX ]]; then
			echo "No packing of $i"
			modjar2 "$DIR" "$i" &
		else 
			modjar "$DIR" "$i" &
		fi
		# only start 4 concurrent jobs
		while [ `jobs | wc -l` -ge 4 ]; do
				sleep 2
		done
	done
	# wait for all jobs (last job is this shell script)
	while [ `jobs | wc -l` -gt 1 ]; do
			sleep 2
	done
	rm .keystore
popd
